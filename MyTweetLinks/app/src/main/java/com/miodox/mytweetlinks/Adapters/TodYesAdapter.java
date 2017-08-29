package com.miodox.mytweetlinks.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.miodox.mytweetlinks.Models.MyTweet;
import com.miodox.mytweetlinks.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by somananda on 8/24/2017.
 */

public class TodYesAdapter extends RecyclerView.Adapter<TodYesAdapter.ViewHolder>{


    private Context context;
    private ArrayList<MyTweet> tweetList = new ArrayList<MyTweet>();
    private ArrayList<String> tweetsKeys =new ArrayList<String>();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String useruid=user.getUid();
    private String whichDate;



    public TodYesAdapter()
    {//default constructor
    }

    public TodYesAdapter(Context context,String whichDate)
    {
        this.context=context;
        this.whichDate=whichDate;
    }


    public void addItem(int pos,MyTweet dataset,String key) {
        //Adding new element to qaList at top
        tweetList.add(pos,dataset);
        //adding corresponding
         tweetsKeys.add(pos,key);

        notifyItemRangeChanged(pos,getItemCount());

    }


    //clear all adapter data
    public void setDataNull() {
       tweetList.clear();
        tweetsKeys.clear();
        notifyDataSetChanged();

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tod_yes_row,parent,false);
        TodYesAdapter.ViewHolder viewHolder = new TodYesAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        final MyTweet tempTweet=tweetList.get(position);

        Glide.with(context)
                .load(tempTweet.getProfileImageurl())
                .fitCenter()
                .into(holder.mProfilePhoto);
        holder.mTime.setText(tempTweet.getTime());
        holder.mUserName.setText(tempTweet.getTweetUsername());
        holder.mLink.setText(tempTweet.getUrl());
        holder.deleteTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                deleteDialog(tweetsKeys.get(position),position);
            }
        });
        holder.mLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW
                        , Uri.parse(tempTweet.getUrl()));
                context.startActivity(browserIntent);
            }
        });
        holder.mProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW
                        , Uri.parse("https://twitter.com/"+tempTweet.getScreenName()));
                context.startActivity(browserIntent);
            }
        });

        holder.mUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW
                        , Uri.parse("https://twitter.com/"+tempTweet.getScreenName()));
                context.startActivity(browserIntent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return tweetList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView mProfilePhoto;
        public ImageView deleteTweet;
        public TextView mTime,mUserName,mLink;

        public ViewHolder(View itemView) {
            super(itemView);

            mProfilePhoto=(CircleImageView) itemView.findViewById(R.id.tod_yes_row_profile_photo);
            mTime=(TextView)itemView.findViewById(R.id.tod_yes_row_time);
            mUserName=(TextView)itemView.findViewById(R.id.tod_yes_row_username);
            mLink=(TextView)itemView.findViewById(R.id.tod_yes_row_link);
            deleteTweet=(ImageView)itemView.findViewById(R.id.delete_tweet);

        }
    }


    void deleteTweetFunc(String key,int pos)
    {

        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child(useruid).child("AllTweets").child(whichDate).child(key).setValue(null);
        deleteItem(pos);



    }


    //delete data at given position
    public void deleteItem(int pos)
    {
        // Remove data from the list
        tweetList.remove(pos);
        tweetsKeys.remove(pos);

        // Update the RecyclerView
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, tweetList.size());
    }


    private void deleteDialog(final String key, final int pos)
    {

            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
            builder.setMessage("Are you sure you want to delete this link?")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteTweetFunc( key, pos);

                        }
                    }).setNegativeButton("Cancel",null);
            android.support.v7.app.AlertDialog dialog = builder.create();
            dialog.show();


    }

}
