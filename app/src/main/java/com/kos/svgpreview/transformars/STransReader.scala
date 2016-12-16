package com.kos.svgpreview.transformars

import java.io.{BufferedReader, File, FileInputStream, InputStreamReader}

import com.kos.svgpreview.files.AndroidFileUtils

import scala.xml.{PrettyPrinter, XML}

/**
  * Created by Kos on 24.09.2016.
  */
object STransReader {

	def readXml(f: File): String = {
		var res = ""
		AndroidFileUtils.tryFile[BufferedReader](new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8")), {
			reader ⇒
				val sb = new StringBuilder()
				var s = reader.readLine()
				while (s != null) {
					sb.append(s)
					sb.append("\n")

					s = reader.readLine()
				}
				res = sb.toString()
		})
		res
		//		try {
		//			val xml = XML.loadFile(f)
		//		//	val formatted = new PrettyPrinter(150, 2).format(xml)
		//			//formatted
		//			xml.toString()
		//		}catch{
		//			case _:Throwable ⇒ ""
		//		}
	}
}
