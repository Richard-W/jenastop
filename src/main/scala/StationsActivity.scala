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
import android.view.View
import android.widget.{ Button, ListView, ProgressBar, TextView }

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext

class StationsActivity extends Activity {

  var listAdapter: StationsAdapter = null
  implicit val activity = this

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_stations)
    getActionBar.setDisplayUseLogoEnabled(true)
    getActionBar.setDisplayShowHomeEnabled(true)

    listAdapter = new StationsAdapter(this, new java.util.ArrayList[Station])
    listView.setAdapter(listAdapter)

    fetchStations
  }

  def fetchStations = {
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    val favorites = new DatabaseHelper(this).favorites
    Station.fetch(favorites) mapUI { stations ⇒
      progressBar.setVisibility(View.GONE)
      listAdapter.list.clear()
      listAdapter.list.addAll(stations)
      listAdapter.notifyDataSetChanged()
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

  def progressBar = findViewById(R.id.stations_progress_bar).asInstanceOf[ProgressBar]

  def failedText = findViewById(R.id.stations_failed_text).asInstanceOf[TextView]

  def errorDescription = findViewById(R.id.stations_error_description).asInstanceOf[TextView]

  def retryButton = findViewById(R.id.stations_retry_button).asInstanceOf[Button]

  def listView = findViewById(R.id.stations_list_view).asInstanceOf[ListView]

  def onRetryButtonClick(view: View) {
    progressBar.setVisibility(View.VISIBLE)
    listView.setVisibility(View.VISIBLE)
    failedText.setVisibility(View.GONE)
    retryButton.setVisibility(View.GONE)
    errorDescription.setVisibility(View.GONE)

    listAdapter.list.clear()
    listAdapter.notifyDataSetChanged()
    fetchStations
  }
}
