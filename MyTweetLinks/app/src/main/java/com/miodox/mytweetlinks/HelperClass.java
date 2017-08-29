package com.miodox.mytweetlinks;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.miodox.mytweetlinks.Models.MyTweet;
import com.miodox.mytweetlinks.Models.SinceMaxidModel;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.UrlEntity;
import com.twitter.sdk.android.core.models.UserEntities;
import com.twitter.sdk.android.core.services.StatusesService;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;

import static android.content.ContentValues.TAG;

/**
 * Created by somananda on 8/23/2017.
 */

public class HelperClass {

    public static int tsize=0;
    private static ProgressDialog mProgress;



    public static List<String> extractUrls(String text)
    {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):(?://|\\\\\\\\\\\\\\\\)+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find())
        {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }



    public static String timeFormatter(String input)
    {
        Date date = null;
        try {
            date = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH).parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat formatter = new SimpleDateFormat("h:mm a dd/MM/yyyy");
        return  formatter.format(date);
    }


    //get current time from the device
    public static String getCurrentTime(int prevDay)
    {

        Timestamp tt=new Timestamp(System.currentTimeMillis());

        java.sql.Date date = new java.sql.Date(tt.getTime()-prevDay);
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        return  formatter.format(date);

    }




//will use as a key in database to store links of a particular date
    public static String dateFormatterForKey(String input)
    {
        Date date = null;
        try {
            date = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH).parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        return  formatter.format(date);
    }



    public static void fetchtweets(final Context context)
    {

        mProgress = ProgressDialog.show(context, "",
                "Please wait...", true);

        long maxid=Pref.getMaxId(context);
      //  long sinceid=Pref.getSinceId(context);

        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();
        Call<List<Tweet>> call = statusesService.homeTimeline(200,null, maxid, null, null, true, true);
        call.enqueue(new Callback<List<Tweet> >() {
            @Override
            public void success(Result<List<Tweet>> result) {


                //object to store list of all  links ,timeStamp,twitterid
                List<MyTweet> mainlist=new ArrayList<MyTweet>();
                List<String> timeKey=new ArrayList<String>();
                Set<String> set = new HashSet<String>();

                long newmaxid=-1;
                List<Tweet> t= (List<Tweet>) result.data;

                long sinceid=Pref.getSinceId(context);


                for(int i=0;i<t.size();i++) {

                    Tweet temptweet=t.get(i);


                    List<UrlEntity> urlEntityList=temptweet.entities.urls;
                    String temptweetTime= timeFormatter(temptweet.createdAt);
                    String tempTimeKey=dateFormatterForKey(temptweet.createdAt);

                    //only store unique date key
                    set.add(tempTimeKey);

                    for(UrlEntity tempUrlEntity:urlEntityList)
                    {
                        MyTweet mtobj=new MyTweet(temptweet.id*(-1),temptweet.user.name,temptweetTime
                                ,tempUrlEntity.expandedUrl,temptweet.user.profileImageUrl,
                                temptweet.user.screenName);
                        mainlist.add(mtobj);
                        timeKey.add(tempTimeKey);

                    }



                    newmaxid =t.get(i).id;
                }
                if(newmaxid!=-1)
                {
                    --newmaxid;
                    Pref.setMaxId(context,newmaxid);

                }

                //call save function to save links to database
           saveLinksToFirebase(mainlist,timeKey,set,context);


            }

            public void failure(TwitterException exception) {
                //Do something on failure
                Log.w(TAG, "createUserWithEmail:failure", exception);
                mProgress.cancel();
            }
        });
    }


    public static  void saveLinksToFirebase(final List<MyTweet> ttweets, List<String> ttimeKey, Set<String> set, final Context context)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String useruid=user.getUid();


         DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();


        HashMap<String,MyTweet> mp=new HashMap<String,MyTweet>();

        tsize=0;

        //if size is zero then cancel progressdialog
        if(ttweets.size()==0)
        {
            mProgress.cancel();
            Toast.makeText(context, ttweets.size()+" shared links found ", Toast.LENGTH_SHORT).show();
        }

        for(int i=0;i<ttweets.size();i++)
        {

            mDatabase.child(useruid).child("AllTweets").child(ttimeKey.get(i))
                    .push().setValue(ttweets.get(i), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError==null)
                    {
                      ++tsize;

                        if(tsize==ttweets.size())
                        {
                            mProgress.cancel();
                            Toast.makeText(context, ttweets.size()+" shared links found ", Toast.LENGTH_SHORT).show();

                        }

                    }

                }
            });

        }

        for(String str :set)
        {
            mDatabase.child(useruid).child("AllDate").child(str).setValue(str);
        }


        DatabaseReference mmDatabase = FirebaseDatabase.getInstance()
                .getReference();

        Long sid=Pref.getSinceId(context);
        Long mid=Pref.getMaxId(context);

        SinceMaxidModel temp1=new SinceMaxidModel(sid,mid);
        mmDatabase.child(useruid).child("AllId").setValue(temp1);


    }



}
