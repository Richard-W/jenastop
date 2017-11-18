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

import java.util.{ Timer, TimerTask }

import android.os.{ AsyncTask, Bundle }
import android.preference.PreferenceManager
import android.view.{ Menu, MenuItem, View }
import android.widget.{ Button, ListView, ProgressBar, TextView }
import net.metanoise.android.jenastop.ui.{ HomeButton, ScalaActivity }

import scala.collection.JavaConversions._
import scala.concurrent.{ ExecutionContext, Promise, Future }
import scala.util.Success

class ScheduleActivity extends ScalaActivity with HomeButton {

  lazy val failedText = findViewById(R.id.schedule_failed_text).asInstanceOf[TextView]
  lazy val errorDescription = findViewById(R.id.schedule_error_description).asInstanceOf[TextView]
  lazy val retryButton = findViewById(R.id.schedule_retry_button).asInstanceOf[Button]
  lazy val listView = findViewById(R.id.schedule_list_view).asInstanceOf[ListView]
  lazy val progressBar = findViewById(R.id.schedule_progress_bar).asInstanceOf[ProgressBar]

  lazy val station: String = getIntent.getStringExtra("station")
  lazy val listAdapter: ScheduleAdapter = new ScheduleAdapter(this, new java.util.ArrayList[ScheduleItem])
  var originallyOrdered: Seq[ScheduleItem] = Seq()
  var sorting: String = "time"
  var timer: Timer = null

  implicit val activity = this

  def contentView = getLayoutInflater.inflate(R.layout.activity_schedule, null)

  protected override def onCreate(savedInstanceState: Bundle) {
    // Create UI
    super.onCreate(savedInstanceState)
    getSupportActionBar.setSubtitle(station)

    sorting = PreferenceManager
      .getDefaultSharedPreferences(this)
      .getString("pref_scheduleSorting", "time")

    // Setup ListView
    listView.setAdapter(listAdapter)
    listView.setClickable(false)
  }

  protected override def onResume(): Unit = {
    super.onResume()

    // Setup timer that refreshes the data every 30
    // seconds.
    timer = new Timer
    timer.scheduleAtFixedRate(new TimerTask {
      override def run(): Unit = fetchSchedule()
    }, 0, 30000)
  }

  protected override def onPause(): Unit = {
    super.onPause()

    // Cancel periodic data refresh
    timer.cancel()
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)
    getMenuInflater.inflate(R.menu.menu_schedule, menu)
    true
  }

  override def onSaveInstanceState(bundle: Bundle): Unit = {
    super.onSaveInstanceState(bundle)
    bundle.putString("sorting", sorting)
    bundle.putParcelableArray("schedules", originallyOrdered.toArray)
  }

  override def onRestoreInstanceState(bundle: Bundle): Unit = {
    super.onRestoreInstanceState(bundle)
    sorting = bundle.getString("sorting")
    originallyOrdered = bundle.getParcelableArray("schedules").toSeq map { _.asInstanceOf[ScheduleItem] }
    displayList()
  }

  def displayList(): Unit = {
    val sorted = sorting match {
      case "dest" ⇒ originallyOrdered.sortBy { _.destination }
      case "line" ⇒ originallyOrdered.sortBy { _.line }
      case "time" ⇒ originallyOrdered
    }
    listAdapter.list.clear()
    listAdapter.list.addAll(sorted)
    listAdapter.notifyDataSetChanged()
  }

  def clearList(): Unit = {
    listAdapter.list.clear()
    listAdapter.notifyDataSetChanged()
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.action_refresh =>
        clearList()
        progressBar.setVisibility(View.VISIBLE)
        fetchSchedule()
      case R.id.sort_by_dest ⇒
        sorting = "dest"
        displayList()
      case R.id.sort_by_line ⇒
        sorting = "line"
        displayList()
      case R.id.sort_by_time ⇒
        sorting = "time"
        displayList()
      case _ ⇒
        return super.onOptionsItemSelected(item)
    }
    true
  }

  def fetchSchedule(): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

    val displayProgressBarFuture = {
      val displayProgressBarPromise = Promise[Unit]
      runOnUiThread(new Runnable {
        override def run(): Unit = {
          if (originallyOrdered.isEmpty) {
            // No data available from previous fetches ⇒ display progress bar
            progressBar.setVisibility(View.VISIBLE)
          }
          displayProgressBarPromise.complete(Success(Unit))
        }
      })
      displayProgressBarPromise.future
    }

    val scheduleFuture = ScheduleItem.fetch(station).zip(displayProgressBarFuture) map { case (a, _) ⇒ a }

    scheduleFuture mapUI { schedules ⇒
      originallyOrdered = schedules
      progressBar.setVisibility(View.GONE)
      if (schedules.isEmpty) {
        errorDescription.setVisibility(View.VISIBLE)
        retryButton.setVisibility(View.VISIBLE)
        errorDescription.setText(R.string.no_stops)
      } else {
        displayList()
      }
    } recoverUI {
      case t: Throwable ⇒
        originallyOrdered = Seq()
        progressBar.setVisibility(View.GONE)
        listView.setVisibility(View.GONE)
        failedText.setVisibility(View.VISIBLE)
        retryButton.setVisibility(View.VISIBLE)
        errorDescription.setVisibility(View.VISIBLE)
        errorDescription.setText(t.getMessage)
    }
  }

  def onRetryButtonClick(view: View) {
    progressBar.setVisibility(View.VISIBLE)
    listView.setVisibility(View.VISIBLE)
    failedText.setVisibility(View.GONE)
    retryButton.setVisibility(View.GONE)
    errorDescription.setVisibility(View.GONE)
    clearList()
    fetchSchedule()
  }

}
