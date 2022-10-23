package com.kos.svgpreview.fragments

import android.content.{Context, Intent}
import android.graphics.{BitmapFactory, Color}
import android.net.Uri
import android.os.Bundle
import android.view.View.OnClickListener
import android.view.{LayoutInflater, View, ViewGroup}
import android.webkit.{WebView, WebViewClient}
import android.widget.{ImageView, TextView}
import androidx.recyclerview.widget.{LinearLayoutManager, RecyclerView}
import com.kos.svgpreview._
import com.kos.svgpreview.adapters.NavFileAdapter
import com.kos.svgpreview.bus.IBusCommand
import com.kos.svgpreview.common.SFragment
import com.kos.svgpreview.data.{BasicData, CommandData}
import com.kos.svgpreview.files.AndroidFileUtils
import com.kos.svgpreview.fragments.handlers.{PreviewSvgHandler, SvgHandlerResult}
import com.kos.svgpreview.parser.XmlView
import com.kos.svgpreview.parser.graphics.TypedArrayUtils
import com.kos.svgpreview.parser.svg.SvgToDrawableConverter
import org.xmlpull.v1.XmlPullParserFactory

import java.io._
import scala.util.Try

/**
  * Created by Kos on 23.09.2016.
  */
object PreviewPageFragment {

	val KEY_FILE_NAME = "key_file_name"
	val KEY_COMMAND_NAME = "key_command_name"


	def apply(basicData: BasicData): PreviewPageFragment = {

		val fragment = new PreviewPageFragment()
		val args: Bundle = new Bundle()
		args.putString(KEY_FILE_NAME, basicData.getUri.toString)
		args.putInt(KEY_COMMAND_NAME, basicData.getCommand)

		fragment.setArguments(args)
		fragment
	}
}

class PreviewPageFragment extends SFragment with OnClickListener {

	import InfoActivity._
	import PreviewPageFragment._


	override def onClick(v: View): Unit = {
		v.getId match {
			case R.id.previewInBrowserBtn ⇒
				v.getTag match {
					case s: Uri ⇒
						Try {

							val intent = new Intent(context, classOf[BrowserActivity])
							intent.setData(s)
							//intent.putExtra(DATA_FOLDER_NAME, s)

							startActivity(intent)
						}
					case _ ⇒
				}

			case R.id.createVectorBtn ⇒
				v.getTag match {

					case s: Uri ⇒
						v.setVisibility(View.INVISIBLE)
						getActivity match {
							case x: IBusCommand ⇒ x.openFromCommand(s.getPath, BasicData.COMMAND_CREATE_ALL_SVG, true)
							case _ ⇒
						}

					case _ ⇒
				}

			case R.id.fonWhite ⇒ setBackColor(0xffffffff)
			case R.id.fonBlack ⇒ setBackColor(0xff000000)
			case R.id.fonRed ⇒ setBackColor(0xffff0000)
			case R.id.fonBlue ⇒ setBackColor(0xff0000ff)
			case R.id.fonGreen ⇒ setBackColor(0xff00ff00)
			case _ ⇒
		}

	}
//
//	def loadBackColor(): Unit ={
//		Option(find[View](R.id.backgroundImages)).foreach { v ⇒
//			val sPref = getActivity.getSharedPreferences(InfoActivity.PREFERENCE, Context.MODE_PRIVATE)
//			val color = sPref.getInt(PREVIEW_COLOR, 0xffffffff)
//			v.setBackgroundColor(color)
//		}
//	}
//
	def setBackColor(color: Int): Unit = {
		find[View](R.id.backgroundImages).setBackgroundColor(color)

		val sPref = getActivity.getSharedPreferences(InfoActivity.PREFERENCE, Context.MODE_PRIVATE)
		val ed = sPref.edit
		ed.putInt(PREVIEW_COLOR, color)
		ed.commit()
	}



	//	override def onCreate(savedInstanceState: Bundle) {
	//		super.onCreate(savedInstanceState)
	//		setHasOptionsMenu(true)
	//	}


	override def onStart(): Unit = {
		super.onStart()
	//	loadBackColor()
	}

	override def onResume(): Unit = {
		super.onResume()

	}


	override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
		//	super.onCreateView(inflater, container, savedInstanceState)

		Try {
			val args = getArguments
			if (args != null) {


				val s = getArguments.getString(KEY_FILE_NAME)
				val fileUri = if (s != null) {
					Uri.parse(s)
				} else {
					Uri.parse("")
				}

				val command = getArguments.getInt(KEY_COMMAND_NAME, 0)

				if (command == 0) {
					if (fileUri.getScheme.equalsIgnoreCase("file")) {
						val fileName = fileUri.getPath


						val file = new BasicData(new File(fileName))

						if (file.isDirectory) {
							if (command == 11) {

							}
							val view = inflater.inflate(R.layout.layout_list, container, false)

							loadFolder(view, file.getPath)
							view
						} else if (file.isXml) {
							val view = inflater.inflate(R.layout.layout_preview, container, false)

							openXml(view, new FileInputStream( file.file))
							view
						} else if (file.isSvg) {
							val view = inflater.inflate(R.layout.layout_svg_preview, container, false)

							openSvg(view,fileUri)
							view
						} else if (file.isImage) {
							val view = inflater.inflate(R.layout.item_sign, container, false)

							openImage(view, new FileInputStream(file.file))
							view
						} else if (file.isText) {
							val view = inflater.inflate(R.layout.layout_file_preview, container, false)

							openText(view, new FileInputStream(file.file))
							view
						} else if (file.isWeb) {
							val view = inflater.inflate(R.layout.layout_browse_preview, container, false)

							openWeb(view, file.file)
							view
						} else {
							val view = inflater.inflate(R.layout.layout_unknown_preview, container, false)
							//		val text = find[TextView](view, R.id.text)
							//		text.setText(file.getName)
							view
						}
					}else{

						val inputPFD = getActivity.getContentResolver.openFileDescriptor(fileUri, "r")
						val mimeType = getActivity.getContentResolver.getType(fileUri)
						if (mimeType==null){
							inflater.inflate(R.layout.layout_unknown_preview, container, false)
						}else {
							mimeType.split('/') match {
								case Array(x, "svg+xml") ⇒
									val view = inflater.inflate(R.layout.layout_svg_preview, container, false)

									openSvg(view, fileUri)
									view
								case Array(x, "xml") ⇒
									val view = inflater.inflate(R.layout.layout_preview, container, false)

									openXml(view, new FileInputStream(inputPFD.getFileDescriptor))
									view
								case Array("text", y) ⇒
									val view = inflater.inflate(R.layout.layout_file_preview, container, false)

									openText(view, new FileInputStream(inputPFD.getFileDescriptor))
									view
								case Array("image", y) ⇒
									val view = inflater.inflate(R.layout.item_sign, container, false)

									openImage(view, new FileInputStream(inputPFD.getFileDescriptor))
									view
								case _ ⇒
									inflater.inflate(R.layout.layout_unknown_preview, container, false)
							}
						}

					}
				} else {
					command match {
						case BasicData.COMMAND_ALL_SVG ⇒
							val view = inflater.inflate(R.layout.layout_browse_preview, container, false)
							if (fileUri.getScheme.equalsIgnoreCase("file")) {
								val fileName = fileUri.getPath
								openWeb(view, fileName, command)
							}
							view
						case _ ⇒
							val view = inflater.inflate(R.layout.layout_unknown_preview, container, false)
							//		val text = find[TextView](view, R.id.text)
							//		text.setText(file.getName)
							view
					}
				}
			} else {
				inflater.inflate(R.layout.layout_file_preview, container, false)
			}

		}.getOrElse(
				inflater.inflate(R.layout.layout_file_preview, container, false)
		)


	}


	override def onViewCreated(view: View, savedInstanceState: Bundle): Unit = {
		super.onViewCreated(view, savedInstanceState)
		Array(R.id.fonWhite, R.id.fonBlack, R.id.fonRed, R.id.fonBlue, R.id.fonGreen).
			map(view.findViewById[View]).
			filter(_!=null).
			foreach(_.setOnClickListener(this))
	}

	def openText(view: View, f: FileInputStream): Unit = {

		val textXml = find[TextView](view, R.id.text)


		AndroidFileUtils.tryFile[BufferedReader](new BufferedReader(new InputStreamReader(f, "utf-8")), {
			reader ⇒
				val sb = new StringBuilder()
				var s = reader.readLine()
				while (s != null) {
					sb.append(s)
					sb.append("\n")

					s = reader.readLine()
				}
				textXml.setText(sb.toString())
		})
	}

	def openWeb(view: View, f: File): Unit = {

		val webView = find[WebView](view, R.id.webView)

		setupWebView(webView)
		webView.loadUrl("file://" + f.getAbsolutePath)

	}

	def openWeb(view: View, f: Uri): Unit = {

		val webView = find[WebView](view, R.id.webView)

		setupWebView(webView)
		webView.loadUrl(f.toString)

	}

	def openWeb(view: View, fileName: String, command: Int): Unit = {

		import scalatags.Text.all._

		val webView = find[WebView](view, R.id.webView)

		val f = new File(fileName)

		val list = AndroidFileUtils.svgFilesList(f)


		val htmlPage = html(
			head(
				meta(
					httpEquiv := "Content-Type",
					content := "text/html; charset=utf-8"
				),
				raw(
					"<style type=\"text/css\">\n" +
					".AllSvg-sign {\n      width: 160px;\n      height: auto;\n      margin: 4px;\n    }\n" +
					".AllSvg-btn {\n      height: 36px;\n      background-color: #ff9800;\n      padding: 8px;\n      margin: 16px;\n      color: white;\n      text-align: center;\n      vertical-align: middle;\n    }\n" +
					"</style>"
				)
		//		AllSvg.render[TypedTag[String]]
			),
			body(
				textAlign.center,
				div(
					//AllSvg.btn,
					cls:= "AllSvg-btn",
					a(href := s"svg:${BasicData.COMMAND_CREATE_ALL_SVG}:${f.getAbsolutePath}",
						color.white,
						getString(R.string.createAllSvg)
					)
				),
				br(),
				list.map { x ⇒
					a(href := s"svg:0:${x.getAbsolutePath}",
						img(
							//AllSvg.sign,
							cls:="AllSvg-sign",
							src := s"${x.getAbsolutePath}"
						)
					)
				}
			)
		).toString()
		//Log.d("Kos",htmlPage)

		setupWebView(webView)
		webView.loadDataWithBaseURL("file:///", htmlPage, "text/html", "UTF-8", null)
	}

	def setupWebView(webView: WebView): Unit = {
		val webSettings = webView.getSettings
		webSettings.setBuiltInZoomControls(true)
		webSettings.setDisplayZoomControls(false)
		webSettings.setSupportZoom(true)
		webSettings.setAllowFileAccess(true)

		//		webSettings.setAllowContentAccess(true)
		webSettings.setDefaultTextEncodingName("utf-8")
		webView.setWebViewClient(new myWebViewClient)


		webView.setBackgroundColor(Color.TRANSPARENT)
	}


	def openXml(view: View, f: FileInputStream): Unit = {

		val handler = new PreviewSvgHandler(this)
		val t = new Thread(new Runnable {

			override def run(): Unit = {

				var res: SvgHandlerResult = SvgHandlerResult(new File(""), null)

				AndroidFileUtils.tryFile[BufferedReader](new BufferedReader(new InputStreamReader(f, "utf-8")), {
					reader ⇒

						val factory = XmlPullParserFactory.newInstance();
						// включаем поддержку namespace (по умолчанию выключена)
						factory.setNamespaceAware(true);

						// создаем парсер

						val xpp = factory.newPullParser();
						xpp.setInput(reader) // даем парсеру на вход Reader
						//	val t = System.currentTimeMillis()


						val vectorState = XmlView.parse(xpp)
						//		val d = System.currentTimeMillis() - t
						//	Log.d("Kos", "time " + f.getName + " " + d)

						res = SvgHandlerResult(new File(""), vectorState)
					//		svgHolder.bind(null, vectorState)

				})
				//svgHolder.bind(f, null)
				handler.sendMessage(handler.obtainMessage(1, res))
			}
		})
		t.start()

		//	val svgHolder = new SvgHolder(view)


	}


	def createConvertBtn(view: View, uri: Uri): Unit ={
		val btn = find[View](view, R.id.previewInBrowserBtn)

		btn.setTag(uri)
		btn.setOnClickListener(this)
		val createBtn = find[View](view, R.id.createVectorBtn)
		if (uri.getScheme.equalsIgnoreCase("file")) {
			createBtn.setTag(uri)
			createBtn.setOnClickListener(this)
		}else{
			createBtn.setVisibility(View.INVISIBLE)
		}
	}


	def openSvg(view: View, uri: Uri): Unit = {
		createConvertBtn(view,uri)


		val handler = new PreviewSvgHandler(this)
		val t = new Thread(new Runnable {

			override def run(): Unit = {
				var res: SvgHandlerResult = SvgHandlerResult(new File(""), null)
				try {
					val fd = getActivity.getContentResolver.openFileDescriptor(uri, "r").getFileDescriptor
					val sb = SvgToDrawableConverter.convert(fd)

					val factory = XmlPullParserFactory.newInstance()
					// включаем поддержку namespace (по умолчанию выключена)
					factory.setNamespaceAware(true)

					// создаем парсер
					val xpp = factory.newPullParser()
					xpp.setInput(new StringReader(sb.toString())) // даем парсеру на вход Reader
					val vectorState = XmlView.parse(xpp)

					res = SvgHandlerResult(new File(""), vectorState)

				}catch {
					case _ :Throwable ⇒
				}
				handler.sendMessage(handler.obtainMessage(1, res))
			}
		})
		t.start()
	}

	def openImage(view: View, f: FileInputStream): Unit = {

		val smallImage = find[ImageView](view, R.id.smallImage)
		val appImage = find[ImageView](view, R.id.appImage)
		val realImage = find[ImageView](view, R.id.realImage)
		val imageXml = find[ImageView](view, R.id.image)
		val textXml = find[TextView](view, R.id.textXml)


		try {
			val options = new BitmapFactory.Options()
			options.inJustDecodeBounds = true
			BitmapFactory.decodeStream(f, null,options)


			val size = (360 * getResources.getDisplayMetrics.density).toInt
			options.inSampleSize = TypedArrayUtils.calculateInSampleSize(options, size, size)
			options.inJustDecodeBounds = false

			val myBitmap =	BitmapFactory.decodeStream(f, null,options)
			smallImage.setImageBitmap(myBitmap)
			appImage.setImageBitmap(myBitmap)
			realImage.setImageBitmap(myBitmap)
			imageXml.setImageBitmap(myBitmap)
			textXml.setText("")
		} catch {
			case x: OutOfMemoryError ⇒
			case x: Throwable ⇒
				x.printStackTrace()
		}


	}


	//===================================

	def loadFolder(view: View, folder: String): Unit = {
		if (folder == null)
			return

		val list = find[RecyclerView](view, R.id.list)

		val folderList = AndroidFileUtils.loadListWithCommand(new File(folder), context)
		if (folderList.isEmpty) {
			val blockText = find[TextView](view, R.id.blockText)
			blockText.setVisibility(View.VISIBLE)
		} else {
			val adapter = new NavFileAdapter(context,
				new OnClickListener {
					override def onClick(v: View): Unit = {
						v.getTag match {
							case f: CommandData =>
								runCommand(f)

							case f: BasicData => openFolder(f)
							case _ =>
						}
					}
				}
				, folderList)


			list.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false))
			list.setAdapter(adapter)
		}
	}

	def runCommand(f: CommandData) = {
		openFolder(f)
	}

	def openFolder(f: BasicData): Unit = {
		if (f.getCommand != 0) {
			val intent = new Intent(context, classOf[PreviewActivity])
			intent.putExtra(DATA_FOLDER_NAME, f.getPath)
			intent.putExtra(DATA_COMMAND_NAME, f.getCommand)
			startActivity(intent)
		} else {
			if (f.isDirectory) {
				val intent = new Intent(context, classOf[MainActivity])
				intent.putExtra(DATA_FOLDER_NAME, f.getPath)
				startActivity(intent)
			} else {
				val intent = new Intent(context, classOf[PreviewActivity])
				intent.putExtra(DATA_FOLDER_NAME, f.getPath)
				startActivity(intent)

			}
		}

	}

	private class myWebViewClient extends WebViewClient {

		def scrollListTo(basicData: String) = {

			val basics = basicData.split(":")
			if (basics.length >= 2) {
				val command = Try(basics(0).toInt).getOrElse(0)
				val fileName = basics(1)


				getActivity match {
					case x: IBusCommand ⇒ x.openFromCommand(fileName, command, true)
					case _ ⇒
				}
			}
		}

		override def shouldOverrideUrlLoading(view: WebView, url: String): Boolean = {
			if (url.startsWith("svg:")) {
				scrollListTo(Uri.decode(url.drop(4)))
			}
			else {
				view.loadUrl(url)
			}
			true
		}
	}

}
