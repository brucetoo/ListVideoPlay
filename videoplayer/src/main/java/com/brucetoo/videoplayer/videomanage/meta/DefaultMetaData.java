package com.brucetoo.videoplayer.videomanage.meta;

/**
 * Created by Bruce Too
 * On 20/04/2017.
 * At 10:18
 */

public class DefaultMetaData implements MetaData{

    private String videoUrl;

    public DefaultMetaData(String videoUrl){
        this.videoUrl = videoUrl;
    }

    @Override
    public String getVideoUrl() {
        return videoUrl;
    }
}
