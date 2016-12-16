package com.kos.svgpreview.data

import java.io.File

/**
  * Created by Kos on 15.09.2016.
  */
object BasicData {
	val imageExts = Array("png", "jpeg", "jpg","gif","bmp","webp")
	val textExts=Array("txt","ini","md","c","cpp","h","hpp","m","java","scala","pas","css","js","php","properties")
	val webExts=Array("html","htm","svg")//todo: svg

	val COMMAND_ALL_SVG = 11
	val COMMAND_CREATE_ALL_SVG = 12
}

class BasicData(val file: File) extends Ordered[BasicData]{

	import BasicData._

	def getName = file.getName

	def getPath = file.getPath

	def getCommand = 0

	lazy val ext = {
		if (file.isFile) {
			val fn = file.getName
			val i = fn.lastIndexOf('.')
			if (i > 0) {
				fn.drop(i + 1).toLowerCase
			} else {
				""
			}
		} else {
			""
		}
	}

	lazy val isDirectory = file.isDirectory
	lazy val isXml = ext == "xml"
	lazy val isSvg = ext == "svg"

	lazy val isImage = imageExts.contains(ext)
	lazy val isText = textExts.contains(ext)
	lazy val isWeb = webExts.contains(ext)

	override def compare(that: BasicData): Int = {
		if (this.isDirectory){
			if (that.isDirectory){
				this.getName.compareTo(that.getName)
			}else{
				-1
			}
		}else {
			if (that.isDirectory) {
				1
			}else{
				this.getName.compareTo(that.getName)
			}
		}
	}

}
