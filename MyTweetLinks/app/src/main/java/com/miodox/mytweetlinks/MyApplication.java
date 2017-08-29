package com.miodox.mytweetlinks;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

/**
 * Created by somananda on 8/23/2017.
 */

public class MyApplication extends Application {


    public void onCreate() {
        super.onCreate();
        TwitterConfig config = new TwitterConfig.Builder(this)
                //.logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(getResources().getString(R.string.CONSUMER_KEY)
                        , getResources().getString(R.string.CONSUMER_SECRET)))
                .debug(true)
                .build();
        Twitter.initialize(config);

       // FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
