package com.kos.svgpreview.common

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

/**
  * Created by Kos on 23.09.2016.
  */
class SFragment extends Fragment {
	def find[T <: View](resId: Int) = getView.findViewById(resId).asInstanceOf[T]

	def find[T <: View](view: View, resId: Int) = view.findViewById(resId).asInstanceOf[T]

	def context = getActivity

	def snack(view:View,stringId:Int) ={
		Snackbar.make(view,stringId,Snackbar.LENGTH_SHORT).show()
	}
}
