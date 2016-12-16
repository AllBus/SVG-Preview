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
//	def RestoreBook(context: Context, resource: Resources, archiveName: String) {
//		try {
//			val _location: String = AndroidFileUtils.getPhonePath.getPath + "/svg_preview/" + archiveName + "/"
//			_dirChecker(_location)
//			val zin: ZipInputStream = new ZipInputStream(resource.openRawResource(
//				if (archiveName == "Create SVG") R.raw.svg
//				else
//					R.raw.sifns)
//			)
//			var ze: ZipEntry = null
//			while ( {
//				ze = zin.getNextEntry; ze
//			} != null) {
//				{
//					if (ze.isDirectory) {
//					}
//					else {
//						_dirParentChecker(_location + ze.getName)
//						try {
//
//							val fout: FileOutputStream = new FileOutputStream(_location + ze.getName)
//							try {
//								val buffer: Array[Byte] = new Array[Byte](1024)
//								var length: Int = 0
//								while ( {
//									length = zin.read(buffer); length
//								} > 0) {
//									{
//										fout.write(buffer, 0, length)
//									}
//								}
//							} finally {
//								fout.close()
//							}
//						}
//						catch {
//							case ignored: Exception =>
//						}
//						zin.closeEntry()
//					}
//				}
//			}
//			zin.close()
//		}
//		catch {
//			case ignored: Exception =>
//		}
//	}

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
