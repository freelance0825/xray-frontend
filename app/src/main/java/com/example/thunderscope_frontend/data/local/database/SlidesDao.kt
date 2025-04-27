package com.example.thunderscope_frontend.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.thunderscope_frontend.data.models.SlidesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SlidesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlides(slides: List<SlidesEntity>)

    @Query("SELECT * FROM slides")
    fun getAllSlides(): Flow<List<SlidesEntity>>

    @Query("SELECT * FROM slides WHERE isCurrentlySelected = 1")
    fun getSelectedSlides(): Flow<List<SlidesEntity>>

    @Query("DELETE FROM slides")
    suspend fun clearAllSlides()
}
