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

import android.app.Activity
import android.content.Intent
import android.view.View.OnClickListener
import android.view.{ View, ViewGroup }
import android.widget.{ TextView, ArrayAdapter }

import scala.collection.JavaConversions._

class NavigationAdapter private (private val activity: Activity, private val list: java.util.List[String]) extends ArrayAdapter[String](activity, R.layout.listitem_navigation, list) {

  def this(activity: Activity) = {
    this(activity, new java.util.ArrayList[String])
    list.add(activity.getResources.getString(R.string.title_activity_settings))
  }

  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val view =
      if (convertView == null) activity.getLayoutInflater.inflate(R.layout.listitem_navigation, parent, false)
      else convertView
    view.findViewById(R.id.setting_name).asInstanceOf[TextView].setText(list(position))
    view.setOnClickListener(new OnClickListener {
      override def onClick(v: View): Unit = {
        if (list(position) == activity.getResources.getString(R.string.title_activity_settings)) {
          val intent = new Intent(activity, classOf[SettingsActivity])
          activity.startActivity(intent)
        }
      }
    })
    view
  }
}
