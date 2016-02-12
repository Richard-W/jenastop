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
import android.widget.{ ImageView, TextView, ArrayAdapter }

import scala.collection.JavaConversions._

case class NavigationItem(
  name: Int,
  icon: Int,
  action: () ⇒ Unit)

class NavigationAdapter private (private val activity: Activity, private val list: java.util.List[NavigationItem]) extends ArrayAdapter[NavigationItem](activity, R.layout.listitem_navigation, list) {

  def this(activity: Activity) = {
    this(activity, new java.util.ArrayList[NavigationItem])
    list.add(NavigationItem(
      R.string.title_activity_navigation,
      R.drawable.ic_navigation_white_24dp,
      () ⇒ {}
    ))
    list.add(NavigationItem(
      R.string.title_activity_settings,
      R.drawable.ic_settings_white_24dp,
      () ⇒ { activity.startActivity(new Intent(activity, classOf[SettingsActivity])) }
    ))
  }

  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val view =
      if (convertView == null) activity.getLayoutInflater.inflate(R.layout.listitem_navigation, parent, false)
      else convertView
    val item = list(position)
    view.findViewById(R.id.navitem_name).asInstanceOf[TextView].setText(activity.getResources.getString(item.name))
    view.findViewById(R.id.navitem_icon).asInstanceOf[ImageView].setImageResource(item.icon)
    view.setOnClickListener(new OnClickListener {
      override def onClick(v: View): Unit = {
        item.action()
      }
    })
    view
  }
}
