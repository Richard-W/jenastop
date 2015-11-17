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

import android.app.Activity

import scala.concurrent.{ Promise, Future, ExecutionContext }

class RichFuture[T](future: Future[T]) {
  def mapUI[U](func: (T) ⇒ U)(implicit activity: Activity, ec: ExecutionContext): Future[U] = {
    val promise = Promise[U]
    future map { t ⇒
      activity.runOnUiThread(new Runnable {
        override def run(): Unit = {
          try {
            promise.success(func(t))
          } catch {
            case t: Throwable ⇒ promise.failure(t)
          }
        }
      })
    }
    promise.future
  }

  def recoverUI[U](func: PartialFunction[Throwable, U])(implicit activity: Activity, ec: ExecutionContext): Future[U] = {
    val promise = Promise[U]
    future recover {
      case t: Throwable ⇒ activity.runOnUiThread(new Runnable {
        override def run(): Unit = {
          try {
            promise.success(func(t))
          } catch {
            case t: Throwable ⇒ promise.failure(t)
          }
        }
      })
    }
    promise.future
  }
}
