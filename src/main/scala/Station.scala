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

import java.net.URL

import android.app.Activity

import scala.concurrent.{ ExecutionContext, Future }
import scala.xml.XML

case class Station(
    name: String,
    favorite: Boolean) extends Ordered[Station] {

  def setFavorite(favorite: Boolean)(implicit db: DatabaseHelper): Station = {
    db.setFavorite(this, favorite)
    this.copy(favorite = favorite)
  }

  def compare(other: Station): Int = {
    if (this.favorite && !other.favorite) {
      -1
    } else if (!this.favorite && other.favorite) {
      1
    } else {
      name.compare(other.name)
    }
  }
}

object Station {
  def fetch(favorites: Set[String])(implicit ec: ExecutionContext, activity: Activity): Future[Seq[Station]] = {
    val favorites = new DatabaseHelper(activity).favorites
    Future {
      val url = new URL("http://www.jenah.de/mapper.php?action=getStStartBy")
      val stationsXml = XML.load(new java.io.InputStreamReader(url.openConnection.getInputStream, "UTF-8"))
      val stationNames = stationsXml \\ "name" map {
        _.text
      }
      stationNames map { name ⇒ Station(name = name, favorite = favorites.contains(name)) }
    }
  }
}
