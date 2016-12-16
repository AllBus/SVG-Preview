package com.kos.svgpreview.adapters.holders

import java.io.File

import android.view.View
import android.widget.TextView
import com.kos.svgpreview.R
import com.kos.svgpreview.parser.XmlView
import com.kos.svgpreview.transformars.STransReader

/**
  * Created by Kos on 25.09.2016.
  */
class SvgHolder(val itemView: View) {

	def find[T <: View](resId: Int) = itemView.findViewById(resId).asInstanceOf[T]

	private[this] val smallImage = find[XmlView](R.id.smallImage)
	private[this] val appImage = find[XmlView](R.id.appImage)
	private[this] val realImage = find[XmlView](R.id.realImage)
	private[this] val imageXml = find[XmlView](R.id.image)
	private[this] val textXml = find[TextView](R.id.textXml)

	def bind(f: File, vectorState: XmlView.VectorDrawableCompatState): Unit = {
		if (vectorState != null) {
			if (vectorState.hasImage) {

				imageXml.fromVectorState(vectorState)
				smallImage.fromVectorState(vectorState)
				appImage.fromVectorState(vectorState)
				realImage.fromVectorState(vectorState)

			} else {
				find[View](R.id.imagesLayout).setVisibility(View.GONE)
			}

		}
		if (f != null) {
			try {
				textXml.setText(STransReader.readXml(f))
			} catch {
				case _: Throwable ⇒
			}
		}
	}
}
