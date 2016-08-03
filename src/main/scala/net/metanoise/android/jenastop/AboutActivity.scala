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
import android.text.{ Html, Spanned }
import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.github.ghik.silencer.silent
import net.metanoise.android.jenastop.ui.{ HomeButton, ScalaActivity }

class AboutActivity extends ScalaActivity with HomeButton {

  def contentView = getLayoutInflater.inflate(R.layout.activity_about, null)

  lazy val textView = findViewById(R.id.textView).asInstanceOf[TextView]

  override def onCreate(bundle: Bundle): Unit = {
    super.onCreate(bundle)
    val html = Html.fromHtml(getResources.getString(R.string.about_text)): @silent // Deprecated but needed for compat
    textView.setText(html)
    textView.setMovementMethod(LinkMovementMethod.getInstance())
  }
}
