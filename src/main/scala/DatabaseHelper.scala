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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.package net.metanoise.android.jenastop
 */
package net.metanoise.android.jenastop

import android.content.Context
import android.database.Cursor
import android.database.sqlite.{ SQLiteDatabase, SQLiteOpenHelper }
import android.util.Log

import scala.annotation.tailrec

class DatabaseHelper(context: Context) extends SQLiteOpenHelper(context, DatabaseHelper.DATABASE_NAME, null, DatabaseHelper.DATABASE_VERSION) {
  def onCreate(db: SQLiteDatabase) = {
    db.execSQL("CREATE TABLE favs ( " + "station TEXT PRIMARY KEY )")
  }

  def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = {
  }

  def favorites: Set[String] = {
    val cursor = getReadableDatabase.rawQuery("SELECT * FROM `favs`", null)
    @tailrec
    def helper(set: Set[String]): Set[String] = {
      if (cursor.isAfterLast) set
      else {
        val elem = cursor.getString(0)
        cursor.moveToNext
        helper(set + elem)
      }
    }
    cursor.moveToFirst()
    helper(Set())
  }

  def setFavorite(station: Station, favorite: Boolean) = {
    val db: SQLiteDatabase = this.getWritableDatabase
    db.beginTransaction()
    try {
      val cursor: Cursor = db.rawQuery("SELECT * FROM `favs` WHERE `station` = ?", Array(station.name))
      if (cursor.getCount == 0 && favorite) {
        db.execSQL("INSERT INTO `favs` (`station`) VALUES ( ? )", Array(station.name))
      } else if (!favorite) {
        db.execSQL("DELETE FROM `favs` WHERE `station` = ?", Array(station.name))
      }
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
  private val DATABASE_VERSION: Int = 1
  private val DATABASE_NAME: String = "jenastop_storage"
}
