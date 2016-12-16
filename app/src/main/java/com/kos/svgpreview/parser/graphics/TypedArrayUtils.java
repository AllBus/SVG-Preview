package com.kos.svgpreview.parser.graphics;

import android.graphics.BitmapFactory;

import com.kos.svgpreview.parser.ColorSet;

import org.xmlpull.v1.XmlPullParser;

/**
 * Created on 23.09.2016.
 *
 * @author Kos
 */

public class TypedArrayUtils {


	public static final int LINECAP_BUTT = 0;
	public static final int LINECAP_ROUND = 1;
	public static final int LINECAP_SQUARE = 2;

	public static final int LINEJOIN_MITER = 0;
	public static final int LINEJOIN_ROUND = 1;
	public static final int LINEJOIN_BEVEL = 2;

	private static final String NAMESPACE = "http://schemas.android.com/apk/res/android";

	public static boolean hasAttribute(XmlPullParser parser, String attrName) {
		return parser.getAttributeValue(NAMESPACE, attrName) != null;
	}

	public static String getAttribute(XmlPullParser parser, String attrName) {
		return parser.getAttributeValue(NAMESPACE, attrName);
	}

	public static float getNamedFloat(XmlPullParser parser, String attrName,float defaultValue) {

		String attr=getAttribute(parser,attrName);
		if (attr!=null){
			return Float.parseFloat(attr);
		}
		return defaultValue;

	}

	public static int getNamedInt(XmlPullParser parser, String attrName,int defaultValue) {
		String attr=getAttribute(parser,attrName);
		if (attr!=null){
			return Integer.parseInt(attr);
		}
		return defaultValue;
	}

	public static boolean getNamedBoolean(XmlPullParser parser, String attrName,boolean defaultValue) {
		String attr=getAttribute(parser,attrName);
		if (attr!=null){
			return Boolean.parseBoolean(attr);
		}
		return defaultValue;
	}

	public static String getNamedString(XmlPullParser parser, String attrName, String defaultValue) {
		String attr=getAttribute(parser,attrName);
		if (attr!=null){
			return attr;
		}
		return defaultValue;
	}

	public static int getNamedColor(XmlPullParser parser, String attrName, int defaultValue) {
		String attr=getAttribute(parser,attrName);
		if (attr!=null){
			return  ColorSet.colorValue(attr,1);
		}
		return defaultValue;
	}


	public static int getNamedLineCap(XmlPullParser parser, String attrName, int defaultValue) {
		String attr=getAttribute(parser,attrName);
		if (attr!=null) {
			switch (attr) {
				case "butt":
					return LINECAP_BUTT;
				case "round":
					return LINECAP_ROUND;
				case "square":
					return LINECAP_SQUARE;
			}
		}
		return defaultValue;
	}

	public static int getNamedLineJoin(XmlPullParser parser, String attrName, int defaultValue) {
		String attr=getAttribute(parser,attrName);
		if (attr!=null) {
			switch (attr) {
				case "miter":
					return LINEJOIN_MITER;
				case "round":
					return LINEJOIN_ROUND;
				case "bevel":
					return LINEJOIN_BEVEL;
			}
		}
		return defaultValue;
	}

	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) >= reqHeight
					&& (halfWidth / inSampleSize) >= reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
}
