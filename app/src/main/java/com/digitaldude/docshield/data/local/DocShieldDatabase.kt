package com.digitaldude.docshield.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import net.sqlcipher.database.SupportFactory

@Database(entities = [DocumentEntity::class], version = 1, exportSchema = false)
abstract class DocShieldDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao

    companion object {
        fun create(context: Context, passphrase: ByteArray): DocShieldDatabase {
            val factory = SupportFactory(passphrase)
            return Room.databaseBuilder(
                context,
                DocShieldDatabase::class.java,
                "docshield.db"
            )
                .openHelperFactory(factory)
                .build()
        }
    }
}