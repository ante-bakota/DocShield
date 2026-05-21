package com.digitaldude.docshield.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.sqlcipher.database.SupportFactory

@TypeConverters(Converters::class)
@Database(entities = [DocumentEntity::class, CategoryEntity::class], version = 4, exportSchema = false)
abstract class DocShieldDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun categoryDao(): CategoryDao

    companion object {

        // Adds the categories table — existing documents and data are untouched
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `categories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun create(context: Context, passphrase: ByteArray): DocShieldDatabase {
            val factory = SupportFactory(passphrase)
            return Room.databaseBuilder(
                context,
                DocShieldDatabase::class.java,
                "docshield.db"
            )
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_3_4)
                .build()
        }
    }
}