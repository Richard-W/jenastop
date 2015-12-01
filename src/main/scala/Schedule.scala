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

import java.net.{ HttpURLConnection, URL, URLEncoder }
import java.util.{ Locale, GregorianCalendar }

import scala.concurrent.{ ExecutionContext, Future }
import scala.io.Source
import scala.xml.XML

import spray.json._

case class Schedule(
    line: String,
    destination: String,
    plannedArrival: GregorianCalendar,
    actualArrival: GregorianCalendar) extends Ordered[Schedule] {
  override def compare(other: Schedule): Int = {
    plannedArrival.compareTo(other.plannedArrival)
  }
}

object Schedule {
  def fetch(station: String)(implicit ec: ExecutionContext): Future[Seq[Schedule]] = Future {

    /*
        val encodedStation = URLEncoder.encode(station.toLowerCase(Locale.GERMAN), "UTF-8")
    val url = new URL("http://fpl.jenah.de/bontip-ifgi/php/getStation.php?action=getMastNo&q=" + encodedStation)
    val xml = XML.load(new InputStreamReader(url.openConnection.getInputStream, "ISO-8859-1"))
    xml \\ "stopno" map {
      _.text
    */
    val url = new URL("http://www.nahverkehr-jena.de/index.php?eID=ajaxDispatcher&request[pluginName]=Stopsmonitor&request[controller]=Stopsmonitor&request[action]=getAllStops")
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    val json = Source.fromInputStream(conn.getInputStream).mkString.parseJson

    val stopnos: Seq[String] = json.asInstanceOf[JsArray].elements map { entry ⇒
      entry.asInstanceOf[JsObject]
        .fields("children").asInstanceOf[JsObject]
    } filter { entry ⇒
      val name = entry
        .fields("name").asInstanceOf[JsArray]
        .elements(0).asInstanceOf[JsObject]
        .fields("value").asInstanceOf[JsString]
        .value
      name == station
    } flatMap { entry ⇒
      entry
        .fields("stopPoints").asInstanceOf[JsArray]
        .elements(0).asInstanceOf[JsObject]
        .fields("children").asInstanceOf[JsObject]
        .fields("stopPoint").asInstanceOf[JsArray]
        .elements map { stopPoint ⇒
          stopPoint.asInstanceOf[JsObject]
            .fields("children").asInstanceOf[JsObject]
            .fields("pointNo").asInstanceOf[JsArray]
            .elements(0).asInstanceOf[JsObject]
            .fields("value").asInstanceOf[JsString]
            .value
        }
    }

    unique(stopnos) flatMap { stopno ⇒
      val url = new URL("http://fpl.jenah.de/bontip-ifgi/php/proxy.php?vsz=60&azbid=" + stopno)
      val scheduleXml = XML.load(new java.io.InputStreamReader(url.openConnection.getInputStream, "ISO-8859-1"))
      scheduleXml \\ "AZBFahrplanlage" map { stop ⇒
        def parseTime(str: String): GregorianCalendar = {
          val fields = str.split("[\\-T:]") map {
            _.toInt
          }
          new GregorianCalendar(fields(0), fields(1), fields(2), fields(3), fields(4), fields(5))
        }
        Schedule(
          (stop \\ "LinienText").text,
          (stop \\ "RichtungsText").text,
          parseTime((stop \\ "AnkunftszeitAZBPlan").text),
          parseTime((stop \\ "AnkunftszeitAZBPrognose").text)
        )
      }
    }
  }
}
