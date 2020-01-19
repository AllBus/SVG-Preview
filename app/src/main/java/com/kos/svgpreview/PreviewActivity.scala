package com.kos.svgpreview

import java.io.File

import android.os.Bundle
import android.view.MenuItem
import android.view.View.OnClickListener
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.TabLayoutWithListener
import com.kos.svgpreview.adapters.FilePagerAdapter
import com.kos.svgpreview.bus.{BusSvgConvert, IBusCommand}
import com.kos.svgpreview.data.BasicData
import com.kos.svgpreview.files.AndroidFileUtils
import com.kos.svgpreview.threads.AsynhConvert
import com.squareup.otto.Subscribe

class PreviewActivity extends TActivity with IBusCommand with OnClickListener {

	import InfoActivity._

	var adapter: FilePagerAdapter = _
	lazy val viewPager = find[ViewPager](R.id.pager)
	lazy val tabs = find[TabLayoutWithListener](R.id.tabs)
	var showBackBtnPost = false


	var navigateHome = false

	var folder: String = null
	var command = 0


	override protected def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_preview)

		val intent = getIntent

		if (intent != null) {
			folder = intent.getStringExtra(DATA_FOLDER_NAME)
			command = intent.getIntExtra(DATA_COMMAND_NAME, 0)
		}


		//	setupToolBarWithBackButton(R.id.toolBar)

		setupTabs(tabs)


		if (savedInstanceState != null) {
			navigateHome = savedInstanceState.getBoolean(KEY_NAVIGATE_HOME, false)
		}


		if (folder != null) {
			//			openXml(new File(folder))

			val (f: File, list: Seq[BasicData]) = readFileList
			adapter = new FilePagerAdapter(this, fragmentManger, list)

			viewPager.setAdapter(adapter)
			viewPager.setCurrentItem(adapter.itemIndex(f, 0))
		}


		tabs.setupWithViewPager(viewPager)
		viewPager.addOnPageChangeListener(new OnPageChangeListener {
			override def onPageScrollStateChanged(state: Int): Unit = {

				if (state == ViewPager.SCROLL_STATE_IDLE && showBackBtnPost) {
					showBackBtnPost = false
					showBackBtn()
				}

			}

			override def onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int): Unit = {

			}

			override def onPageSelected(position: Int): Unit = {
				if (position == 0)
					navigateHome = false
			}
		})

	}

	def readFileList: (File, Seq[BasicData]) = {
		val f = new File(folder)
		val list: Seq[BasicData] = command match {

			case 0 ⇒
				val fp = f.getParentFile
				if (fp.isDirectory) {
					AndroidFileUtils.getTabList(this, fp)
				} else
					Seq.empty

			case BasicData.COMMAND_ALL_SVG ⇒

				if (f.isDirectory) {
					AndroidFileUtils.getTabList(this, f)
				} else
					Seq.empty

			case _ ⇒
				Seq.empty
		}
		(f, list)
	}

	override def onOptionsItemSelected(item: MenuItem): Boolean = {
		item.getItemId match {
			case android.R.id.home ⇒
				onBackPressed()
				true
			case _ ⇒
				super.onOptionsItemSelected(item)
		}
	}

	override def openFromCommand(fileName: String, command: Int, addBack: Boolean): Unit = {
		if (adapter != null && fileName != null) {
			command match {
				case BasicData.COMMAND_CREATE_ALL_SVG ⇒
					new AsynhConvert(AndroidFileUtils.myDocuments()).execute(fileName)

				case _ ⇒
					navigateHome = true
					viewPager.setCurrentItem(adapter.itemIndex(new File(fileName), command))
					showBackBtnPost = true
			}
		}
	}


	override def onSaveInstanceState(outState: Bundle): Unit = {
		super.onSaveInstanceState(outState)
		outState.putBoolean(KEY_NAVIGATE_HOME, navigateHome)

	}

	override def onBackPressed(): Unit = {
		if (navigateHome && viewPager.getCurrentItem > 0) {
			navigateHome = false
			viewPager.setCurrentItem(0)
		} else {
			super.onBackPressed()
		}
	}

	//	def openXml(f: File): Unit = {
	//
	//		text.setText(f.getName)
	//
	//		AndroidFileUtils.tryFile[BufferedReader](new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8")), {
	//			reader ⇒
	//
	//				val factory = XmlPullParserFactory.newInstance();
	//				// включаем поддержку namespace (по умолчанию выключена)
	//				factory.setNamespaceAware(true);
	//
	//				// создаем парсер
	//				val xpp = factory.newPullParser();
	//				xpp.setInput(reader) // даем парсеру на вход Reader
	//				imageXml.parse(xpp)
	//
	//				smallImage.fromVectorState(imageXml.getVectorState)
	//				appImage.fromVectorState(imageXml.getVectorState)
	//				realImage.fromVectorState(imageXml.getVectorState)
	//
	//		})
	//
	//		AndroidFileUtils.tryFile[BufferedReader](new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8")), {
	//			reader ⇒
	//				val sb = new StringBuilder()
	//				var s = reader.readLine()
	//				while (s != null) {
	//					sb.append(s)
	//					sb.append("\n")
	//
	//					s = reader.readLine()
	//				}
	//				textXml.setText(sb.toString())
	//		})
	//	}

	override def cancelNavigateStack(command: Int, addBack: Boolean): Unit = {
		navigateHome = false
	}

	@Subscribe
	def busCompleteConvert(updater: BusSvgConvert): Unit = {
		if (updater.isComplete>0) {
			if (updater.isComplete == 1)
				snack(viewPager, R.string.snackCompleteCreateVector)
			else
				snack(viewPager, R.string.snackComplete2CreateVector)
//			val (f: File, list: Seq[BasicData]) = readFileList
//			if (adapter!=null){
//				adapter.update(list)
//			}

		} else {
			snack(viewPager, R.string.snackCompleteFailCreateVector)
		}
	}

}