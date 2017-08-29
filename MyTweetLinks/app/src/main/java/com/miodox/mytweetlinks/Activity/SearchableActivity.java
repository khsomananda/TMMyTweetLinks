package com.miodox.mytweetlinks.Activity;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import com.miodox.mytweetlinks.Adapters.AllDateAdapter;
import com.miodox.mytweetlinks.Adapters.TodYesAdapter;
import com.miodox.mytweetlinks.HelperClass;
import com.miodox.mytweetlinks.Models.MyTweet;
import com.miodox.mytweetlinks.R;

public class SearchableActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private TodYesAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private DatabaseReference mDatabase;
    private TextView mMsg;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMsg=(TextView)findViewById(R.id.no_search_result_msg);
        mMsg.setVisibility(View.INVISIBLE);

        mProgress=(ProgressBar)findViewById(R.id.loading_search_progressBar);

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            String searchDate=intent.getStringExtra("whichDate");
            String tempDate=searchDate;
            if(searchDate==null) {
             errorMsg();
                tempDate = "ALL DATES";
            }
                setTitle(tempDate+" - Search \""+query+"\" ");

            mRecyclerView=(RecyclerView)findViewById(R.id.search_recycler_view);
            mLayoutManager=new LinearLayoutManager(getApplicationContext());

            mRecyclerView.setLayoutManager(mLayoutManager);

            mAdapter=new TodYesAdapter(SearchableActivity.this,searchDate);
            mRecyclerView.setAdapter(mAdapter);



            if(searchDate!=null)
            {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String useruid=user.getUid();

                mDatabase = FirebaseDatabase.getInstance().getReference(useruid)
                        .child("AllTweets").child(searchDate);

                query=query.toLowerCase();
                query=query.trim();
                retrievingData(query);
            }
            else mProgress.setVisibility(View.INVISIBLE);


        }



    }



    private void errorMsg()
    {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(SearchableActivity.this);
        builder.setMessage("Sorry... you can't search from ALL DATES")
                .setPositiveButton(android.R.string.ok,null);
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();


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



    void retrievingData(final String query)
    {
        Query tQuery= mDatabase.orderByChild("tweetId");

        tQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mProgress.setVisibility(View.INVISIBLE);


                boolean isAnyItem=false;

                for (DataSnapshot qaSnapshot: dataSnapshot.getChildren())
                {

                    MyTweet mData= qaSnapshot.getValue(MyTweet.class);

                    String tempname=mData.getTweetUsername();
                    if(query.length()<=tempname.length())
                    {

                        tempname=tempname.substring(0,query.length()).toLowerCase();

                        if(tempname.equals(query))
                        {
                            isAnyItem=true;
                            mAdapter.addItem(mAdapter.getItemCount(),mData,qaSnapshot.getKey());
                        }


                    }


                }

                if (!isAnyItem)
                {
                    mMsg.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                mProgress.setVisibility(View.INVISIBLE);


            }
        });
    }

}
