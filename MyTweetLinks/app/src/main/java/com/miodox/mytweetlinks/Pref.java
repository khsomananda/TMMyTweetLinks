package com.miodox.mytweetlinks;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by somananda on 8/24/2017.
 */

public class Pref {


    private static final String TWEET_SINCE_ID = "tweetsinceid";
    private static final String TWEET_MAX_ID = "tweetmaxid";

    //tweets since id
    public static void setSinceId(Context context, long signature)
    {
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor=pref.edit();
        editor.putLong(TWEET_SINCE_ID,signature);
        editor.commit();
    }

    public static long getSinceId(Context context)
    {
        SharedPreferences pref =PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getLong(TWEET_SINCE_ID,900000000000000000L);

    }



//tweets max id
    public static void setMaxId(Context context, long signature)
    {
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor=pref.edit();
        editor.putLong(TWEET_MAX_ID,signature);
        editor.commit();
    }

    public static long getMaxId(Context context)
    {
        SharedPreferences pref =PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getLong(TWEET_MAX_ID,999999999999999999L);

    }

}
