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

import java.util.Collections

import android.app.Activity
import android.content.Intent
import android.view.View.OnClickListener
import android.view.{ View, ViewGroup }
import android.widget.{ ArrayAdapter, ImageView, SectionIndexer, TextView }

import scala.collection.JavaConversions._

class StationsAdapter(activity: Activity, val list: java.util.List[Station]) extends ArrayAdapter[Station](activity, R.layout.listitem_station, list) with SectionIndexer {

  implicit val context = activity

  override def getView(position: Int, convertView: View, parent: ViewGroup) = {
    val station = this.getItem(position)
    val view = if (convertView == null) {
      activity.getLayoutInflater.inflate(R.layout.listitem_station, parent, false)
    } else {
      convertView
    }

    view.findViewById(R.id.listitem_station_name).asInstanceOf[TextView].setText(station.name)
    val favStar = view.findViewById(R.id.listitem_station_fav).asInstanceOf[ImageView]
    favStar.setImageResource(
      if (station.favorite) R.drawable.ic_star_blue_grey_900_24dp else R.drawable.ic_star_border_blue_grey_900_24dp)
    favStar.setOnClickListener(new OnClickListener {
      override def onClick(v: View): Unit = withDatabase { implicit db ⇒
        while (StationsAdapter.this.list.contains(station)) {
          StationsAdapter.this.list.remove(station)
        }
        StationsAdapter.this.list.add(station.setFavorite(!station.favorite))
        StationsAdapter.this.notifyDataSetChanged()
      }
    })

    view.setOnClickListener(new OnClickListener {
      override def onClick(v: View): Unit = {
        val scheduleActivityIntent = new Intent(activity, classOf[ScheduleActivity])
        scheduleActivityIntent.putExtra("station", station.name)
        activity.startActivity(scheduleActivityIntent)
      }
    })

    view
  }

  override def notifyDataSetChanged(): Unit = {
    // Remove duplicate stations
    val intermediate = list.distinct
    list.clear()
    list.addAll(intermediate)

    // Sort stations by name
    Collections.sort(list)

    val favorites = list filter { _.favorite }

    val favoriteCharacter = new String(Character.toChars(0x2605))

    // Create list of all sections
    val alphaSections = (list map { _.name.charAt(0).toUpper.toString.asInstanceOf[AnyRef] }).distinct.toArray
    sections = if (favorites.nonEmpty) favoriteCharacter +: alphaSections else alphaSections

    // Calculate the position of the first element of every section
    positionForSection = sections map { section ⇒
      if (section == favoriteCharacter) 0
      else list.indexOf(list.filter { _.name.charAt(0).toString == section }.head) + favorites.length
    }

    // Calculate the section for every given position
    sectionForPosition = Seq.fill(favorites.length) { 0 } ++ (list map { item ⇒
      sections.indexOf(sections.filter { _.asInstanceOf[String] == item.name.charAt(0).toUpper.toString }.head)
    })

    list.prependAll(favorites)
    super.notifyDataSetChanged()
  }

  var sections: Array[AnyRef] = Array()
  var positionForSection: Seq[Int] = Seq()
  var sectionForPosition: Seq[Int] = Seq()

  override def getPositionForSection(sectionIndex: Int): Int = {
    positionForSection(sectionIndex)
  }

  override def getSections: Array[AnyRef] = {
    sections
  }

  override def getSectionForPosition(position: Int): Int = {
    sectionForPosition(position)
  }
}
