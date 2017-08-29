package com.miodox.mytweetlinks.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miodox.mytweetlinks.Activity.MainActivity;
import com.miodox.mytweetlinks.Adapters.TodYesAdapter;
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
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.UrlEntity;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;

import static android.content.ContentValues.TAG;


public class TodayYesterdayFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    private String mParam1;
   // private String mParam2;
    private RecyclerView mRecyclerView;
    private TodYesAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private DatabaseReference mDatabase;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private ProgressDialog mProgress;
    private int tsize=0;
    private TextView mSwipeDownMessage;
    private int prevCount=0;
    private long offset;
    private int mLastVisibleItemPosition;
    private ProgressBar qaProgressBar;


    public TodayYesterdayFragment() {
        // Required empty public constructor
    }




    public static TodayYesterdayFragment newInstance(String param1) {
        TodayYesterdayFragment fragment = new TodayYesterdayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);

        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView=(ViewGroup) inflater.inflate(R.layout.fragment_today_yesterday, container, false);

        qaProgressBar=(ProgressBar)rootView.findViewById(R.id.load_more_progressBar);
        //hide progress bar when activity start
        qaProgressBar.setVisibility(View.GONE);

        mSwipeDownMessage=(TextView)rootView.findViewById(R.id.swipe_down_message);
        mySwipeRefreshLayout=(SwipeRefreshLayout)rootView.findViewById(R.id.swiperefresh_tod_yes);
        mRecyclerView=(RecyclerView)rootView.findViewById(R.id.today_yesterday_recycler_view);
        mLayoutManager=new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter=new TodYesAdapter(getActivity(),mParam1);
        mRecyclerView.setAdapter(mAdapter);



        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String useruid=user.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference(useruid).child("AllTweets").child(mParam1);
       // mDatabase.keepSynced(true);

        offset=(-1)*Pref.getSinceId(getActivity());
        //show when fragment start
        mySwipeRefreshLayout.setRefreshing(true);
        prevCount=0;
        retrievingData(true);


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //swipe refresh should be idle
                if (mySwipeRefreshLayout.isRefreshing() == false) {
                    mLastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition();
                    if (mLastVisibleItemPosition == mAdapter.getItemCount() - 1) {
                        if (prevCount != mLastVisibleItemPosition) {
                            Log.d("soma", "hurray");
                           qaProgressBar.setVisibility(View.VISIBLE);
                            retrievingData(false);
                        }

                        prevCount = mLastVisibleItemPosition;
                    }
                }
            }
        });



        mySwipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        downloadNewFeed();

                    }
                }
        );

        return rootView;
    }


    //download feed from twitter and upload to database
    void downloadNewFeed()
    {


        long sinceid= Pref.getSinceId(getActivity());

        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();
        Call<List<Tweet>> call = statusesService.homeTimeline(100,sinceid, null, null, null, true, true);
        call.enqueue(new Callback<List<Tweet> >() {
            @Override
            public void success(Result<List<Tweet>> result) {


                //object to store list of all  links ,timeStamp,twitterid
                List<MyTweet> mainlist=new ArrayList<MyTweet>();
                List<String> timeKey=new ArrayList<String>();
                Set<String> set = new HashSet<String>();

                long newsince=-1;
                List<Tweet> t= (List<Tweet>) result.data;


                for(int i=0;i<t.size();i++) {

                    Tweet temptweet=t.get(i);

                    Log.v(TAG,temptweet.id+"\n");
                    if(i==0&&newsince<temptweet.id)
                    {

                        newsince=temptweet.id+100;
                        Pref.setSinceId(getActivity(),newsince);
                    }

                    List<UrlEntity> urlEntityList=temptweet.entities.urls;
                    String temptweetTime= HelperClass.timeFormatter(temptweet.createdAt);
                    String tempTimeKey=HelperClass.dateFormatterForKey(temptweet.createdAt);



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




                }

                //call save function to save links to database
                saveLinksToFirebase(mainlist,timeKey,set);


            }

            public void failure(TwitterException exception) {
                //Do something on failure
                Log.w(TAG, "createUserWithEmail:failure", exception);
                mAdapter.setDataNull();
                offset=-1*Pref.getSinceId(getActivity());
                prevCount=0;
                retrievingData(true);

            }
        });
    }




    public   void saveLinksToFirebase(final List<MyTweet> ttweets, List<String> ttimeKey, Set<String> set)
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
            mAdapter.setDataNull();
            offset=-1*Pref.getSinceId(getActivity());
            prevCount=0;
            retrievingData(true);

            return;
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

                            mAdapter.setDataNull();
                            offset=-1*Pref.getSinceId(getActivity());
                            prevCount=0;
                            retrievingData(true);
                           // mProgress.cancel();
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

        Long sid=Pref.getSinceId(getActivity());
        Long mid=Pref.getMaxId(getActivity());

        SinceMaxidModel temp1=new SinceMaxidModel(sid,mid);
        mmDatabase.child(useruid).child("AllId").setValue(temp1);



    }





    void retrievingData(final boolean isRefreshLoad)
    {

        Query tQuery= mDatabase.orderByChild("tweetId").startAt(offset+100).limitToFirst(10);

        tQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //if call via scroll listener
                if(!isRefreshLoad)
                    qaProgressBar.setVisibility(View.GONE);
                else
                    mySwipeRefreshLayout.setRefreshing(false);



                for (DataSnapshot qaSnapshot: dataSnapshot.getChildren())
                {
                    mSwipeDownMessage.setVisibility(View.INVISIBLE);

                    MyTweet mData= qaSnapshot.getValue(MyTweet.class);
                   // Log.v("Soma",mData.getTweetId()+"");
                    mAdapter.addItem(mAdapter.getItemCount(),mData,qaSnapshot.getKey());
                    offset=mData.getTweetId();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                if(!isRefreshLoad)
                    qaProgressBar.setVisibility(View.GONE);
                else   mySwipeRefreshLayout.setRefreshing(false);

            }
        });
    }



}
