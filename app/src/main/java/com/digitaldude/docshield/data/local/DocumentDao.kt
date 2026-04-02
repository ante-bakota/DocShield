package com.digitaldude.docshield.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document : DocumentEntity)

    @Query("SELECT * FROM documents ORDER BY dateadded DESC")
    fun getAllDocuments() : Flow<List<DocumentEntity>>
}