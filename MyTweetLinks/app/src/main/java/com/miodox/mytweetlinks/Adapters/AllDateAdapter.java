package com.miodox.mytweetlinks.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.miodox.mytweetlinks.Activity.ShowTweetsByDateActivity;
import com.miodox.mytweetlinks.Models.MyTweet;
import com.miodox.mytweetlinks.R;

import java.util.ArrayList;



/**
 * Created by somananda on 8/24/2017.
 */

public class AllDateAdapter extends  RecyclerView.Adapter<AllDateAdapter.ViewHolder> {


    private Context context;
    private ArrayList<String> dateNameList = new ArrayList<String>();
    private ArrayList<String> tweetsKeys =new ArrayList<String>();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String useruid=user.getUid();





    public AllDateAdapter()
    {//default constructor
    }

    public AllDateAdapter(Context context)
    {
        this.context=context;

    }


    public void addItem(int pos,String dataset,String key) {
        //Adding new element to qaList at top
        dateNameList.add(pos,dataset);
        //adding corresponding
        tweetsKeys.add(pos,key);

        notifyItemRangeChanged(pos,getItemCount());

    }


    //clear all adapter data
    public void setDataNull() {
        // storesList.clear();
        dateNameList.clear();
        notifyDataSetChanged();

    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_day_row,parent,false);
        AllDateAdapter.ViewHolder viewHolder = new AllDateAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mDate.setText(dateNameList.get(position));


    }

    @Override
    public int getItemCount() {
        return dateNameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mDate;

        public ViewHolder(View itemView) {
            super(itemView);
            mDate=(TextView) itemView.findViewById(R.id.date_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                   Intent intent=new Intent(context, ShowTweetsByDateActivity.class);
                    intent.putExtra("date",dateNameList.get(getAdapterPosition()));
                    context.startActivity(intent);

                }
            });


        }
    }


    void deleteTweetFunc(String key,int pos)
    {

        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child(useruid).child("AllDate").child(key).setValue(null);
        deleteItem(pos);


    }

    //delete data at given position
    public void deleteItem(int pos)
    {
        // Remove data from the list
        dateNameList.remove(pos);
        tweetsKeys.remove(pos);

        // Update the RecyclerView
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, dateNameList.size());
    }

}
