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
import android.os.{ AsyncTask, Bundle }
import android.view.{ Menu, MenuItem, View }
import android.widget.{ Button, ListView, ProgressBar, TextView }

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext

class ScheduleActivity extends Activity {

  var station: String = null
  var listAdapter: ScheduleAdapter = null
  implicit val activity = this

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.menu_schedule, menu)
    return true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.action_refresh =>
        listAdapter.list.clear()
        listAdapter.notifyDataSetChanged()
        progressBar.setVisibility(View.VISIBLE)
        fetchSchedule
        true
      case _ =>
        super.onOptionsItemSelected(item)
    }
  }

  def fetchSchedule = {
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    Schedule.fetch(station) mapUI { schedules ⇒
      listAdapter.addAll(schedules)
      listAdapter.notifyDataSetChanged()
      progressBar.setVisibility(View.GONE)
    } recoverUI {
      case t: Throwable ⇒
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
    listAdapter.list.clear()
    listAdapter.notifyDataSetChanged()
    fetchSchedule
  }

  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_schedule)
    getActionBar.setDisplayUseLogoEnabled(true)
    getActionBar.setDisplayShowHomeEnabled(true)

    val intent = getIntent
    station = intent.getStringExtra("station")
    getActionBar.setSubtitle(station)

    listAdapter = new ScheduleAdapter(this, new java.util.ArrayList[Schedule])
    listView.setAdapter(listAdapter)
    listView.setClickable(false)

    fetchSchedule
  }
}
