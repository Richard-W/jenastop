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

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.language.implicitConversions

package object jenastop {
  implicit def futureToRichFuture[T](future: Future[T]): RichFuture[T] = new RichFuture(future)

  @tailrec
  def unique(list: Seq[String], uniqueList: Seq[String] = Seq()): Seq[String] = {
    if (list.isEmpty) uniqueList
    else {
      if (uniqueList contains list.head) unique(list.tail, uniqueList)
      else unique(list.tail, uniqueList :+ list.head)
    }
  }
}
