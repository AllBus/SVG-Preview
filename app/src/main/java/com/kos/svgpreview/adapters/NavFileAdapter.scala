package com.kos.svgpreview.adapters

import java.io.File

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View.OnClickListener
import android.view.{LayoutInflater, ViewGroup}
import com.kos.svgpreview.R
import com.kos.svgpreview.adapters.holders.NavFileHolder
import com.kos.svgpreview.data.BasicData

/**
  * Created by Kos on 14.09.2016.
  */
class NavFileAdapter(val context:Context,val onClick:OnClickListener,
					private[this] var list:Seq[BasicData]) extends RecyclerView.Adapter[NavFileHolder]{



	private[this] val inflater=LayoutInflater.from(context)

	//private[this] var list= Seq.empty[File]

	def setList(newList:Seq[BasicData]): Unit ={
		if (newList!=list){
			list=newList
			Log.d("Kos","set list")
			notifyDataSetChanged()
		}
	}


	override def getItemCount: Int = {
		list.size
	}

	override def onBindViewHolder(holder: NavFileHolder, position: Int): Unit = {

		holder.bind(list(position),position)
	}

	override def onCreateViewHolder(parent: ViewGroup, viewType: Int): NavFileHolder = {

		new NavFileHolder(inflater.inflate(R.layout.item_folder,parent,false),onClick)
	}
}
