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
    db.execSQL("CREATE TABLE `stations` (`name` TEXT PRIMARY KEY, `favorite` INT)");
  }

  @tailrec
  private def cursorToSet(cursor: Cursor, set: Set[String] = Set()): Set[String] = {
    if (cursor.isAfterLast) set
    else {
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
        db.execSQL("CREATE TABLE `stations` (`name` TEXT PRIMARY KEY, `favorite` INT)");
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
  }

  def stations: Set[Station] = {
    val db = this.getReadableDatabase
    val cursor = db.rawQuery("SELECT * FROM `stations`", Array())
    @tailrec
    def helper(cursor: Cursor, set: Set[Station] = Set()): Set[Station] = {
      if (cursor.isAfterLast) set
      else {
        val name = cursor.getString(0)
        val favorite = cursor.getInt(1) != 0
        cursor.moveToNext()
        helper(cursor, set + Station(name, favorite))
      }
    }
    cursor.moveToFirst()
    helper(cursor)
  }

  def updateStations()(implicit ec: ExecutionContext): Future[Unit] = {
    val favs: Set[String] = this.stations filter { _.favorite } map { _.name }
    Station.fetchNames map { names ⇒
      val db = this.getWritableDatabase
      db.beginTransaction()
      try {
        db.execSQL("DELETE FROM `stations`")
        for (name <- names) {
          db.execSQL("INSERT INTO `stations` (`name`, `favorite`) VALUES (?, ?)", Array(name, if (favs.contains(name)) "1" else "0"))
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
  private val DATABASE_VERSION: Int = 3
  private val DATABASE_NAME: String = "jenastop_storage"
}
