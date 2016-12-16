package com.kos.common.helpers;

import android.os.Build;

/**
 * Created by Lenovo-PC on 15.06.14.
 * Вспомогательный класс для получения информации об устройстве
 */
public class DeviceHelper {
	// получить версию Аndroid
	public static String getAndroidVersion() {
		return Build.VERSION.RELEASE;
	}

	// получить название устройства
	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}


	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}
}
