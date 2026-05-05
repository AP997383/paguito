package com.nexusystem.paguito.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nexusystem.paguito.data.local.dao.ProductosDao
import com.nexusystem.paguito.data.local.dao.AbonosDao
import com.nexusystem.paguito.data.local.dao.DeudoresDao
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexus.medi.data.local.entity.PagosEntinty
import kotlin.also
import kotlin.jvm.java

@Database(
    entities = [ DeudoresEntity::class, PorductosEntity::class, PagosEntinty::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun abonosDao(): AbonosDao
    abstract fun deudoresDao(): DeudoresDao
    abstract fun productosDao(): ProductosDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "paguito_db"
                ).build()
                    .also { INSTANCE = it }
            }
    }
}

