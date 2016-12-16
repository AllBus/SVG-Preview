package com.kos.svgpreview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import com.kos.svgpreview.common.SActivity

class BrowserActivity extends SActivity {

	import InfoActivity._

	override protected def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_browser)


		val intent = getIntent
		val folder = if (intent != null) {
			intent.getStringExtra(DATA_FOLDER_NAME)
		} else null

		if (folder!=null) {
			val webView = find[WebView](R.id.webView)
			webView.loadUrl(folder)
			val webSettings = webView.getSettings
			webSettings.setBuiltInZoomControls(true)
			webSettings.setDisplayZoomControls(false)
			webSettings.setSupportZoom(true)
		}
	}
}