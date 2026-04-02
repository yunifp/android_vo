package com.bit.bilikdigitalkarawang.shared.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bit.bilikdigitalkarawang.shared.data.source.local.dao.KandidatDao
import com.bit.bilikdigitalkarawang.shared.data.source.local.dao.PemilihDao
import com.bit.bilikdigitalkarawang.shared.data.source.local.dao.PemilihanDao
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.KandidatEntity
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.PemilihEntity
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.PemilihanEntity

@Database(
    entities = [
        KandidatEntity::class,
        PemilihEntity::class,
        PemilihanEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class LocalDbService: RoomDatabase() {
    abstract fun kandidatDao(): KandidatDao
    abstract fun pemilihDao(): PemilihDao
    abstract fun pemilihanDao(): PemilihanDao
}