package com.miodox.mytweetlinks.Activity;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.miodox.mytweetlinks.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;

public class MyProfileActivity extends AppCompatActivity {

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String useruid=user.getUid();
    private TextView mName,mFollerw,mFollowing,mBio;
    private CircleImageView mPic;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Profile");

        mName=(TextView)findViewById(R.id.my_profile_user_name);
        mPic=(CircleImageView) findViewById(R.id.my_profile_url);
        mFollerw=(TextView)findViewById(R.id.followerCount);
        mFollowing=(TextView)findViewById(R.id.followingCount);
        mBio=(TextView)findViewById(R.id.userBio);
        mProgressBar=(ProgressBar)findViewById(R.id.my_profile_progressBar);



        mName.setText(user.getDisplayName());

        showProfileDetail();



    }




    private void showProfileDetail()
    {
        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();


        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(session);

        Call<User> userResult = twitterApiClient.getAccountService().verifyCredentials(false,false,false);
        userResult.enqueue(new Callback<User>() {

            @Override
            public void failure(TwitterException e) {

                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void success(Result<User> userResult) {
                mProgressBar.setVisibility(View.INVISIBLE);

                User user = userResult.data;

                try {

                    String tempLink=user.profileImageUrl;
                    tempLink=tempLink.substring(0,tempLink.length()-11);
                    tempLink+=".jpg";
                    Glide.with(MyProfileActivity.this)
                            .load(tempLink)
                            .fitCenter()
                            .into(mPic);
                    Log.v("somlink",tempLink);
                    Log.v("somlink",user.profileImageUrl);
                    if(user.description!=null)
                   mBio.setText("\"    "+user.description+"    \"");
                    else
                        mBio.setText("\"    -    \"");

                    mFollowing.setText(user.friendsCount+" Following");
                    mFollerw.setText(user.followersCount+" Followers");

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

        });

    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
