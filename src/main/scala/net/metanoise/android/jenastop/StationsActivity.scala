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

import android.os.{ AsyncTask, Bundle }
import android.support.v4.content.ContextCompat
import android.view.{ Menu, MenuItem, View }
import android.widget._
import net.metanoise.android.jenastop.ui.{ ScalaActivity, NavigationDrawer }

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext

class StationsActivity extends ScalaActivity with NavigationDrawer {

  /* UI elements */
  lazy val progressBar = findViewById(R.id.stations_progress_bar).asInstanceOf[ProgressBar]
  lazy val failedText = findViewById(R.id.stations_failed_text).asInstanceOf[TextView]
  lazy val errorDescription = findViewById(R.id.stations_error_description).asInstanceOf[TextView]
  lazy val retryButton = findViewById(R.id.stations_retry_button).asInstanceOf[Button]
  lazy val listView = findViewById(R.id.stations_list_view).asInstanceOf[ListView]

  lazy val listAdapter: StationsAdapter = new StationsAdapter(this, new java.util.ArrayList[Station])

  override protected lazy val navigationAdapter = new NavigationAdapter(this)
  override protected val navigationOpenResource: Int = R.string.nav_open
  override protected val navigationCloseResource: Int = R.string.nav_close
  override protected lazy val navigationBackgroundColor: Int = ContextCompat.getColor(this, R.color.navDrawerBackground)

  implicit val activity = this

  def contentView = getLayoutInflater.inflate(R.layout.activity_stations, null)

  override def onCreate(savedInstanceState: Bundle): Unit = withDatabase { db ⇒
    super.onCreate(savedInstanceState)
    listView.setAdapter(listAdapter)

    val stations = db.stations

    if (db.flag("needStationsUpdate")) {
      fetchStations
      db.flag("needStationsUpdate", false)
    } else {
      listAdapter.list.addAll(stations)
      listAdapter.notifyDataSetChanged()
    }
  }

  def fetchStations = withDatabase { db ⇒
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

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

  def onRetryButtonClick(view: View) {
    listAdapter.list.clear()
    listAdapter.notifyDataSetChanged()
    fetchStations
  }

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
}
