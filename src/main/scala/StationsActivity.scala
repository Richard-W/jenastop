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
import android.view.{ MenuItem, Menu, View }
import android.widget.{ Button, ListView, ProgressBar, TextView }

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext

class StationsActivity extends Activity {

  var listAdapter: StationsAdapter = null
  implicit val activity = this

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.menu_stations, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.action_refresh ⇒
        fetchStations
        true
      case _ ⇒
        super.onOptionsItemSelected(item)
    }
  }

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_stations)
    getActionBar.setDisplayUseLogoEnabled(true)
    getActionBar.setDisplayShowHomeEnabled(true)

    listAdapter = new StationsAdapter(this, new java.util.ArrayList[Station])
    listView.setAdapter(listAdapter)

    val db = new DatabaseHelper(this)
    val stations = db.stations

    // After upgrade from Database version 1 to 2 the database only contains favorite stations.
    // Also the set might be entirely empty. In both cases we have to update the stations
    // database. If a user has marked every stations favorite this always refreshes, which
    // might be considered a FIXME.
    if (stations.exists { !_.favorite }) {
      listAdapter.list.addAll(stations)
      listAdapter.notifyDataSetChanged()
    } else {
      fetchStations
    }
  }

  def fetchStations = {
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    val db = new DatabaseHelper(this)

    progressBar.setVisibility(View.VISIBLE)
    listView.setVisibility(View.VISIBLE)
    failedText.setVisibility(View.GONE)
    retryButton.setVisibility(View.GONE)
    errorDescription.setVisibility(View.GONE)
    listAdapter.list.clear()
    listAdapter.notifyDataSetChanged()

    db.updateStations() mapUI { _ ⇒
      val stations = db.stations
      progressBar.setVisibility(View.GONE)
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

    listAdapter.list.clear()
    listAdapter.notifyDataSetChanged()
    fetchStations
  }
}
