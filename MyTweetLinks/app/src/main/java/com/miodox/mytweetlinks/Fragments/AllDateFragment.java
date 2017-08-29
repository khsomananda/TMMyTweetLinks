package com.miodox.mytweetlinks.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miodox.mytweetlinks.Adapters.AllDateAdapter;
import com.miodox.mytweetlinks.Adapters.TodYesAdapter;
import com.miodox.mytweetlinks.R;


public class AllDateFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    private RecyclerView mRecyclerView;
    private AllDateAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private DatabaseReference mDatabase;
    private ChildEventListener childEventListener;
    private boolean isFirstTime=true;
    private ValueEventListener mValuelistener;
    private ProgressBar qaProgressBar;


    public AllDateFragment() {
        // Required empty public constructor
    }



    public static AllDateFragment newInstance() {
        AllDateFragment fragment = new AllDateFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView=(ViewGroup) inflater.inflate(R.layout.fragment_all_date, container, false);


        qaProgressBar=(ProgressBar)rootView.findViewById(R.id.date_all_progressBar);
        mRecyclerView=(RecyclerView)rootView.findViewById(R.id.all_date_recycler_view);
        mLayoutManager=new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter=new AllDateAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);



        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String useruid=user.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference(useruid).child("AllDate");
       // mDatabase.keepSynced(true);

        retrievingData();

        return rootView;
    }

    private void retrievingData() {



       mValuelistener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAdapter.setDataNull();

                qaProgressBar.setVisibility(View.INVISIBLE);
                for (DataSnapshot qaSnapshot: dataSnapshot.getChildren())
                {
                    String mData= qaSnapshot.getValue(String.class);
                    mAdapter.addItem(0, mData,qaSnapshot.getKey());

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        mDatabase.addValueEventListener(mValuelistener);


    }


    @Override
    public void onStop() {
        super.onStop();
        if(mDatabase!=null)
        {
            mDatabase.removeEventListener(mValuelistener);
        }
    }


}
