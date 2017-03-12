package com.kos.common.helpers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Lenovo-PC on 21.06.14.
 * Вспомогательный класс для работы с Intent
 */
public class IntentHelper {
    public static final String GOOGLE_MARKET_SCHEME = "market://";
    public static final String GOOGLE_MARKET = GOOGLE_MARKET_SCHEME + "details?";
    public static final String GOOGLE_MARKET_ID_PARAMETER = "id=";
    public static final String GOOGLE_MARKET_HTTPS = "https://play.google.com/store/apps/details?";

    // открыть PlayMarket c или браузер для выбранного приложения
    public static  void openPlayMarket(Activity activity, String Query){
        Intent googleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_MARKET + Query));
        try {
            activity.startActivity(googleIntent);
        }catch (ActivityNotFoundException e){
            // В случае отсутствия PlayMarket, открыть в браузере
            googleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_MARKET_HTTPS + Query));
            activity.startActivity(googleIntent);
        }
    }

    public static void shareText(Context context, String subject, String text, String chooserTitle) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, subject)
                .putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(share, chooserTitle));
    }

    public static void showBrowser(Activity activity, String uri) {
        try {
            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            activity.startActivity(intent);
        }catch (Exception ignored){

        }
    }
}
