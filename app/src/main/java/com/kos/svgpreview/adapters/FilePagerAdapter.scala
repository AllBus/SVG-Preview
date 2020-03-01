package com.kos.svgpreview.adapters

import java.io.File

import android.content.Context
import androidx.fragment.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import com.kos.svgpreview.data.BasicData
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

		PreviewPageFragment(list(position))
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
