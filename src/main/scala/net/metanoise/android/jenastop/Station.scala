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

import java.net.{ HttpURLConnection, URL }

import spray.json._

import scala.concurrent.{ ExecutionContext, Future }
import scala.io.Source

case class Station(
    name: String,
    favorite: Boolean) extends Ordered[Station] {

  def setFavorite(favorite: Boolean)(implicit db: DatabaseHelper): Station = {
    db.setFavorite(this, favorite)
    this.copy(favorite = favorite)
  }

  def compare(other: Station): Int = {
    name.compare(other.name)
  }
}

object Station {
  def fetchStations()(implicit ec: ExecutionContext): Future[Seq[String]] = Future {
    val url = new URL("http://www.nahverkehr-jena.de/index.php?eID=ajaxDispatcher&request[pluginName]=Stopsmonitor&request[controller]=Stopsmonitor&request[action]=getAllStops")
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    val json = Source.fromInputStream(conn.getInputStream).mkString.parseJson

    (json.asInstanceOf[JsArray].elements map {
      _.asInstanceOf[JsObject]
        .fields("children").asInstanceOf[JsObject]
        .fields("name").asInstanceOf[JsArray]
        .elements(0).asInstanceOf[JsObject]
        .fields("value").asInstanceOf[JsString]
        .value
    } groupBy { a ⇒ a }).toSeq map { case (a, b) ⇒ a }
  }
}
