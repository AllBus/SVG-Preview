package com.kos.svgpreview.parser.svg

import scala.collection.mutable
import scala.xml.Node

/**
  * Created by Kos on 13.09.2016.
  */
object CssStyleObject {

	type CssStyle=Map[String,String]

	def addStyle(css: CssStyle, attrLine: String ):CssStyle={
		css ++ readStyle(attrLine)
	}

	def readStyle(attrLine: String ):CssStyle={
		attrLine.split(';').map(x ⇒ x.split(':')).collect {
			case Array(name, value) ⇒ name.trim -> value.trim
		}.toMap
	}



	def createStyle(attrLine:String):CssStyle={
		readStyle(attrLine)
	}

	def addAttribute(node: Node, attrName: String): CssStyle = {
		val attr = node \@ attrName
		if (attr.nonEmpty)
			Map(attrName → attr)
		else
			Map()
	}

	def addAttribute(node: Node): CssStyle = {
		node.attributes.asAttrMap

//	addAttribute(node, "fill") ++
//		addAttribute(node, "stroke")++
//		addAttribute(node, "stroke-miterlimit")++
//		addAttribute(node, "stroke-linecap")++
//		addAttribute(node, "stroke-width")++
//		addAttribute(node, "transform")++
//		addAttribute(node, "stroke-linejoin")++
//		addAttribute(node, "stroke-opacity")++
//		addAttribute(node, "fill-opacity")
//	val attr = node \@ attrName
//	if (attr.nonEmpty)
//		Map(attrName → attr)
//	else
//		Map()
}


	/**
	  * чтение содержимого тега style
	  * @param styleMapOut
	  * @param text
	  */
	def readStyleTag(styleMapOut: mutable.Map[String, CssStyle], text: String): Unit = {
		text.split('}').foreach { x ⇒
			val t = x.split('{')
			if (t.length == 2) {
				styleMapOut += t(0).trim → readStyle(t(1))
			}
		}
	}

	implicit class CssStyleImpl(val cssStyle:CssStyle) extends AnyVal{
		def \@(arg:String) = cssStyle.getOrElse(arg,"")
	}

}
