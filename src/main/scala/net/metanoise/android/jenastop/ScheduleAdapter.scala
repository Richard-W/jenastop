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
    view.findViewById(R.id.listitem_schedule_line).asInstanceOf[TextView].setText(schedule.lineName)
    view.findViewById(R.id.listitem_schedule_destination).asInstanceOf[TextView].setText(
      if (schedule.destination.length > 18) schedule.destination.substring(0, 18) + "…" else schedule.destination
    )
    view.findViewById(R.id.listitem_schedule_time).asInstanceOf[TextView].setText(schedule.time)

    view
  }
}
