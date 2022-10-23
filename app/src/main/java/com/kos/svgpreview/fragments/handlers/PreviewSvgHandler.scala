package com.kos.svgpreview.fragments.handlers

import com.kos.svgpreview.fragments.PreviewPageFragment
import java.lang.ref.WeakReference

import android.os.{Handler, Message}
import com.kos.svgpreview.adapters.holders.SvgHolder

/**
  * Created by Kos on 25.09.2016.
  */
//noinspection ScalaDeprecation
class PreviewSvgHandler(previewPageFragment: PreviewPageFragment) extends Handler{
	private[this] val refFragment=new WeakReference(previewPageFragment)

	override def handleMessage( msg: Message) {
		val fragment = refFragment.get()
		if (fragment!=null) {
			msg.obj match {
				case svg: SvgHandlerResult ⇒
					val fv=fragment.getView
					if (fv!=null) {
						val svgHolder = new SvgHolder(fv)
						svgHolder.bind(svg.f, svg.vectorState)
					}
				case _ ⇒
			}
		}
	}
}
