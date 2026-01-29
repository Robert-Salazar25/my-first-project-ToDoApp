package com.example.todoapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.todoapp.data.local.entity.TaskEntity

@Database(entities = [TaskEntity::class], version = 3, exportSchema = false)
abstract class TaskDatabase: RoomDatabase() {

    abstract fun TaskDao(): TaskDao

    companion object {
        // Migración de 1 a 2 (original para notas)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tasks ADD COLUMN notas TEXT DEFAULT ''")
            }
        }

        // Nueva migración de 2 a 3 (para los nuevos campos y eliminar fecha)
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear nueva tabla temporal sin el campo fecha
                database.execSQL("""
                    CREATE TABLE tasks_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tarea TEXT NOT NULL,
                        hora TEXT NOT NULL,
                        notas TEXT DEFAULT '',
                        diasSemana TEXT DEFAULT '',
                        esRecurrente INTEGER DEFAULT 0
                    )
                """.trimIndent())

                // Copiar datos de la tabla vieja a la nueva (excluyendo fecha)
                database.execSQL("""
                    INSERT INTO tasks_new (id, tarea, hora, notas)
                    SELECT id, tarea, hora, notas FROM tasks
                """.trimIndent())

                // Eliminar tabla vieja
                database.execSQL("DROP TABLE tasks")

                // Renombrar nueva tabla
                database.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
            }
        }

        fun getDatabase(context: Context): TaskDatabase {
            return Room.databaseBuilder(
                context,
                TaskDatabase::class.java,
                "task_database"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
        }
    }
}