package com.bit.bilikdigitalkarawang.shared.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.KandidatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KandidatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllKandidat(kandidatList: List<KandidatEntity>)

    @Query("SELECT * FROM kandidat WHERE id_kandidat = :idKandidat LIMIT 1")
    suspend fun getKandidatById(idKandidat: String): KandidatEntity?

    @Query("SELECT * FROM kandidat")
    fun getAllKandidat(): Flow<List<KandidatEntity>>

    @Query("DELETE FROM kandidat")
    suspend fun clearAll()
}