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

import java.util.{ Calendar, Collections, GregorianCalendar }

import android.view.{ View, ViewGroup }
import android.widget.{ ArrayAdapter, TextView }

class ScheduleAdapter(activity: ScheduleActivity, val list: java.util.List[Schedule]) extends ArrayAdapter[Schedule](activity, R.layout.listitem_schedule, list) {
  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val view = if (convertView == null) {
      activity.getLayoutInflater.inflate(R.layout.listitem_schedule, parent, false)
    } else {
      convertView
    }

    val schedule: Schedule = list.get(position)
    view.findViewById(R.id.listitem_schedule_line).asInstanceOf[TextView].setText(schedule.line)
    view.findViewById(R.id.listitem_schedule_destination).asInstanceOf[TextView].setText(
      if (schedule.destination.length > 20) schedule.destination.substring(0, 20) + "…" else schedule.destination
    )

    def cal2str(cal: GregorianCalendar): String = {
      val hour = cal.get(Calendar.HOUR_OF_DAY)
      val minute = cal.get(Calendar.MINUTE)
      val hour_s = if (hour < 10) {
        "0" + hour.toString
      } else {
        hour.toString
      }
      val minute_s = if (minute < 10) {
        "0" + minute.toString
      } else {
        minute.toString
      }
      hour_s + ":" + minute_s
    }
    view.findViewById(R.id.listitem_schedule_actual).asInstanceOf[TextView].setText(cal2str(schedule.actualArrival))
    view.findViewById(R.id.listitem_schedule_planned).asInstanceOf[TextView].setText(cal2str(schedule.plannedArrival))

    view
  }

  override def notifyDataSetChanged(): Unit = {
    Collections.sort(list)
    super.notifyDataSetChanged()
  }
}
