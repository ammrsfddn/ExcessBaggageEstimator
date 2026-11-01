package com.ammar.excessbaggage.util;

import android.content.Context;
import android.content.Intent;

import com.ammar.excessbaggage.R;

/** Opens the Android share sheet with the app link. */
public final class ShareHelper {

    private ShareHelper() { }

    public static void shareApp(Context context) {
        String appName = context.getString(R.string.app_name);
        String message = context.getString(R.string.share_message, appName,
                context.getString(R.string.app_url));

        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, appName);
        send.putExtra(Intent.EXTRA_TEXT, message);

        context.startActivity(Intent.createChooser(send,
                context.getString(R.string.share_chooser_title)));
    }
}