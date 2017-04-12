package com.brucetoo.videoplayer.videomanage.manager;


import com.brucetoo.videoplayer.videomanage.meta.MetaData;

public interface PlayerItemChangeListener {
    void onPlayerItemChanged(MetaData currentItemMetaData);
}
