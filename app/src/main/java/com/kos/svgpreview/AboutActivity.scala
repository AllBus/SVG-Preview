package com.kos.svgpreview

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.TabLayoutWithListener
import com.kos.common.helpers.DeviceHelper
import com.kos.common.helpers.IntentHelper

class AboutActivity extends TActivity with View.OnClickListener {

	lazy val tabs = find[TabLayoutWithListener](R.id.tabs)


	//noinspection ScalaDeprecation
	override protected def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_about)
		try {
			findViewById[View](R.id.tvVersion).asInstanceOf[TextView].setText(getString(R.string.ap_version, getPackageManager.getPackageInfo(this.getPackageName, 0).versionName))
		}
		catch {
			case e: PackageManager.NameNotFoundException ⇒
		}
		findViewById[View](R.id.btnWriteLater).setOnClickListener(this)
		findViewById[View](R.id.btnMarkApp).setOnClickListener(this)
		findViewById[View](R.id.btnPrivatePolicy).setOnClickListener(this)

		setupTabs(tabs)
		tabs.addTab(tabs.newTab().setText(R.string.titleAbout))
	}

	def writeToUs {
		val emailIntent: Intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.sm_email), null))
		try {
			val emailSubject: String = getString(R.string.sm_email_subject, getString(R.string.app_name), getPackageManager.getPackageInfo(getPackageName, 0).versionName, getString(R.string.app_platform))
			val emailText: String = getString(R.string.sm_email_text, DeviceHelper.getAndroidVersion, DeviceHelper.getDeviceName)
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
			emailIntent.putExtra(Intent.EXTRA_TEXT, emailText)
			startActivity(emailIntent)
		}
		catch {
			case ignored: Exception ⇒ {
			}
		}
	}

	override def onClick(v: View) {
		v.getId match {
			case R.id.btnWriteLater ⇒
				writeToUs

			case R.id.btnMarkApp ⇒
				IntentHelper.openPlayMarket(this, IntentHelper.GOOGLE_MARKET_ID_PARAMETER + getPackageName)

			case R.id.btnPrivatePolicy ⇒
				IntentHelper.showBrowser(this,getString(R.string.privatePolicyUri))
			case _ ⇒ super.onClick(v)
		}
	}
}