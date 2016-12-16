package com.kos.svgpreview.threads;

import android.os.AsyncTask;

import com.kos.svgpreview.bus.BusProvider;
import com.kos.svgpreview.bus.BusSvgConvert;
import com.kos.svgpreview.parser.svg.SvgToDrawableConverter;

/**
 * Created by Kos on 02.10.2016.
 */
public class AsynhConvert extends AsyncTask<String,Void,BusSvgConvert> {

	private String defaultPath;

	public AsynhConvert(String defaultPath){
		this.defaultPath=defaultPath;
	}


	@Override
	protected BusSvgConvert doInBackground(String... params) {

		for (String fileName : params) {
			return new BusSvgConvert(SvgToDrawableConverter.convertInPath(fileName,defaultPath),fileName);
		}
		return null;
	}

	@Override
	protected void onPostExecute(BusSvgConvert result) {
		super.onPostExecute(result);
		if (result!=null){
			BusProvider.post(result);
		}
	}
}
