package com.kos.svgpreview.adapters

import java.io.{File, FilenameFilter}

import android.app.{Fragment, FragmentManager}
import android.content.Context
import android.support.v13.app.FragmentPagerAdapter
import com.kos.svgpreview.R
import com.kos.svgpreview.data.{BasicData, CommandData}
import com.kos.svgpreview.files.AndroidFileUtils
import com.kos.svgpreview.fragments.PreviewPageFragment


/**
  * Created by Kos on 23.09.2016.
  */
class FilePagerAdapter(context:Context,fm: FragmentManager,fileList:Seq[BasicData]) extends FragmentPagerAdapter(fm) {


	private[this] var list :Seq[BasicData] = fileList

	def itemIndex(f: File,command:Int): Int = {
		list.indexWhere(x ⇒ x.file==f && x.getCommand==command)
	}

	val mCount=list.length

	override def getItem(position: Int): Fragment = {
		PreviewPageFragment(list(position).getPath,list(position).getCommand)
	}

	override def getCount: Int = {
		mCount
	}

	override def getPageTitle(position: Int): CharSequence = {
		list(position).getName
	}

	def update(newList: Seq[BasicData]):Boolean = {
		if (newList!=null) {
			if (list != newList) {
				list = newList
				notifyDataSetChanged()
				return true
			}
		}
		false
	}
}
