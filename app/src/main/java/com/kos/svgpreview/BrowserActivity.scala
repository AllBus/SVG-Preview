package com.kos.svgpreview

import android.content.Context
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import com.kos.svgpreview.common.SActivity

class BrowserActivity extends SActivity {

	import InfoActivity._

	override protected def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_browser)


		val intent = getIntent
		val folder = if (intent != null) {
			intent.getData
		//	intent.getStringExtra(DATA_FOLDER_NAME)
		} else null

		if (folder!=null) {
			val webView = find[WebView](R.id.webView)
			webView.loadUrl(folder.toString)
			val webSettings = webView.getSettings
			webSettings.setBuiltInZoomControls(true)
			webSettings.setDisplayZoomControls(false)
			webSettings.setSupportZoom(true)

		}

		loadBackColor()
	}

	def loadBackColor(): Unit ={
		val sPref = getSharedPreferences(InfoActivity.PREFERENCE, Context.MODE_PRIVATE)
		val color = sPref.getInt(PREVIEW_COLOR, 0xffffffff)
		find[View](R.id.webView).setBackgroundColor(color)
	}

	def setBackColor(color: Int): Unit = {
		find[View](R.id.webView).setBackgroundColor(color)
		import android.content.SharedPreferences.Editor
		val sPref = getSharedPreferences(InfoActivity.PREFERENCE, Context.MODE_PRIVATE)
		val ed = sPref.edit
		ed.putInt(PREVIEW_COLOR, color)
		ed.commit()
	}
}