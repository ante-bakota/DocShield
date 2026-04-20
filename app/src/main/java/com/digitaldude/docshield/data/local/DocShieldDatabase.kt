package com.digitaldude.docshield.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.sqlcipher.database.SupportFactory

@TypeConverters(Converters::class)
@Database(entities = [DocumentEntity::class], version = 2, exportSchema = false)
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
                .fallbackToDestructiveMigration(false)
                .build()
        }
    }
}