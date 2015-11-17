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

import java.util.Collections

import android.app.Activity
import android.content.Intent
import android.view.View.OnClickListener
import android.view.{ View, ViewGroup }
import android.widget.{ ArrayAdapter, ImageView, TextView }

/**
 * Created by richard on 08.11.15.
 */
class StationsAdapter(activity: Activity, val list: java.util.List[Station]) extends ArrayAdapter[Station](activity, R.layout.listitem_station, list) {
  implicit val db = new DatabaseHelper(activity)

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
      if (station.favorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
    )
    favStar.setOnClickListener(new OnClickListener {
      override def onClick(v: View): Unit = {
        StationsAdapter.this.list.remove(station)
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
    Collections.sort(list)
    super.notifyDataSetChanged()
  }
}
