package com.kos.svgpreview.bus;

import androidx.annotation.NonNull;

import com.squareup.otto.Bus;

/**
 * Created by Kos on 02.10.2016.
 */
public class BusProvider {
	private static Bus bus=new Bus();

	public static void register(Object activity){
		bus.register(activity);
	}

	public static void unregister(Object activity){
		bus.unregister(activity);
	}

	public static void post(@NonNull Object event){
		bus.post(event);
	}

}
