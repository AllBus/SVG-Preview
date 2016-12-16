package com.kos.svgpreview.fragments.handlers

import java.io.File

import com.kos.svgpreview.parser.XmlView.VectorDrawableCompatState

/**
  * Created by Kos on 25.09.2016.
  */
case class SvgHandlerResult(val f:File,val vectorState : VectorDrawableCompatState) {

}
