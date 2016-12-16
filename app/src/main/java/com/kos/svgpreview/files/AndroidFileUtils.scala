package com.kos.svgpreview.files

import java.io.{FileInputStream, _}

import android.content.Context
import android.os.Environment
import com.kos.svgpreview.{PreviewActivity, R}
import com.kos.svgpreview.data.{BasicData, CommandData}
import com.kos.svgpreview.parser.svg.SvgToDrawableConverter

import scala.util.Try

/**
  * Created by Kos on 14.09.2016.
  */
object AndroidFileUtils {
	def myDocuments(): String = {
		Try(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath).getOrElse("/")
	}


	def getSDPath: File = {
		try { {
			val rawSecondaryStoragesStr: String = System.getenv("SECONDARY_STORAGE")
			if (rawSecondaryStoragesStr != null && !rawSecondaryStoragesStr.isEmpty) {
				val rawSecondaryStorages: Array[String] = rawSecondaryStoragesStr.split(File.pathSeparator)
				for (rawSecondaryStorage <- rawSecondaryStorages) {
					val root: File = new File(rawSecondaryStorage)
					if (root.exists && root.isDirectory && root.canWrite) {
						return root
					}
				}
			}
		}
		}
		catch {
			case ignored: Exception => {

			}
		}
		getPhonePath
	}

	def getPhonePath: File = {
		Try(Environment.getExternalStorageDirectory).getOrElse(new File("/"))
	}

	def getDownloadsPath: File = {
		Try(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).getOrElse(new File("/"))
	}
	def myDocumentsPath: File = {
		Try(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)).getOrElse(new File("/"))
	}

	@inline
	def tryFile[A <: Closeable](top: ⇒ A, body: A ⇒ Unit): Unit = {
		try {
			val a = top
			try {
				body(a)
			} finally {
				a.close()
			}
		} catch {
			case _: Throwable ⇒
		}
	}

	val defualtFilter = new FilenameFilter {
		override def accept(dir: File, name: String): Boolean = {
			if (name.startsWith("."))
				return false
			true

		}
	}


	def loadListWithCommand(file: File,context:Context): Seq[BasicData] ={
		var fl=loadList(file)
		val estSvg=fl.exists(_.isSvg)

		if (estSvg){
			fl = new CommandData(file,BasicData.COMMAND_ALL_SVG,context.getString(R.string.commandSvgAllPreview)) +: fl
		}

		fl
	}

	def loadList(file: File): Seq[BasicData] = {
		try {
			if (file.exists() && file.isDirectory) {
				file.listFiles(AndroidFileUtils.defualtFilter).map(new BasicData(_)).sorted
			} else {
				Seq[BasicData]()
			}
		}catch{
			case _:Throwable ⇒
				Seq[BasicData]()
		}
	}

	def loadList(folderPath: String,context:Context): Seq[BasicData] = {

		val arg: Seq[BasicData] = if (folderPath == null) {
			val a = new CommandData(AndroidFileUtils.getPhonePath,0, context.getString(R.string.filePhonePath))
			val b = new CommandData(AndroidFileUtils.getSDPath,0, context.getString(R.string.fileSDPath))
			val com=Seq[BasicData](
				new CommandData(AndroidFileUtils.myDocumentsPath,0, context.getString(R.string.fileDocumentsPath)),
			new CommandData(AndroidFileUtils.getDownloadsPath,0, context.getString(R.string.fileDownloadsPath))
			) //new CommandData(new File("create"), 1,"Create Xml"),new CommandData(new File("svg"), 1,"Create SVG"))
			if (a.getPath == b.getPath) {
				a +: com
			} else {
				Seq[BasicData](	a,	b) ++com
			}
		} else {
			val file = new File(folderPath)
			loadList(file)
		}

		arg
	}

	def svgFilesList(f: File) = {
		val prom=f.listFiles(new FilenameFilter {
			override def accept(dir: File, name: String): Boolean = {
				if (name.toLowerCase.endsWith(".svg"))
					return true
				false
			}
		})
		if (prom!=null)
			prom.sorted
		else
			Array[File]()
	}

	def getTabList(context:Context,file:File):Seq[BasicData] = {
		val fl = AndroidFileUtils.loadListWithCommand(file, context)
			fl
	}
}
