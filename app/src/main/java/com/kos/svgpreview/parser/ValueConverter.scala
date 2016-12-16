package com.kos.svgpreview.parser

import scala.util.Try

/**
  * Created by Kos on 12.09.2016.
  */
object ValueConverter {
	def ti(intString: String) = {
		if (null == intString || intString.isEmpty)
			0
		else
			Try(intString.trim.toInt).getOrElse(0)
	}

	def tf(floatString: String): Float = {
		if (null == floatString || floatString.isEmpty)
			0
		else
			Try(floatString.trim.toFloat).getOrElse(0f)
	}

	def tf(floatString: String, defaultValue: Float): Float = {
		if (null == floatString || floatString.isEmpty)
			defaultValue
		else
			Try(floatString.trim.toFloat).getOrElse(defaultValue)
	}

	def hex(value:Int): String ={
		value.formatted("#%08x")
	}

	/**
	  *
	  * Извлечь из строки значения разделённые запятыми, ничиная с позиции startPos  и заканчивая закрывающей скобкой ")"
	  *
	  * @param text     текст из которого извлекаем значения
	  * @param startPos символ с которого начинаем извлекать
	  * @return список извлечённных строк
	  */
	def extractArray(text: String, startPos: Int, separator: Char = ','): (Array[String], Int) = {
		if (startPos < text.length) {
			val end = text.indexOf(')', startPos)
			if (end >= 0) {
				return text.substring(if (text(startPos) == '(') startPos + 1 else startPos, end).split(separator) → (end + 1)
			}
		}
		Array[String]() → text.length
	}
}
