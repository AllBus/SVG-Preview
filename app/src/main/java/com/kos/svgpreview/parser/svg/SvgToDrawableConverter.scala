package com.kos.svgpreview.parser.svg

import java.io.{BufferedWriter, File, FileDescriptor, FileWriter, FilenameFilter}
import java.text.{DecimalFormat, NumberFormat}
import java.util.Locale

import com.kos.svgpreview.parser.svg.CssStyleObject.CssStyle
import com.kos.svgpreview.parser.{ColorSet, ValueConverter}
import ValueConverter._
import CssStyleObject._
import android.net.Uri
import com.kos.svgpreview.parser.graphics.{PathDataNode, PathParser}

import scala.collection.mutable
import scala.util.Try
import scala.xml.{Elem, Node, XML}

/**
  * Created by Kos on 09.09.2016.
  */
object SvgToDrawableConverter {

	//type CssStyle=Map[String,String]

	val vectorHead = "<vector xmlns:android=\"http://schemas.android.com/apk/res/android\" "
	val vectorEnd = "</vector>"

	def vectorBegin(widthR: String, heightR: String) = {

		var widthS = widthR
		var heightS = heightR

		if (widthS.endsWith("px") || widthS.endsWith("pt") || widthS.endsWith("cm") || widthS.endsWith("mm") || widthS.endsWith("dp"))
			widthS = widthS.dropRight(2)

		if (heightS.endsWith("px") || heightS.endsWith("pt") || heightS.endsWith("cm") || heightS.endsWith("mm") || heightS.endsWith("dp"))
			heightS = heightS.dropRight(2)

		val width = Try(widthS.toFloat).getOrElse(24f).toInt
		val height = Try(heightS.toFloat).getOrElse(24f).toInt

		vectorHead +
			"android:width=\"" + width + "dp\"\n" +
			"android:height=\"" + height + "dp\"\n" +
			"android:viewportWidth=\"" + widthS + "\"\n" +
			"android:viewportHeight=\"" + heightS + "\">\n"
	}


	def getColor(color: String, opacity: Float) = ColorSet.colorValue(color, opacity)

	def getSize(size: String, tekTrans: NodeTransform) = {
		val s = size.trim
		formatter(tekTrans.transformScale(tf(if (s.endsWith("px")) s.dropRight(2) else s)))

	}

	def getAttrFillType(value: String): String = value match {
		case "nonzero" ⇒ "android:fillType=\"" + "nonZero" + "\""
		case "evenodd" ⇒ "android:fillType=\"" + "evenOdd" + "\""
		case _ ⇒ "android:fillType=\"" + value + "\""
	}

	def getAttrOpacity(attrName: String, value: String, tekNodeProp: NodeAttribute): String = value match {
		case "1" ⇒ ""
		case _ ⇒ "android:" + attrName + "Alpha=\"" + tf(value) + "\""
	}

	def strokeVisible(attrSet: Map[String, String]): Boolean = {
		val strokeText = attrSet.getOrElse("stroke", "")
		strokeText.nonEmpty && getColor(strokeText, 1) != 0

	}

	def getAttrs(attrLine: CssStyle, tekTrans: NodeTransform, tekNodeProp: NodeAttribute) = {
		attrLine.collect {
			case ("fill", value) ⇒
				" android:fillColor=\"" + hex(getColor(value, tekNodeProp.opacity)) + "\""
			case ("stroke", value) if strokeVisible(attrLine) ⇒
				" android:strokeColor=\"" + hex(getColor(value, tekNodeProp.opacity)) + "\""
			case ("stroke-width", value) if strokeVisible(attrLine) ⇒
				" android:strokeWidth=\"" + getSize(value, tekTrans) + "\""
			case ("stroke-miterlimit", value) if strokeVisible(attrLine) ⇒
				"android:strokeMiterLimit=\"" + getSize(value, tekTrans) + "\""
			case ("stroke-linecap", value) ⇒
				"android:strokeLineCap=\"" + value.trim + "\""
			case ("stroke-linejoin", value) ⇒
				"android:strokeLineJoin=\"" + value.trim + "\""
			case ("stroke-opacity", value) if strokeVisible(attrLine) ⇒
				getAttrOpacity("stroke", value.trim, tekNodeProp)
			case ("fill-opacity", value) ⇒
				getAttrOpacity("fill", value.trim, tekNodeProp)

			//todo: need api24
			//			case ("fill-rule",value) ⇒
			//				getAttrFillType(value.trim)

		}
	}

	def validName(text: String): String = {
		val li = text.lastIndexOf('.')

		Translit.convert(if (li > 0) text.take(li) else text).toLowerCase.replace('.', '_')
	}


	val locale = new Locale("en", "UK")
	val format = NumberFormat.getNumberInstance(locale).asInstanceOf[DecimalFormat]
	format.applyPattern("0.##")


	def formatter(value: Float): String = {
		format.format(value)
	}


	def deconstructPathM(pathNodes: Array[PathDataNode]): Array[PathDataNode] = {
		pathNodes.flatMap { node ⇒
			node.`type` match {
				case 'm' if node.params.length > 2 ⇒
					Seq(new PathDataNode('m', node.params.take(2)),
						new PathDataNode('l', node.params.drop(2)))
				case 'M' if node.params.length > 2 ⇒
					Seq(new PathDataNode('M', node.params.take(2)),
						new PathDataNode('L', node.params.drop(2)))
				case _ ⇒ Seq(node)
			}
		}
	}

	def getPath(pathData: String, tekTrans: NodeTransform): String = {
		val pathNodes = deconstructPathM(PathParser.createNodesFromPathData(pathData))
		if (pathNodes.nonEmpty) {

			val ph = pathNodes.head
			if (ph.`type` == 'm')
				ph.`type` = 'M'
		}
		printPath(pathNodes, tekTrans)

	}


	def printPath(pathNodes: Array[PathDataNode], tekTrans: NodeTransform): String = {
		val sb = new StringBuilder()

		pathNodes.foreach { node ⇒
			sb.append(node.`type`)

			val values: Array[Float] = node.`type` match {

				case 'Z' ⇒
					Array[Float]()
				case 'M' | 'L' | 'T' | 'C' | 'S' | 'Q'
				⇒
					node.params.grouped(2).map(xy ⇒ tekTrans.transform(xy(0), xy(1))).flatten.toArray

				//     incr = 2;
				case 'm' | 'l' | 't' | 'c' | 's' | 'q' ⇒
					node.params.grouped(2).map(xy ⇒ tekTrans.transformR(xy(0), xy(1))).flatten.toArray
				case 'H' ⇒
					//todo: need transform
					node.params.map(_ + tekTrans.x)

				case 'V' ⇒
					//todo: need transform
					node.params.map(_ + tekTrans.y)
				//   incr = 1;
				case 'h' ⇒
					node.params.map(tekTrans.transformRX(_, 0))

				case 'v' ⇒
					node.params.map(tekTrans.transformRY(0, _))

				case 'A' ⇒
					node.params.grouped(7).map(xy ⇒ Array(
						tekTrans.transformSX(xy(0), xy(1)),
						tekTrans.transformSY(xy(0), xy(1)),
						xy(2), xy(3), xy(4),
						tekTrans.transformX(xy(5), xy(6)),
						tekTrans.transformY(xy(5), xy(6))
					)).flatten.toArray

				case 'a' ⇒
					node.params.grouped(7).map(xy ⇒ Array(
						tekTrans.transformSX(xy(0), xy(1)),
						tekTrans.transformSY(xy(0), xy(1)),
						xy(2), xy(3), xy(4),
						tekTrans.transformRX(xy(5), xy(6)),
						tekTrans.transformRY(xy(5), xy(6))
					)).flatten.toArray
				//    incr = 7;

				case _ ⇒
					node.params

			}
			sb.append(values.map(formatter).mkString(","))

		}

		//		var pos=  path.indexOf('M')
		//		while (pos>=0) {
		//
		//
		//			pos = path.indexOf('M', pos+1)
		//		}
		//
		//

		sb.result()
	}


	def convert(fd: FileDescriptor): StringBuilder = {
		convertXml(XML.loadFile(fd))
	}


	def convert(file: File): StringBuilder = {
		convertXml(XML.loadFile(file))
	}

	def convertXml(xml:Elem): StringBuilder = {

		val svg = xml \\ "svg"

		val styleMap = mutable.Map[String, CssStyle]()

		val width = svg \@ "width"
		val height = svg \@ "height"
		val viewBox = (svg \@ "viewBox").split(" ").filter(_.nonEmpty)

		val sb = new StringBuilder()

		if (viewBox.length == 4) {
			sb.append(vectorBegin(viewBox.lift(2).getOrElse("24"), viewBox.lift(3).getOrElse("24")))
		} else {
			sb.append(vectorBegin(width, height))
		}


		def construct(main: Node, transform: NodeTransform, nodeProp: NodeAttribute, style: CssStyle) {

			main.child.foreach { node ⇒

				val fs = fullPathAttributes(node, styleMap)
				val attrsLine = style ++ fs

				var tekTrans = transform
				//	attrsLine.getOrElse("")
				val tekTransformText = fs.getOrElse("transform", "")
				val tekNodeProp = NodeAttribute(nodeProp.opacity * tf(fs.getOrElse("opacity", ""), 1))


				if (tekTransformText.nonEmpty) {

					var pred = 0
					var p = tekTransformText.indexOf('(')
					while (p >= 0) {
						val (a, e) = extractArray(tekTransformText, p)

						//	println(s">${tekTransformText.substring(pred, p)}<>${a.mkString(":")}<")

						tekTrans = tekTransformText.substring(pred, p) → a.length match {

							case ("translate", 1) ⇒ tekTrans(tf(a(0)), 0)
							case ("translate", 2) ⇒ tekTrans(tf(a(0)), tf(a(1)))

							case ("matrix", 6) ⇒ tekTrans.mult(tf(a(0)), tf(a(1)), tf(a(2)), tf(a(3)), tf(a(4)), tf(a(5)))

							case ("scale", 1) ⇒ tekTrans.scale(tf(a(0)), tf(a(0)))
							case ("scale", 2) ⇒ tekTrans.scale(tf(a(0)), tf(a(1)))

							case ("rotate", 1) ⇒ tekTrans.rotate(tf(a(0)))
							case ("rotate", 3) ⇒ tekTrans.rotate(tf(a(0)), tf(a(1)), tf(a(2)))

							case ("skewX", 1) ⇒ tekTrans.skewX(tf(a(0)))
							case ("skewY", 1) ⇒ tekTrans.skewY(tf(a(0)))

							case _ ⇒ tekTrans
						}

						pred = e
						p = tekTransformText.indexOf('(', pred)
					} //end while p

				}

				node.label match {

					case "path" ⇒

						val path = node \@ "d"

						if (path.nonEmpty) {
							vectorDrawablePath(sb, attrsLine, tekTrans, tekNodeProp, getPath(path, tekTrans))
						} //end if path

					case "circle" ⇒

						val cx = tf(node \@ "cx")
						val cy = tf(node \@ "cy")
						val r = tf(node \@ "r")

						vectorDrawablePath(sb, attrsLine, tekTrans, tekNodeProp, printPath(svgPathCircle(cx, cy, r), tekTrans))

					case "ellipse" ⇒

						val cx = tf(node \@ "cx")
						val cy = tf(node \@ "cy")
						val rx = tf(node \@ "rx")
						val ry = tf(node \@ "ry")

						vectorDrawablePath(sb, attrsLine, tekTrans, tekNodeProp, printPath(svgPathEllipse(cx, cy, rx, ry), tekTrans))

					case "rect" ⇒

						val x = tf(node \@ "x")
						val y = tf(node \@ "y")
						val h = tf(node \@ "height")
						val w = tf(node \@ "width")
						val rx = tf(node \@ "rx")
						val ry = tf(node \@ "ry")

						vectorDrawablePath(sb, attrsLine, tekTrans, tekNodeProp, printPath(svgPathRect(x, y, w, h, rx, ry), tekTrans))

					case "polygon" ⇒

						val points = node \@ "points"
						vectorDrawablePath(sb, attrsLine, tekTrans, tekNodeProp, getPath("M" + points + "z", tekTrans))

					case "polyline" ⇒

						val points = node \@ "points"
						vectorDrawablePath(sb, attrsLine, tekTrans, tekNodeProp, getPath("M" + points, tekTrans))

					case "line" ⇒

						val x1 = tf(node \@ "x1")
						val y1 = tf(node \@ "y1")
						val x2 = tf(node \@ "x2")
						val y2 = tf(node \@ "y2")

						vectorDrawablePath(sb, attrsLine, tekTrans, tekNodeProp, printPath(svgPathLine(x1, y1, x2, y2), tekTrans))

					case "g" ⇒
						construct(node, tekTrans, tekNodeProp, attrsLine)

					case "style" ⇒
						readStyleTag(styleMap, node.text)

					case _ ⇒
				} //end node

			}
		} //end construct

		svg.foreach(construct(_, NodeTransform.identity, NodeAttribute(1), createStyle("fill:black;")))

		sb.append(vectorEnd)
		sb
	}

	def vectorDrawablePath(sb: StringBuilder, cssStyle: CssStyle, tekTrans: NodeTransform, tekAttribute: NodeAttribute, pathData: String): Unit = {
		sb.append("<path ")
		sb.append(getAttrs(cssStyle, tekTrans, tekAttribute).mkString("", " ", "\n"))
		sb.append("android:pathData=\"" + pathData + "\"")
		sb.append("/>\n")
	}


	def fullPathAttributes(node: Node, styleMap: mutable.Map[String, CssStyle]): CssStyle = {

		val p = styleMap.getOrElse(node.label, Map()) ++
			(node \@ "class").split(' ').flatMap(v ⇒ styleMap.get("." + v)).foldLeft(Map[String, String]())((l, r) ⇒ l ++ r) ++
			styleMap.getOrElse("#" + (node \@ "id"), Map()) ++
			readStyle(node \@ "style") ++
			addAttribute(node)

		p

	}

	def dataNode(kind: Char, params: Float*): PathDataNode = {
		new PathDataNode(kind, params.toArray)
	}

	def dataNodeZ = new PathDataNode('z', Array[Float]())

	def svgPathEllipse(cx: Float, cy: Float, rx: Float, ry: Float): Array[PathDataNode] = {
		Array(
			dataNode('M', cx - rx, cy),
			dataNode('a', rx, ry, 0, 1, 0, 2 * rx, 0),
			dataNode('a', rx, ry, 0, 1, 0, -2 * rx, 0),
			dataNodeZ
		)
	}

	def svgPathCircle(cx: Float, cy: Float, r: Float) = {

		Array(
			dataNode('M', cx, cy),
			dataNode('m', -r, 0),
			dataNode('a', r, r, 0, 1, 1, 2 * r, 0),
			dataNode('a', r, r, 0, 1, 1, -2 * r, 0)
			//	dataNodeZ
		)

	}

	def svgPathLine(x1: Float, y1: Float, x2: Float, y2: Float) = {

		val nodes = Array(
			dataNode('M', x1, y1),
			dataNode('L', x2, y2)
			//	dataNodeZ
		)

		nodes
		//	s"M $cx,$cy m ${-r},0 a $r,$r,0,1,1,${2 * r},0 a $r, $r,0,1,1,${-2 * r},0z"
	}


	def svgPathRect(x: Float, y: Float, w: Float, h: Float, rx: Float, ry: Float) = {


		if (rx == 0 || ry == 0) {
			Array(
				dataNode('M', x, y),
				dataNode('L', x + w, y),
				dataNode('L', x + w, y + h),
				dataNode('L', x, y + h),
				dataNodeZ
			)

			//	s"M$x,$y h$w v $h h ${-w}z"
		} else {

			Array(
				dataNode('M', x + rx, y),
				dataNode('L', x + w - rx, y),
				dataNode('A', rx, ry, 0, 0, 1, x + w, y + ry),
				dataNode('L', x + w, y + h - ry),
				dataNode('A', rx, ry, 0, 0, 1, x + w - rx, y + h),
				dataNode('L', x + rx, y + h),
				dataNode('A', rx, ry, 0, 0, 1, x, y + h - ry),
				dataNode('L', x, y + ry),
				dataNode('A', rx, ry, 0, 0, 1, x + rx, y),
				dataNodeZ
			)

			//	val a = s"a$rx,$ry,0,0,1,"
			//	s"M${x + rx},$y,h${w - 2 * rx} $a$rx,$ry v${h - 2 * ry} $a${-rx},$ry h${-(w - 2 * rx)}$a${-rx},${-ry} v${-(h - 2 * ry)}$a$rx,${-ry}z"
		}

	}

	//======================================

	def writeVectorDrawableFile(sb: StringBuilder, dir:String,sourceFileName: File): Boolean = {
		try {
			val validFileName = validName(sourceFileName.getName)


			val buf = new BufferedWriter(new FileWriter(dir + validFileName + ".xml"))
			try {
				buf.append(sb.toString())
				buf.flush()
			}
			finally {
				buf.close()
			}
			//	println("list.add(new ItemValue(\"" + validFileName + "\",R.drawable." + validFileName + "));")
			true
		} catch {
			case e: Throwable ⇒
				e.printStackTrace()
				//System.err.println(s"Error in ${sourceFileName.getName}")
				false
		}
	}

	def _dirChecker(dir: String):Boolean = {
		val f: File = new File(dir)

		if (!f.isDirectory) {
			f.mkdirs
		}else
			true

	}

	def createDir(folder: File, defaultPath: String) :(String,Int)= {

		if (folder==null){
			val fb = defaultPath + "/drawable/"
			_dirChecker(fb)
			(fb,2)
		}else {
			val fa = folder.getAbsolutePath + "/drawable/"
			if (_dirChecker(fa))
				(fa,1)
			else {
				val fb = defaultPath + "/drawable/"
				_dirChecker(fb)
				(fb,2)
			}
		}
	}

	def convertInPath(filePath: String, defaultPath:String): Int = {
		try {
			val folder = new File(filePath)


			if (folder.isDirectory) {
				val (dir,index) = createDir(folder,defaultPath)


				for (filename ← folder.listFiles(new FilenameFilter {
					override def accept(dir: File, name: String): Boolean = {
						name.toLowerCase.endsWith(".svg")
					}
				})) {

					writeVectorDrawableFile(convert(filename),dir, filename)

				}
				index

			}else {
				val (dir,index) = createDir(folder.getParentFile,defaultPath)
				if (createSvgFile(filePath, dir))
					index
				else
					0
			}

		} catch {
			case e: Throwable ⇒
				0
		}

	}



	def createSvgFile(fileName: String,dir:String): Boolean = {
		try {
			val sourceFile = new File(fileName)

		//	Log.d("Kos","create "+sourceFile.getAbsolutePath)
			if (sourceFile.getName.toLowerCase.endsWith(".svg")) {
				val sb = convert(sourceFile)

				writeVectorDrawableFile(sb,dir, sourceFile)
			}else
				false
		} catch {
			case e: Throwable ⇒
		//		e.printStackTrace()
				false
		}
	}
}
