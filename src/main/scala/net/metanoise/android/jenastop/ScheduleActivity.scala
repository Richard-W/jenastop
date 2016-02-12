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
import android.view.{ Menu, MenuItem, View }
import android.widget.{ Button, ListView, ProgressBar, TextView }
import net.metanoise.android.jenastop.ui.ScalaActivity

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext

class ScheduleActivity extends ScalaActivity {

  var station: String = null
  var listAdapter: ScheduleAdapter = null
  var originallyOrdered: Seq[Schedule] = null
  var sorting: Int = 0
  var timer: Timer = null

  implicit val activity = this

  def contentView = getLayoutInflater.inflate(R.layout.activity_schedule, null)

  protected override def onCreate(savedInstanceState: Bundle) {
    // Create UI
    super.onCreate(savedInstanceState)

    val actionBar = getSupportActionBar
    actionBar.setHomeButtonEnabled(true)
    actionBar.setDisplayHomeAsUpEnabled(true);

    // Get station from intent
    val intent = getIntent
    station = intent.getStringExtra("station")
    actionBar.setSubtitle(station)

    // Setup ListView
    listAdapter = new ScheduleAdapter(this, new java.util.ArrayList[Schedule])
    listView.setAdapter(listAdapter)
    listView.setClickable(false)

    // Initialize other fields
    originallyOrdered = Seq()
    sorting = R.id.sort_by_time
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
    bundle.putInt("sorting", sorting)
    bundle.putParcelableArray("schedules", originallyOrdered.toArray)
  }

  override def onRestoreInstanceState(bundle: Bundle): Unit = {
    super.onRestoreInstanceState(bundle)
    sorting = bundle.getInt("sorting")
    originallyOrdered = bundle.getParcelableArray("schedules").toSeq map { _.asInstanceOf[Schedule] }
    displayList()
  }

  def displayList(): Unit = {
    val sorted = sorting match {
      case R.id.sort_by_dest ⇒ originallyOrdered.sortBy { _.destination }
      case R.id.sort_by_line ⇒ originallyOrdered.sortBy { _.line }
      case R.id.sort_by_time ⇒ originallyOrdered
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
      case R.id.home | android.R.id.home ⇒
        this.finish()
      case R.id.action_refresh =>
        clearList()
        progressBar.setVisibility(View.VISIBLE)
        fetchSchedule()
      case R.id.sort_by_dest | R.id.sort_by_line | R.id.sort_by_time ⇒
        sorting = item.getItemId
        displayList()
      case _ ⇒
        return super.onOptionsItemSelected(item)
    }
    true
  }

  def fetchSchedule(): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

    if (originallyOrdered.isEmpty) {
      // No data available from previous fetches ⇒ display progress bar
      progressBar.setVisibility(View.VISIBLE)
    }

    Schedule.fetch(station) mapUI { schedules ⇒
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

  def failedText = findViewById(R.id.schedule_failed_text).asInstanceOf[TextView]

  def errorDescription = findViewById(R.id.schedule_error_description).asInstanceOf[TextView]

  def retryButton = findViewById(R.id.schedule_retry_button).asInstanceOf[Button]

  def listView = findViewById(R.id.schedule_list_view).asInstanceOf[ListView]

  def progressBar = findViewById(R.id.schedule_progress_bar).asInstanceOf[ProgressBar]

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
