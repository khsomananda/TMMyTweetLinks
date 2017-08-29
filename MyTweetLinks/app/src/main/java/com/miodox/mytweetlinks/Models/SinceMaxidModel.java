package com.miodox.mytweetlinks.Models;

/**
 * Created by somananda on 8/25/2017.
 */

public class SinceMaxidModel {

    Long sinceId;
    Long maxid;

   public SinceMaxidModel()
    {

    }
   public  SinceMaxidModel(Long sinceId,Long maxid)
    {
        this.sinceId=sinceId;
        this.maxid=maxid;
    }

    public long getSinceId() {
        return sinceId;
    }

    public void setSinceId(long sinceId) {
        this.sinceId = sinceId;
    }

    public long getMaxid() {
        return maxid;
    }

    public void setMaxid(long maxid) {
        this.maxid = maxid;
    }



}
