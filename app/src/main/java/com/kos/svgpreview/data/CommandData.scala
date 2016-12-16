package com.kos.svgpreview.data

import java.io.File

/**
  * Created by Kos on 15.09.2016.
  */
class CommandData(file:File,val command:Int,val commandName:String) extends BasicData(file){

	override def getName = commandName

	override def getCommand = command
}
