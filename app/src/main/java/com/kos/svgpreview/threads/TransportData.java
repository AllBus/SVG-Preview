package com.kos.svgpreview.threads;

import com.kos.svgpreview.bus.BusProvider;

/**
 * Created by Kos on 02.10.2016.
 */
public class TransportData {
	public String fileName;

	public TransportData(String fileName) {
		this.fileName = fileName;
	}

	public void complete(Object event){
		BusProvider.post(event);
	}
}
