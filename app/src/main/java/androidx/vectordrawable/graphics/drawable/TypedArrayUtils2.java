/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package androidx.vectordrawable.graphics.drawable;

import org.xmlpull.v1.XmlPullParser;

class TypedArrayUtils2 {
    private static final String NAMESPACE = "http://schemas.android.com/apk/res/android";



    public static boolean hasAttribute(XmlPullParser parser, String attrName) {
        return parser.getAttributeValue(NAMESPACE, attrName) != null;
    }

    public static float getNamedFloat(XmlPullParser parser, String attrName,
                                      int resId, float defaultValue) {
        final boolean hasAttr = hasAttribute(parser, attrName);
        if (!hasAttr) {
            return defaultValue;
        } else {

            return Float.parseFloat( parser.getAttributeValue(NAMESPACE,attrName));
         //   return a.getFloat(resId, defaultValue);
        }
    }

    public static boolean getNamedBoolean( XmlPullParser parser, String attrName,
                                          int resId, boolean defaultValue) {
        final boolean hasAttr = hasAttribute(parser, attrName);
        if (!hasAttr) {
            return defaultValue;
        } else {

            return Boolean.parseBoolean( parser.getAttributeValue(NAMESPACE,attrName));
           // return a.getBoolean(resId, defaultValue);
        }
    }

    public static int getNamedInt( XmlPullParser parser, String attrName,
                                  int resId, int defaultValue) {
        final boolean hasAttr = hasAttribute(parser, attrName);
        if (!hasAttr) {
            return defaultValue;
        } else {
            return Integer.parseInt( parser.getAttributeValue(NAMESPACE,attrName));
        //    return a.getInt(resId, defaultValue);
        }
    }

    public static int getNamedColor(XmlPullParser parser, String attrName,
                                    int resId, int defaultValue) {
        final boolean hasAttr = hasAttribute(parser, attrName);
        if (!hasAttr) {
            return defaultValue;
        } else {
            return defaultValue;
       //     return a.getColor(resId, defaultValue);
        }
    }
}
