package com.kos.svgpreview.common

import android.app.Activity
import android.view.View
import android.widget.Toolbar
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kos.svgpreview.bus.BusProvider

/**
  * Created by Kos on 23.09.2016.
  */


class SActivity extends AppCompatActivity {



	def find[T <: View](resId: Int) = findViewById(resId).asInstanceOf[T]

	def fragmentManger=getSupportFragmentManager

	def setupToolBarWithBackButton(@IdRes toolbarId:Int): Unit ={
		setupToolBar(toolbarId)


		val actionBar=getActionBar
		if (actionBar!=null){

			actionBar.setDisplayHomeAsUpEnabled(true)

		}
	}

	def setupToolBar(@IdRes toolbarId:Int): Unit ={
		val toolbar = findViewById(toolbarId).asInstanceOf[Toolbar]
		setActionBar(toolbar)
	}

	def snack(view:View,stringId:Int) ={
		Snackbar.make(view,stringId,Snackbar.LENGTH_SHORT).show()
	}

	override def onResume(): Unit = {
		super.onResume()
		BusProvider.register(this)
	}

	override def onPause(): Unit = {
		super.onPause()
		BusProvider.unregister(this)
	}
}
