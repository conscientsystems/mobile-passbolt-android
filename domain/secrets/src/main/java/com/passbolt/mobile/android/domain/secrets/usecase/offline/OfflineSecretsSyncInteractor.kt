package com.passbolt.mobile.android.domain.secrets.usecase.offline

import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticatedUseCaseOutput
import com.passbolt.mobile.android.core.mvp.authentication.CompleteAuthenticatedOutput
import com.passbolt.mobile.android.core.mvp.authentication.IncompleteAuthenticatedOutput
import com.passbolt.mobile.android.domain.accounts.usecase.GetSelectedAccountUseCase
import com.passbolt.mobile.android.domain.preferences.AccountFlagsUpdate
import com.passbolt.mobile.android.domain.preferences.AccountPreferencesRepository
import com.passbolt.mobile.android.domain.secrets.offline.OfflineCacheRepository
import com.passbolt.mobile.android.domain.secrets.offline.OfflineCachedSecret
import com.passbolt.mobile.android.domain.secrets.offline.ResourceModifiedState
import com.passbolt.mobile.android.ui.OfflineModeSetting
import timber.log.Timber
import java.time.ZonedDateTime

/**
 * Keeps the local encrypted secret cache in step with the offline setting.
 *
 * Runs at the end of every full data refresh, after the resources table has been
 * updated. Only secrets whose resource is newer than the cached copy (or not cached
 * yet) are fetched, in batches through `resources.json?contain[secret]=1&filter[has-id][]`,
 * so a steady-state refresh costs nothing when nothing changed. Secrets of resources
 * that left the offline set are deleted. A failure leaves the existing cache in place.
 */
class OfflineSecretsSyncInteractor(
    private val offlineCacheRepository: OfflineCacheRepository,
    private val accountPreferencesRepository: AccountPreferencesRepository,
    private val getSelectedAccountUseCase: GetSelectedAccountUseCase,
) {
    suspend fun sync(onProgress: suspend (done: Int, total: Int) -> Unit = { _, _ -> }): Output {
        val userId = requireNotNull(getSelectedAccountUseCase.execute(Unit).selectedAccount)
        val mode = accountPreferencesRepository.getAccountFlags(userId).offlineMode
        if (!mode.isEnabled) {
            if (offlineCacheRepository.countCachedSecrets(userId) > 0) {
                Timber.d("[Offline] Mode is off - dropping cached secrets")
                offlineCacheRepository.removeAllCachedSecrets(userId)
            }
            return Output.Disabled
        }

        val localResources = offlineCacheRepository.getLocalResourcesState(userId).associateBy { it.resourceId }
        val targetIds: Set<String> =
            when (mode) {
                OfflineModeSetting.ALL_ENTRIES -> localResources.keys
                OfflineModeSetting.SELECTED_ENTRIES ->
                    offlineCacheRepository.getMarkedResourceIds(userId).filter { it in localResources }.toSet()
                OfflineModeSetting.OFF -> emptySet()
            }
        val cached = offlineCacheRepository.getCachedSecretsState(userId).associateBy { it.resourceId }

        val toRemove = cached.keys - targetIds
        if (toRemove.isNotEmpty()) {
            Timber.d("[Offline] Removing ${toRemove.size} cached secrets no longer in the offline set")
            offlineCacheRepository.removeCachedSecrets(toRemove.toList(), userId)
        }

        val stale =
            targetIds.filter { id ->
                val cachedState = cached[id]
                val resource = requireNotNull(localResources[id])
                cachedState == null || cachedState.modified.isBefore(resource.modified)
            }
        Timber.d("[Offline] ${targetIds.size} in offline set, ${stale.size} to fetch")

        var done = 0
        stale.chunked(BATCH_SIZE).forEach { batch ->
            when (val result = offlineCacheRepository.fetchSecretsForResources(batch)) {
                is DomainResult.Incomplete -> {
                    Timber.e("[Offline] Secret batch fetch failed: $result")
                    return Output.Failure(result)
                }
                is DomainResult.Finished -> {
                    val now = ZonedDateTime.now()
                    offlineCacheRepository.putCachedSecrets(
                        result.value.map {
                            OfflineCachedSecret(
                                resourceId = it.resourceId,
                                encryptedSecret = it.encryptedSecret,
                                resourceModified = resourceModifiedFor(it.resourceId, localResources, it.resourceModified),
                                cachedAt = now,
                            )
                        },
                        userId,
                    )
                    done += batch.size
                    onProgress(done, stale.size)
                }
            }
        }

        accountPreferencesRepository.updateAccountFlags(
            AccountFlagsUpdate(offlineLastSyncEpochMillis = System.currentTimeMillis()),
            userId,
        )
        val cachedCount = offlineCacheRepository.countCachedSecrets(userId)
        Timber.d("[Offline] Sync finished - $cachedCount secrets cached")
        return Output.Success(cachedCount)
    }

    // Prefer the local resource's stamp (the one the next comparison will use) so a
    // clock difference between the two responses cannot make an entry look stale forever.
    private fun resourceModifiedFor(
        resourceId: String,
        localResources: Map<String, ResourceModifiedState>,
        fromServer: ZonedDateTime,
    ): ZonedDateTime = localResources[resourceId]?.modified ?: fromServer

    sealed class Output : AuthenticatedUseCaseOutput {
        data object Disabled : Output(), CompleteAuthenticatedOutput

        data class Success(
            val cachedCount: Int,
        ) : Output(),
            CompleteAuthenticatedOutput

        data class Failure(
            override val incomplete: DomainResult.Incomplete,
        ) : Output(),
            IncompleteAuthenticatedOutput
    }

    private companion object {
        // ~55 chars per uuid in the query string - 40 keeps the request URL short
        private const val BATCH_SIZE = 40
    }
}
