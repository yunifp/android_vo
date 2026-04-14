package com.bit.bilikdigitalkarawang.shared.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.PemilihanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PemilihanDao {

    @Query("SELECT * FROM pemilihan WHERE nik = :nik LIMIT 1")
    suspend fun getPemilihanByNik(nik: String): PemilihanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPemilihan(pemilihan: PemilihanEntity)

    @Update
    suspend fun updatePemilihan(pemilihan: PemilihanEntity)

    @Query("SELECT * FROM pemilihan WHERE idDpt = :idDpt LIMIT 1")
    suspend fun getPemilihanByIdDpt(idDpt: String): PemilihanEntity?

    @Transaction
    suspend fun insertOrUpdateByIdDpt(pemilihan: PemilihanEntity) {
        val existing = getPemilihanByIdDpt(pemilihan.idDpt)
        if (existing != null) {
            updatePemilihan(pemilihan.copy(id = existing.id))
        } else {
            insertPemilihan(pemilihan)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPemilihan(pemilihanList: List<PemilihanEntity>)

    @Query("SELECT * FROM pemilihan")
    fun getAllPemilihan(): Flow<List<PemilihanEntity>>

    // HAPUS getJumlahSuaraSahPerKandidat() dari sini karena perhitungannya
    // sekarang dipindah ke UseCase (Homomorphic Addition)

    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 1")
    fun getJumlahSah(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 1 AND jenisKelamin = 'L'")
    fun getJumlahSahLaki(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 1 AND jenisKelamin = 'P'")
    fun getJumlahSahPerempuan(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 2")
    fun getJumlahTidakSah(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 2 AND jenisKelamin = 'L'")
    fun getJumlahTidakSahLaki(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 2 AND jenisKelamin = 'P'")
    fun getJumlahTidakSahPerempuan(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 3")
    fun getJumlahAbstain(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 3 AND jenisKelamin = 'L'")
    fun getJumlahAbstainLaki(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 3 AND jenisKelamin = 'P'")
    fun getJumlahAbstainPerempuan(): Flow<Int>

    // HAPUS getPemilihByKandidat() dari sini karena HE menyembunyikan siapa memilih siapa

    @Query("DELETE FROM pemilihan")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM pemilihan")
    suspend fun getTotalPemilihan(): Int

    @Query("DELETE FROM PEMILIHAN")
    suspend fun resetPemilihan()

    @Query("SELECT COUNT(*) FROM pemilihan WHERE nik = :nik AND hasPrintUlang = 1")
    suspend fun hasPrintUlang(nik: String): Int

    @Query("UPDATE pemilihan SET hasPrintUlang = 1 WHERE nik = :nik")
    suspend fun updateHasPrintUlang(nik: String)

    @Query("SELECT nik FROM pemilihan ORDER BY id DESC LIMIT 1")
    fun getNikLastRowPemilihan(): Flow<String?>
}