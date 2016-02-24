/* Copyright 2015 Richard Wiedenhöft <richard@wiedenhoeft.xyz>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.metanoise.android.jenastop

import android.content.Context
import android.database.Cursor
import android.database.sqlite.{ SQLiteDatabase, SQLiteOpenHelper }
import android.util.Log

import scala.annotation.tailrec
import scala.concurrent.{ ExecutionContext, Future }

class DatabaseHelper(context: Context) extends SQLiteOpenHelper(context, DatabaseHelper.DATABASE_NAME, null, DatabaseHelper.DATABASE_VERSION) {
  def onCreate(db: SQLiteDatabase) = {
    db.execSQL("CREATE TABLE `stations` (`name` TEXT PRIMARY KEY, `favorite` INT, `gpsX` REAL, `gpsY` REAL)")
    db.execSQL("CREATE TABLE `flags` (`name` TEXT PRIMARY KEY, `value` INT)")
    db.execSQL("INSERT INTO `flags` (`name`, `value`) VALUES ('needStationsUpdate', '1')")
  }

  @tailrec
  private def cursorToSet(cursor: Cursor, set: Set[String] = Set()): Set[String] = {
    if (cursor.isAfterLast) {
      cursor.close()
      set
    } else {
      val elem = cursor.getString(0)
      cursor.moveToNext
      cursorToSet(cursor, set + elem)
    }
  }

  def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = {
    if (oldVersion < 2 && newVersion >= 2) {
      db.beginTransaction()
      try {
        val cursor = db.rawQuery("SELECT * FROM `favs`", Array())
        cursor.moveToFirst
        val favs = cursorToSet(cursor)
        db.execSQL("DROP TABLE `favs`")
        db.execSQL("CREATE TABLE `stations` (`name` TEXT PRIMARY KEY, `favorite` INT)")
        for (fav <- favs) {
          db.execSQL("INSERT INTO `stations` (`name`, `favorite`) VALUES (?, '1')", Array(fav))
        }
        db.setTransactionSuccessful()
      } catch {
        case t: Throwable ⇒ Log.w("Jenastop", t)
      } finally {
        db.endTransaction()
      }
    }

    if (oldVersion < 3 && newVersion >= 3) {
      // A lot of stations changed. This will trigger a refresh.
      db.execSQL("DELETE FROM `stations` WHERE `favorite` = '0'")
    }

    if (oldVersion < 4 && newVersion >= 4) {
      // Add field for stopnos
      db.execSQL("ALTER TABLE `stations` ADD COLUMN `stoppoints` TEXT")
      db.execSQL("CREATE TABLE `flags` (`name` TEXT PRIMARY KEY, `value` INT)")
      db.execSQL("INSERT INTO `flags` (`name`, `value`) VALUES ('needStationsUpdate', '1')")
    }

    if (oldVersion < 5 && newVersion >= 5) {
      // Remove stopnos
      db.beginTransaction()
      try {
        db.execSQL("ALTER TABLE `stations` RENAME TO `tmp`")
        db.execSQL("CREATE TABLE `stations` (`name` TEXT PRIMARY KEY, `favorite` INT)")
        val cursor = db.rawQuery("SELECT `name`, `favorite` FROM `tmp`", Array())
        cursor.moveToFirst()
        @tailrec
        def helper(): Unit = {
          if (cursor.isAfterLast) cursor.close()
          else {
            val name = cursor.getString(0)
            val fav = cursor.getInt(1).toString
            db.execSQL("INSERT INTO `stations` (`name`, `favorite`) VALUES (?, ?)", Array(name, fav))
            cursor.moveToNext()
            helper()
          }
        }
        helper()
        db.execSQL("DROP TABLE `tmp`")
        db.setTransactionSuccessful()
      } catch {
        case t: Throwable ⇒ Log.e("Jenastop", "Database upgrade failed", t)
      } finally {
        db.endTransaction()
      }
    }

    if (oldVersion < 6 && newVersion >= 6) {
      db.beginTransaction()
      try {
        db.execSQL("ALTER TABLE `stations` ADD COLUMN `gpsX` REAL")
        db.execSQL("ALTER TABLE `stations` ADD COLUMN `gpsY` REAL")
        db.execSQL("UPDATE `flags` SET `value` = 1 WHERE `name` = 'needStationsUpdate'")
        db.setTransactionSuccessful()
      } finally {
        db.endTransaction()
      }
    }
  }

  def flag(name: String): Boolean = {
    val db = this.getReadableDatabase
    val cursor = db.rawQuery("SELECT `value` FROM `flags` WHERE `name` = ?", Array(name))
    cursor.moveToFirst()
    val result = cursor.getInt(0) != 0
    cursor.close()
    result
  }

  def flag(name: String, value: Boolean): Unit = {
    val db = this.getWritableDatabase
    db.execSQL("UPDATE `flags` SET `value` = ? WHERE `name` = ?", Array(if (value) "1" else "0", name))
  }

  def stations: Set[Station] = {
    val db = this.getReadableDatabase
    val cursor = db.rawQuery("SELECT * FROM `stations`", Array())
    @tailrec
    def helper(cursor: Cursor, set: Set[Station] = Set()): Set[Station] = {
      if (cursor.isAfterLast) {
        cursor.close()
        set
      } else {
        val name = cursor.getString(0)
        val favorite = cursor.getInt(1) != 0
        val gpsX = cursor.getDouble(2)
        val gpsY = cursor.getDouble(3)
        cursor.moveToNext()
        helper(cursor, set + Station(name, favorite, gpsX, gpsY))
      }
    }
    cursor.moveToFirst()
    helper(cursor)
  }

  def updateStations()(implicit ec: ExecutionContext): Future[Unit] = {
    val favs: Set[String] = this.stations filter { _.favorite } map { _.name }
    Station.fetchStations map { stations ⇒
      val db = this.getWritableDatabase
      db.beginTransaction()
      try {
        db.execSQL("DELETE FROM `stations`")
        for ((name, gpsX, gpsY) <- stations) {
          db.execSQL("INSERT INTO `stations` (`name`, `favorite`, `gpsX`, `gpsY`) VALUES (?, ?, ?, ?)", Array(
            name,
            if (favs.contains(name)) "1" else "0",
            gpsX,
            gpsY
          ))
        }
        db.setTransactionSuccessful()
      } catch {
        case t: Throwable ⇒ Log.w("Jenastop", t)
      } finally {
        db.endTransaction()
      }
    }
  }

  def setFavorite(station: Station, favorite: Boolean) = {
    val db = this.getWritableDatabase
    db.beginTransaction()
    try {
      db.execSQL("UPDATE `stations` SET `favorite` = ? WHERE `name` = ?", Array(if (favorite) "1" else "0", station.name))
      db.setTransactionSuccessful()
    } catch {
      case e: Exception =>
        Log.w("Jenastop", e)
    } finally {
      db.endTransaction()
    }
  }
}

object DatabaseHelper {
  private val DATABASE_VERSION: Int = 6
  private val DATABASE_NAME: String = "jenastop_storage"
}
