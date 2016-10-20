package com.brucetoo.listvideoplay.videomanage.manager;


import com.brucetoo.listvideoplay.videomanage.meta.MetaData;

public interface PlayerItemChangeListener {
    void onPlayerItemChanged(MetaData currentItemMetaData);
}
