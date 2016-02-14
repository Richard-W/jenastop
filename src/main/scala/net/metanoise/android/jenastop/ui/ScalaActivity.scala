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
package net.metanoise.android.jenastop.ui

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View

trait ScalaActivity extends AppCompatActivity {

  /**
   * Override this to wrap the view returned by contentView into another
   * structure
   *
   * @return The activity layout
   */
  protected def topContentView: View = contentView

  /**
   * Layout for this activity
   *
   * @return The activity layout
   */
  protected def contentView: View

  override protected def onCreate(bundle: Bundle): Unit = {
    super.onCreate(bundle)
    setContentView(topContentView)
    getSupportActionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, net.metanoise.android.jenastop.R.color.themeColorDark)))
  }
}
