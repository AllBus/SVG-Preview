package com.kos.svgpreview.data

import java.io.File

import android.net.Uri

/**
  * Created by Kos on 25.09.2016.
  */
class SvgCommandData(file:File,val command:Int) extends BasicData(file){


}


class ContentCommandData(val uri:Uri,val command:Int) extends BasicData(new File("")){
	override def getPath: String = uri.getPath

	override def getUri: Uri = uri
}