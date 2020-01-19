package com.kos.svgpreview.files

import java.io.{File, FileOutputStream}
import java.util.zip.{ZipEntry, ZipInputStream}

import android.content.Context
import android.content.res.Resources
import com.kos.svgpreview.R


/**
  * Created by Kos on 15.09.2016.
  */
object TRestorer {


	def _dirChecker(dir: String) {
		val f: File = new File(dir)
		if (!f.isDirectory) {
			f.mkdirs
		}
	}

	private def _dirParentChecker(dir: String) {
		var f: File = new File(dir)
		f = f.getParentFile
		if (f != null) {
			if (!f.isDirectory) {
				f.mkdirs
			}
		}
	}
}
