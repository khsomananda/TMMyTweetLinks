package com.miodox.mytweetlinks.Activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.HeaderViewListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.miodox.mytweetlinks.Fragments.AllDateFragment;
import com.miodox.mytweetlinks.Fragments.TodayYesterdayFragment;
import com.miodox.mytweetlinks.HelperClass;
import com.miodox.mytweetlinks.Models.MyTweet;
import com.miodox.mytweetlinks.Pref;
import com.miodox.mytweetlinks.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.List;

import retrofit2.Call;

public class ShowTweetActivity extends AppCompatActivity {


    private static final String TAG = "ShowTweetactivity";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private  FloatingActionButton fab;

    private String whichDateSearch=whichDateSelected(0);


    private ViewPager mViewPager;
    //private long maxid =999999999999999999L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tweet);

        setTitle("");


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);




        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);





        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addOldTweetsDialog();

            }
        });


        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                switch (position)
                {
                    case 0:
                        whichDateSearch=whichDateSelected(0);
                        fab.setVisibility(View.INVISIBLE);
                        break;

                    case 1:
                        int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
                        whichDateSearch=whichDateSelected(MILLIS_IN_DAY);
                        fab.setVisibility(View.INVISIBLE);
                        break;

                    case 2:
                        whichDateSearch=null;
                        fab.setVisibility(View.VISIBLE);
                        break;

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {


            }
        });



        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.user_search_view);
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String arg0) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {

                Intent searchIntent = new Intent(getApplicationContext(), SearchableActivity.class);
                searchIntent.putExtra(SearchManager.QUERY, query);
                searchIntent.putExtra("whichDate",whichDateSearch);
                searchIntent.setAction(Intent.ACTION_SEARCH);
                startActivity(searchIntent);
                return true;
            }
        });


    }








    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_tweet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {

            Pref.setMaxId(ShowTweetActivity.this,999999999999999999L);
            Pref.setSinceId(ShowTweetActivity.this,900000000000000000L);
            FirebaseAuth.getInstance().signOut();

            Intent intent=new Intent(ShowTweetActivity.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }

        else if(id==R.id.action_share)
        {
            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "http://play.google.com/store/apps/details?id=com.miodox.mytweetlinks");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
        else if(id==R.id.action_profile)
        {
            startActivity(new Intent(ShowTweetActivity.this,MyProfileActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            String whichDay;
            if(position==0)
            {

                whichDay=whichDateSelected(0);
                return TodayYesterdayFragment.newInstance(whichDay);
            }

              else if(position==1)
            {

                int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
                whichDay=whichDateSelected(MILLIS_IN_DAY);
                return TodayYesterdayFragment.newInstance(whichDay);

            }
            else if(position==2)
            {

                return new  AllDateFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:

                    return "TODAy";
                case 1:

                    return "YESTERDAY";
                case 2:

                    return "ALL DATES";
            }
            return null;
        }
    }



    private String  whichDateSelected(int whichtime)
    {
        return HelperClass.getCurrentTime(whichtime);

    }

    private void addOldTweetsDialog()
    {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ShowTweetActivity.this);
        builder.setMessage("Do you want to scan your old tweets to search for shared links?" +
                "This is useful when you login for first time and want to see all links shared in last 5 days.  " +
                "\nNote.this service can scan upto 800 previous tweets(200/click).")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        HelperClass.fetchtweets(ShowTweetActivity.this);
                    }
                }).setNegativeButton("Cancel",null);
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();


    }
}
