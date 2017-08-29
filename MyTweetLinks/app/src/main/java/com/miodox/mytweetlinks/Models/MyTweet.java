package com.miodox.mytweetlinks.Models;

/**
 * Created by somananda on 8/24/2017.
 */

public class MyTweet {


    private String url;
    private long tweetId;
    private String tweetUsername;
    private String time;
    private String profileImageurl;
    private String screenName;


    public MyTweet()
    {
        //empty constructor
    }

    public MyTweet(long tweetId,String tweetUsername,String time
            ,String url,String profileImageurl,String screenName)
    {
        this.url=url;
        this.tweetId=tweetId;
        this.tweetUsername=tweetUsername;
        this.time=time;
        this.profileImageurl=profileImageurl;
        this.screenName=screenName;
    }




    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }




    public long getTweetId() {
        return tweetId;
    }

    public void setTweetId(long tweetId) {
        this.tweetId = tweetId;
    }

    public String getTweetUsername() {
        return tweetUsername;
    }

    public void setTweetUsername(String tweetUsername) {
        this.tweetUsername = tweetUsername;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getProfileImageurl() {
        return profileImageurl;
    }

    public void setProfileImageurl(String profileImageurl) {
        this.profileImageurl = profileImageurl;
    }




}
