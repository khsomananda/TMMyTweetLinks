package com.miodox.mytweetlinks.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miodox.mytweetlinks.HelperClass;
import com.miodox.mytweetlinks.Models.MyTweet;
import com.miodox.mytweetlinks.Models.SinceMaxidModel;
import com.miodox.mytweetlinks.Pref;
import com.miodox.mytweetlinks.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.UrlEntity;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;

import static com.miodox.mytweetlinks.HelperClass.dateFormatterForKey;
import static com.miodox.mytweetlinks.HelperClass.timeFormatter;

public class MainActivity extends AppCompatActivity {

    private TwitterLoginButton loginButton;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private int tsize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        loginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                handleTwitterSession(result.data);

            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
                errorMsg();
                mProgress.cancel();
            }
        });

    }


    private void errorMsg()
    {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Please make sure you have installed Twitter app on your device (some devices may required Twitter app).")
                .setPositiveButton(android.R.string.ok,null);
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        mProgress = ProgressDialog.show(MainActivity.this, "",
                "Please wait...", true);

        // Pass the activity result to the login button.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }




    private void handleTwitterSession(TwitterSession session) {
       // Log.d(TAG, "handleTwitterSession:" + session);


        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                           // Log.d(TAG, "signInWithCredential:success");
                           getInfo();
                        } else {

                            mProgress.cancel();
                            // If sign in fails, display a message to the user.
                         //   Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }





    void getInfo()
    {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String useruid=user.getUid();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance()
                .getReference(useruid).child("AllId");
       // mDatabase.keepSynced(true);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mProgress.cancel();

                SinceMaxidModel smm=dataSnapshot.getValue(SinceMaxidModel.class);
                if(smm!=null)
                {
                  //  Toast.makeText(MainActivity.this, "null", Toast.LENGTH_SHORT).show();
                    Pref.setMaxId(MainActivity.this,smm.getMaxid());
                    Pref.setSinceId(MainActivity.this,smm.getSinceId());
                }
                fetchtweets();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgress.cancel();

            }
        });

    }



    void nextAct()
    {
        Intent intent=new Intent(MainActivity.this,ShowTweetActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }




    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
            nextAct();
    }









    private void fetchtweets()
    {

        mProgress = ProgressDialog.show(MainActivity.this, "",
                "Please wait...", true);


        long sinceid=Pref.getSinceId(MainActivity.this);

        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();
        Call<List<Tweet>> call = statusesService.homeTimeline(200,sinceid,null, null, null, true, true);
        call.enqueue(new Callback<List<Tweet> >() {
            @Override
            public void success(Result<List<Tweet>> result) {



                //object to store list of all  links ,timeStamp,twitterid
                List<MyTweet> mainlist=new ArrayList<MyTweet>();
                List<String> timeKey=new ArrayList<String>();
                Set<String> set = new HashSet<String>();

                long newmaxid=-1;
                List<Tweet> t= (List<Tweet>) result.data;

                long sinceid=Pref.getSinceId(MainActivity.this);
                long maxid=Pref.getMaxId(MainActivity.this);


                for(int i=0;i<t.size();i++) {


                    Tweet temptweet=t.get(i);
                  //  Log.v("soma",temptweet.id+"\n");
                    if(sinceid<temptweet.id)
                    {
                        sinceid=temptweet.id+100;
                        Pref.setSinceId(MainActivity.this,sinceid);
                    }

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

                if(newmaxid!=-1 && newmaxid<maxid)
                {
                    newmaxid--;
                    Pref.setMaxId(MainActivity.this,newmaxid);
                }

                //call save function to save links to database
                saveLinksToFirebase(mainlist,timeKey,set,MainActivity.this);


            }

            public void failure(TwitterException exception) {
                //Do something on failure
                Log.w("Soma", "createUserWithEmail:failure", exception);
                mProgress.cancel();
            }
        });
    }


    private   void saveLinksToFirebase(final List<MyTweet> ttweets, List<String> ttimeKey, Set<String> set, final Context context)
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
           nextAct();
            mProgress.cancel();
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
                           nextAct();
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
      //  mmDatabase.keepSynced(true);



        Long sid=Pref.getSinceId(MainActivity.this);
        Long mid=Pref.getMaxId(MainActivity.this);
        //Log.v("soma singh",sid+"-"+mid+"\n");

        SinceMaxidModel temp1=new SinceMaxidModel(sid,mid);
        mmDatabase.child(useruid).child("AllId").setValue(temp1);


    }







}
