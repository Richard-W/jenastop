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

import android.os.Bundle
import android.view.MenuItem

trait HomeButton extends ScalaActivity {

  /**
    * Override this with the action that happens when the home button is clicked
    */
  def onHomeButtonClick(): Unit = this.finish()

  override protected def onCreate(bundle: Bundle): Unit = {
    super.onCreate(bundle)
    val actionBar = getSupportActionBar
    actionBar.setHomeButtonEnabled(true)
    actionBar.setDisplayHomeAsUpEnabled(true)
  }

  override protected def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case android.R.id.home ⇒
        this.finish()
        true
      case _ ⇒
        super.onOptionsItemSelected(item)
    }
  }
}
