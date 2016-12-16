package com.kos.svgpreview.stylesheets.previewstyles

import scalacss.Defaults._

/**
  * Created by Kos on 28.09.2016.
  */
object AllSvg extends StyleSheet.Inline {
	import dsl._
	val sign = style(
		width(160.px),
		height.auto,
		margin(4.px)
	)

	val btn = style(
		height(36.px),
		backgroundColor(c"#ff9800"),
		padding(8.px),
		margin(16.px),
		color(white),
		textAlign.center,
		verticalAlign.middle


	)


}
