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

import org.jsoup._

import scala.concurrent.{ ExecutionContext, Future }
import scala.collection.JavaConversions._

case class Schedule(
  line: String,
  destination: String,
  time: String)

object Schedule {
  def fetch(stationName: String)(implicit ec: ExecutionContext): Future[Seq[Schedule]] = Future {
    val html = Jsoup.connect("http://www.nahverkehr-jena.de/fahrplan/haltestellenmonitor.html")
      .data("tx_akteasygojenah_stopsmonitor[__referrer][@extension]", "AktEasygoJenah")
      .data("tx_akteasygojenah_stopsmonitor[__referrer][@vendor]", "AKT")
      .data("tx_akteasygojenah_stopsmonitor[__referrer][@controller]", "Stopsmonitor")
      .data("tx_akteasygojenah_stopsmonitor[__referrer][@action]", "form")
      .data("tx_akteasygojenah_stopsmonitor[__referrer][arguments]", "YTowOnt99edb37317ce2146cbb36d8cfdd8a13d5bdaa1401")
      .data("tx_akteasygojenah_stopsmonitor[__trustedProperties]", "a:2:{s:8:\"stopName\";i:1;s:12:\"selectedStop\";i:1;}4e8bc1e643111a77d8aa363a030a699651803489")
      .data("tx_akteasygojenah_stopsmonitor[selectedStop]", "")
      .data("tx_akteasygojenah_stopsmonitor[stopName]", stationName)
      .timeout(10000)
      .post()

    html.getElementById("monitoringResult").select("tbody tr").toList map { element ⇒
      val cols = element.select("td").toList
      val line = cols(0).child(0).html
      val destination = cols(1).html
      val time = cols(2).html.split("<br>")(0)
      Schedule(line, destination, time)
    }
  }
}
