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

import scala.concurrent.{ Promise, Future, ExecutionContext }
import scala.util.{ Failure, Success }

class RichFuture[T](future: Future[T]) {
  def mapUI[U](func: (T) ⇒ U)(implicit activity: Activity, ec: ExecutionContext): Future[U] = {
    val promise = Promise[U]
    future andThen {
      case Success(value) ⇒
        activity.runOnUiThread(new Runnable {
          override def run(): Unit = {
            try {
              promise.success(func(value))
            } catch {
              case t: Throwable ⇒ promise.failure(t)
            }
          }
        })
      case Failure(fail) ⇒
        promise.failure(fail)
    }
    promise.future
  }

  def recoverUI[U >: T](func: PartialFunction[Throwable, U])(implicit activity: Activity, ec: ExecutionContext): Future[U] = {
    val promise = Promise[U]
    future andThen {
      case Success(value) ⇒
        promise.success(value)
      case Failure(fail) ⇒
        activity.runOnUiThread(new Runnable {
          override def run(): Unit = {
            try {
              promise.success(func(fail))
            } catch {
              case t: Throwable ⇒ promise.failure(t)
            }
          }
        })
    }
    promise.future
  }
}
