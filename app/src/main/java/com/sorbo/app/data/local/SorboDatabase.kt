// Capa de datos · base de datos de Room: declara las tablas, los conversores y entrega el DAO.
package com.sorbo.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * La base local del diario. Se crea una sola vez desde `AppContainer`.
 * Si más adelante cambian las tablas, hay que subir [version] y escribir una migración.
 */
@Database(entities = [VinoEntidad::class, DegustacionEntidad::class], version = 1, exportSchema = false)
@TypeConverters(Conversores::class)
abstract class SorboDatabase : RoomDatabase() {

    abstract fun vinoDao(): VinoDao

    companion object {
        private const val NOMBRE_ARCHIVO = "sorbo.db"

        fun crear(context: Context): SorboDatabase =
            Room.databaseBuilder(context.applicationContext, SorboDatabase::class.java, NOMBRE_ARCHIVO).build()
    }
}
