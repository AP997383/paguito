package com.nexusystem.paguito.data

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun abonosDao(): AbonosDao
    abstract fun deudoresDao(): DeudoresDao
    abstract fun productosDao(): ProductosDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Conserva registros históricos; sin propietario comprobable quedan fuera de las consultas de cuentas. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ProductosTble ADD COLUMN ownerAccount TEXT NOT NULL DEFAULT ''")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ProductosTble_ownerAccount ON ProductosTble(ownerAccount)")
            }
        }

        /** Conserva clientes e historial y deja sin asignar los registros históricos aún no verificados. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ProductosTble ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE PagosTable ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE DeudoresTable ADD COLUMN ownerAccount TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE DeudoresTable ADD COLUMN creditBalance REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE PagosTable ADD COLUMN ownerAccount TEXT NOT NULL DEFAULT ''")
                db.execSQL("CREATE TABLE PagosTable_v3 (id INTEGER PRIMARY KEY AUTOINCREMENT, idRemoteDatabase TEXT NOT NULL, idDeudorRemoteDatabase TEXT NOT NULL, idDeudor TEXT NOT NULL, montoAbonado REAL NOT NULL, saldoAntesDeAbono REAL NOT NULL, fechaAbono TEXT NOT NULL, pagoATiempo INTEGER NOT NULL, tipoPago INTEGER NOT NULL, isIngreso INTEGER NOT NULL, notas TEXT NOT NULL, jsonAbonoPorProducto TEXT NOT NULL, ownerAccount TEXT NOT NULL DEFAULT '', isSynced INTEGER NOT NULL DEFAULT 0)")
                db.execSQL("INSERT INTO PagosTable_v3 SELECT id, idRemoteDatabase, idDeudorRemoteDatabase, idDeudor, CAST(montoAbonado AS REAL), CAST(saldoAntesDeAbono AS REAL), fechaAbono, pagoATiempo, tipoPago, isIngreso, notas, jsonAbonoPorProducto, ownerAccount, isSynced FROM PagosTable")
                db.execSQL("DROP TABLE PagosTable")
                db.execSQL("ALTER TABLE PagosTable_v3 RENAME TO PagosTable")
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "paguito_db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
                    .also { INSTANCE = it }
            }
    }
}

