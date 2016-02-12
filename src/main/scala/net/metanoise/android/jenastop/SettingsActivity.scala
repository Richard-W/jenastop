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

import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.View
import net.metanoise.android.jenastop.ui.{ HomeButton, ScalaActivity }

class SettingsFragment extends PreferenceFragment {

  override def onCreate(bundle: Bundle): Unit = {
    super.onCreate(bundle)
    addPreferencesFromResource(R.xml.preferences)
  }
}

class SettingsActivity extends ScalaActivity with HomeButton {

  override protected def contentView: View = getLayoutInflater.inflate(R.layout.activity_settings, null)

  override def onCreate(bundle: Bundle): Unit = {
    super.onCreate(bundle)

    getFragmentManager.beginTransaction()
      .replace(android.R.id.content, new SettingsFragment)
      .commit()
  }
}
