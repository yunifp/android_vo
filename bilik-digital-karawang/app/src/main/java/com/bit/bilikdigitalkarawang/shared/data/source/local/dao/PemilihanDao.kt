package com.bit.bilikdigitalkarawang.shared.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.PemilihanEntity
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.SuaraSahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PemilihanDao {

    @Query("SELECT * FROM pemilihan WHERE nik = :nik LIMIT 1")
    suspend fun getPemilihanByNik(nik: String): PemilihanEntity?

    // Insert satu entri
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
            // Update dengan id yang sama agar tidak create new row
            updatePemilihan(pemilihan.copy(id = existing.id))
        } else {
            insertPemilihan(pemilihan)
        }
    }

    // Insert banyak entri sekaligus (jika dibutuhkan)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPemilihan(pemilihanList: List<PemilihanEntity>)

    // Ambil semua data pemilihan (opsional)
    @Query("SELECT * FROM pemilihan")
    fun getAllPemilihan(): Flow<List<PemilihanEntity>>

    // Hitung jumlah suara sah per kandidat
    @Query("""
        SELECT 
            k.no_urut AS noUrut,
            k.nama_calon AS nama,
            k.local_foto AS localFoto,
            COUNT(p.noUrut) AS jumlah,
            SUM(CASE WHEN p.jenisKelamin = 'L' THEN 1 ELSE 0 END) AS jumlahLaki,
            SUM(CASE WHEN p.jenisKelamin = 'P' THEN 1 ELSE 0 END) AS jumlahPerempuan
        FROM kandidat AS k
        LEFT JOIN pemilihan AS p ON p.noUrut = k.no_urut AND p.idStatus = 1
        GROUP BY k.no_urut
        ORDER BY COUNT(p.noUrut) DESC, k.no_urut ASC
    """)
    fun getJumlahSuaraSahPerKandidat(): Flow<List<SuaraSahEntity>>


    // Hitung jumlah suara sah
    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 1")
    fun getJumlahSah(): Flow<Int>

    // Hitung jumlah suara sah laki-laki
    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 1 AND jenisKelamin = 'L'")
    fun getJumlahSahLaki(): Flow<Int>

    // Hitung jumlah suara sah perempuan
    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 1 AND jenisKelamin = 'P'")
    fun getJumlahSahPerempuan(): Flow<Int>

    // Hitung jumlah suara tidak sah
    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 2")
    fun getJumlahTidakSah(): Flow<Int>

    // Hitung jumlah suara tidak sah laki-laki
    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 2 AND jenisKelamin = 'L'")
    fun getJumlahTidakSahLaki(): Flow<Int>

    // Hitung jumlah suara tidak sah perempuan
    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 2 AND jenisKelamin = 'P'")
    fun getJumlahTidakSahPerempuan(): Flow<Int>

    // Hitung jumlah abstain
    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 3")
    fun getJumlahAbstain(): Flow<Int>

    // Hitung jumlah abstain laki-laki
    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 3 AND jenisKelamin = 'L'")
    fun getJumlahAbstainLaki(): Flow<Int>

    // Hitung jumlah abstain perempuan
    @Query("SELECT COUNT(*) FROM pemilihan WHERE idStatus = 3 AND jenisKelamin = 'P'")
    fun getJumlahAbstainPerempuan(): Flow<Int>

    // Cari siapa saja yang memilih kandidat tertentu
    @Query("SELECT * FROM pemilihan WHERE noUrut = :noUrut AND idStatus = 1")
    fun getPemilihByKandidat(noUrut: Int): Flow<List<PemilihanEntity>>

    // Hapus semua data pemilihan (opsional)
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
