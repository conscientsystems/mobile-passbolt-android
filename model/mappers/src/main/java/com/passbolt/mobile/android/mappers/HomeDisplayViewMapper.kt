package com.passbolt.mobile.android.mappers

import com.passbolt.mobile.android.entity.resource.Permission
import com.passbolt.mobile.android.entity.resource.ResourceDatabaseView
import com.passbolt.mobile.android.ui.DefaultFilterUiModel
import com.passbolt.mobile.android.ui.HomeDisplayViewModel
import com.passbolt.mobile.android.ui.HomeDisplayViewUiModel

/**
 * Mapper responsible for mapping between UI related resource display view type and database related
 * ordering and filtering types.
 */
class HomeDisplayViewMapper {
    /**
     * @param homeView UI related resources display view type
     * @return Database related type for order or filter
     */
    fun map(homeView: HomeDisplayViewModel) =
        when (homeView) {
            is HomeDisplayViewModel.AllItems, HomeDisplayViewModel.NotLoaded -> ResourceDatabaseView.ByNameAscending
            is HomeDisplayViewModel.RecentlyModified -> ResourceDatabaseView.ByModifiedDateDescending
            is HomeDisplayViewModel.Favourites -> ResourceDatabaseView.IsFavourite
            is HomeDisplayViewModel.OwnedByMe -> ResourceDatabaseView.HasPermissions(setOf(Permission.OWNER))
            is HomeDisplayViewModel.SharedWithMe ->
                ResourceDatabaseView.HasPermissions(
                    setOf(Permission.READ, Permission.WRITE),
                )
            is HomeDisplayViewModel.Folders -> ResourceDatabaseView.ByModifiedDateDescending
            is HomeDisplayViewModel.Tags -> ResourceDatabaseView.ByModifiedDateDescending
            is HomeDisplayViewModel.Groups -> ResourceDatabaseView.ByModifiedDateDescending
            is HomeDisplayViewModel.Expiry -> ResourceDatabaseView.HasExpiry
        }

    fun map(homeView: HomeDisplayViewUiModel): HomeDisplayViewModel =
        when (homeView) {
            HomeDisplayViewUiModel.ALL_ITEMS -> HomeDisplayViewModel.AllItems
            HomeDisplayViewUiModel.FAVOURITES -> HomeDisplayViewModel.Favourites
            HomeDisplayViewUiModel.RECENTLY_MODIFIED -> HomeDisplayViewModel.RecentlyModified
            HomeDisplayViewUiModel.SHARED_WITH_ME -> HomeDisplayViewModel.SharedWithMe
            HomeDisplayViewUiModel.OWNED_BY_ME -> HomeDisplayViewModel.OwnedByMe
            HomeDisplayViewUiModel.FOLDERS -> HomeDisplayViewModel.folderRoot()
            HomeDisplayViewUiModel.TAGS -> HomeDisplayViewModel.tagsRoot()
            HomeDisplayViewUiModel.GROUPS -> HomeDisplayViewModel.groupsRoot()
            HomeDisplayViewUiModel.EXPIRY -> HomeDisplayViewModel.Expiry
        }

    fun map(
        userSetHomeView: DefaultFilterUiModel,
        lastUsedHomeView: HomeDisplayViewUiModel,
    ): HomeDisplayViewModel =
        when (userSetHomeView) {
            DefaultFilterUiModel.LAST_USED -> map(lastUsedHomeView)
            DefaultFilterUiModel.ALL_ITEMS -> HomeDisplayViewModel.AllItems
            DefaultFilterUiModel.FAVOURITES -> HomeDisplayViewModel.Favourites
            DefaultFilterUiModel.RECENTLY_MODIFIED -> HomeDisplayViewModel.RecentlyModified
            DefaultFilterUiModel.SHARED_WITH_ME -> HomeDisplayViewModel.SharedWithMe
            DefaultFilterUiModel.OWNED_BY_ME -> HomeDisplayViewModel.OwnedByMe
            DefaultFilterUiModel.FOLDERS -> HomeDisplayViewModel.folderRoot()
            DefaultFilterUiModel.TAGS -> HomeDisplayViewModel.tagsRoot()
            DefaultFilterUiModel.GROUPS -> HomeDisplayViewModel.groupsRoot()
            DefaultFilterUiModel.EXPIRY -> HomeDisplayViewModel.Expiry
        }
}
