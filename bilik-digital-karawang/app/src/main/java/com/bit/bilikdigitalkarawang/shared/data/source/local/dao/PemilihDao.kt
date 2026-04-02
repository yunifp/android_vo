package com.bit.bilikdigitalkarawang.shared.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.PemilihEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PemilihDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPemilih(pemilihList: List<PemilihEntity>)

    @Query("SELECT * FROM pemilih WHERE nik = :nik LIMIT 1")
    suspend fun getPemilihByNik(nik: String): PemilihEntity?

    @Query("SELECT * FROM pemilih")
    fun getAllPemilih(): Flow<List<PemilihEntity>>

    @Query("SELECT COUNT(*) FROM pemilih")
    fun countAllPemilih(): Flow<Int>

    @Query("DELETE FROM pemilih")
    suspend fun clearAll()
}