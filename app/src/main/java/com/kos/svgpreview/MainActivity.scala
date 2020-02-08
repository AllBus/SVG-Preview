package com.kos.svgpreview

import java.io._

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View.OnClickListener
import android.view.{MenuItem, View}
import android.widget.{PopupMenu, TextView}
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.{LinearLayoutManager, RecyclerView}
import com.google.android.material.TabLayoutWithListener
import com.kos.svgpreview.adapters.NavFileAdapter
import com.kos.svgpreview.bus.BusSvgConvert
import com.kos.svgpreview.data.{BasicData, CommandData}
import com.kos.svgpreview.files.{AndroidFileUtils, TRestorer}
import com.squareup.otto.Subscribe


object InfoActivity {
	val DATA_FOLDER_NAME = "show_folder_name"
	val DATA_COMMAND_NAME = "show_command_name"
	val LOG_TAG = "Kos"
	val KEY_NAVIGATE_HOME = "key_navigate_home"

	val PREFERENCE= "preference"
	val PREVIEW_COLOR = "key_preview_color"

	val FILE_PERMISSION = 10002
}

class MainActivity extends TActivity with PopupMenu.OnMenuItemClickListener {

	import InfoActivity._


	lazy val list = find[RecyclerView](R.id.list)
	lazy val tabs = find[TabLayoutWithListener](R.id.tabs)

	lazy val moreBtn= find[View](R.id.moreBtn)

	def folderName(folder: String): String = {
		if (folder == null)
			getString(R.string.homeFolder)
		else {
			new File(folder).getName
		}

	}

	protected override def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

//		setupToolBarWithBackButton(R.id.toolBar)

		setupTabs(tabs)

		loadFileList()

		moreBtn.setOnClickListener(this)
		checkPermissions()
	}


	override def onResume(): Unit = {
		super.onResume()

	}

	def loadFileList(): Unit = {
		val intent = getIntent
		val folder = if (intent != null) {
			intent.getStringExtra(DATA_FOLDER_NAME)
		} else null

		val folderList = if (folder == null) AndroidFileUtils.loadList(folder,this) else AndroidFileUtils.loadListWithCommand(new File(folder), this)
		if (folderList.isEmpty) {
			val blockText = find[TextView](R.id.blockText)
			blockText.setVisibility(View.VISIBLE)
		} else {
			val blockText = find[TextView](R.id.blockText)
			blockText.setVisibility(View.GONE)
			val adapter = new NavFileAdapter(this,
				new OnClickListener {
					override def onClick(v: View): Unit = {
						v.getTag match {
							case f: CommandData =>
//								if (f.command == 1) {
//									TRestorer.RestoreBook(getApplicationContext, getResources, f.getName)
//								} else {
									openFolder(f)
							//	}
							case f: BasicData => openFolder(f)
							case _ =>
						}
					}
				}
				, folderList)


			list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false))
			list.setAdapter(adapter)
		}


		tabs.removeAllTabs()
		tabs.addTab(tabs.newTab().setText(folderName(folder)))

	}

	def openFolder(f: BasicData): Unit = {
		if (f.getCommand != 0) {
			val intent = new Intent(this, classOf[PreviewActivity])
			intent.putExtra(DATA_FOLDER_NAME, f.getPath)
			intent.putExtra(DATA_COMMAND_NAME, f.getCommand)
			startActivity(intent)
		} else {
			if (f.isDirectory) {
				val intent = new Intent(this, classOf[MainActivity])
				intent.putExtra(DATA_FOLDER_NAME, f.getPath)
				startActivity(intent)
			} else {
				val intent = new Intent(this, classOf[PreviewActivity])
				intent.putExtra(DATA_FOLDER_NAME, f.getPath)
				startActivity(intent)
			}
		}
	}

	def showPoopup(): Unit = {
		val popup = new PopupMenu(this, moreBtn)
		popup.inflate(R.menu.construct)
		popup.setOnMenuItemClickListener(this)
		popup.show()

	}

	override def onClick(v: View): Unit = {
		v.getId match {
			case R.id.moreBtn ⇒ showPoopup()
			case _ ⇒ super.onClick(v)
		}

	}

	override def onMenuItemClick(item: MenuItem): Boolean = {
		item.getItemId match {
			case R.id.menuUpdateBtn ⇒	loadFileList()
			case R.id.menuAboutBtn ⇒ startActivity(new Intent(this,classOf[AboutActivity]))
			case _ ⇒

		}
		true
	}

	@Subscribe
	def busCompleteConvert(updater:BusSvgConvert): Unit ={
		if (updater.isComplete>0) {
			if (updater.isComplete == 1)
				snack(list, R.string.snackCompleteCreateVector)
			else
				snack(list, R.string.snackComplete2CreateVector)
			loadFileList()
//			val file=new File( updater.fileName).getParent
//			if (file!=null){
//				val intent = getIntent
//				val folder = if (intent != null) {
//					intent.getStringExtra(DATA_FOLDER_NAME)
//				} else null
//
//				if (folder!=null){
//					new File(folder).getAbsolutePath==file
//				}
//
//			}
//
		}else{
			snack(list, R.string.snackCompleteFailCreateVector)
		}
	}

	private def checkPermissions(): Boolean = {
		if (ContextCompat.checkSelfPermission(this,
			android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, Array(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE), FILE_PERMISSION)
			return false
		}
		return true
	}




	override def onRequestPermissionsResult(requestCode: Int,
											permissions: Array[String],
											grantResults: Array[Int]
										   ) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == FILE_PERMISSION) {

			if (grantResults.nonEmpty) {
				if (grantResults(0) == PackageManager.PERMISSION_GRANTED) {
					showAccess()

				} else {
					showViewNoAccess()
					if (!shouldShowRequestPermissionRationale(permissions(0)) && isLaunchPermScreen) {
						isLaunchPermScreen = false
						val intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
						val uri = Uri.fromParts("package", this.getPackageName, null)
						intent.setData(uri)
						startActivity(intent)
					}
				}
			}
		}
	}

	var isLaunchPermScreen = false

	def showViewNoAccess() = {}

	def showAccess() = {
		loadFileList()
	}
}