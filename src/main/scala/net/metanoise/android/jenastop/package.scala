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
package net.metanoise.android

import android.content.Context

import scala.concurrent.Future
import scala.language.implicitConversions

package object jenastop {
  implicit def futureToRichFuture[T](future: Future[T]): RichFuture[T] = new RichFuture(future)

  def withDatabase[T](f: (DatabaseHelper) ⇒ T)(implicit context: Context): T = {
    val db = new DatabaseHelper(context)
    val result = f(db)
    db.close()
    result
  }
}
