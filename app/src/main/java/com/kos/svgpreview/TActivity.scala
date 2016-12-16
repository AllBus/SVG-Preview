package com.kos.svgpreview

import android.support.design.widget.TabLayoutWithListener
import android.util.Log
import android.view.{MenuItem, View}
import android.view.View.OnClickListener
import android.widget.Button
import com.kos.svgpreview.common.SActivity

/**
  * Created by Kos on 30.09.2016.
  */
class TActivity extends SActivity with OnClickListener{


	override def onOptionsItemSelected(item: MenuItem): Boolean = {
		item.getItemId match {
			case android.R.id.home ⇒
				onBackPressed()
				true
			case _ ⇒
				super.onOptionsItemSelected(item)
		}
	}

	def setupTabs(tabs: TabLayoutWithListener) ={

		val leftBtn= find[View](R.id.backBtn)
		leftBtn.setOnClickListener(this)
		tabs.setScrollListener(new TabLayoutWithListener.IScrollTab() {
			def onScrollTabs(x: Float) {
				leftBtn.setX(-x)

			}
		})
	}

	def showBackBtn(): Unit ={
		find[View](R.id.backBtn) match {
			case leftBtn :View ⇒
				if (leftBtn.getX< -leftBtn.getWidth/2) {
					leftBtn.setX(-leftBtn.getWidth)
					leftBtn.animate().translationX(0)
				}

			case _ ⇒
		}

	}

	override def onClick(v: View): Unit = {
		v.getId match{
			case R.id.backBtn ⇒

				onBackPressed()
			case _ ⇒
		}
	}
}
