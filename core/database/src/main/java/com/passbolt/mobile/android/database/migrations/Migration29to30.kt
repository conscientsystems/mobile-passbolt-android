package com.passbolt.mobile.android.database.migrations

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Suppress("MagicNumber")
object Migration29to30 : Migration(29, 30) {
    private const val CREATE_OFFLINE_ITEM =
        "CREATE TABLE IF NOT EXISTS `OfflineItem` (" +
            "`resourceId` TEXT NOT NULL, " +
            "`markedAt` INTEGER NOT NULL, " +
            "PRIMARY KEY(`resourceId`), " +
            "FOREIGN KEY(`resourceId`) REFERENCES `Resource`(`resourceId`) ON UPDATE NO ACTION ON DELETE CASCADE )"
    private const val CREATE_OFFLINE_SECRET =
        "CREATE TABLE IF NOT EXISTS `OfflineSecret` (" +
            "`resourceId` TEXT NOT NULL, " +
            "`encryptedSecret` TEXT NOT NULL, " +
            "`resourceModified` INTEGER NOT NULL, " +
            "`cachedAt` INTEGER NOT NULL, " +
            "PRIMARY KEY(`resourceId`), " +
            "FOREIGN KEY(`resourceId`) REFERENCES `Resource`(`resourceId`) ON UPDATE NO ACTION ON DELETE CASCADE )"

    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(CREATE_OFFLINE_ITEM)
        connection.execSQL(CREATE_OFFLINE_SECRET)
    }
}
