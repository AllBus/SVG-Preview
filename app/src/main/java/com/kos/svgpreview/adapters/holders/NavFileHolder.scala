package com.kos.svgpreview.adapters.holders

import android.view.View
import android.view.View.OnClickListener
import android.widget.{ImageView, TextView}
import androidx.recyclerview.widget.RecyclerView
import com.kos.svgpreview.R
import com.kos.svgpreview.data.BasicData

/**
  * Created by Kos on 14.09.2016.
  */
class NavFileHolder(topView: View, clickListener: OnClickListener) extends RecyclerView.ViewHolder(topView) {

	itemView.setOnClickListener(clickListener)


	def find[T <: View](resId: Int) = itemView.findViewById(resId).asInstanceOf[T]

	private[this] val image = find[ImageView](R.id.image)
	private[this] val text = find[TextView](R.id.text)


	def bind(item: BasicData, position: Int): Unit = {
		itemView.setTag(item)

		image.setImageResource(if (item.getCommand == 0) {
			if (item.isDirectory) R.drawable.ic_folder_open_black_24dp
			else if (item.isXml) R.drawable.ic_code_black_24dp
			else if (item.isImage) R.drawable.ic_image_black_24dp
			else if (item.isSvg) R.drawable.ic_image_black_24dp
			else if (item.isText) R.drawable.ic_insert_drive_file_black_24dp
			else if (item.isWeb	) R.drawable.ic_public_black_24dp
			else
				R.drawable.ic_unknown_file_24dp

		} else if (item.getCommand == BasicData.COMMAND_ALL_SVG) {
			R.drawable.ic_all_media_black_24dp
		} else {
			R.drawable.ic_folder_open_black_24dp
		})
		text.setText(item.getName)

	}

}
