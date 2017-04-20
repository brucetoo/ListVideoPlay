# Maybe this is a better user interaction of play video in List.

 ![list_video_play](./scale.gif)

# Play video in ListView,RecyclerView,and support full-screen,and more feature about video play control

# EFFECT

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/6-cE4g3wO5I/0.jpg)](https://www.youtube.com/watch?v=6-cE4g3wO5I)


### Detail Implements

[Android列表视频播放开发之路](http://www.jianshu.com/p/4db96418f32b)


 * Define a [IViewTrakcer](https://github.com/brucetoo/ListVideoPlay/blob/master/videoplayer/src/main/java/com/brucetoo/videoplayer/IViewTracker.java) to track the view

 * Define [FloatLayerView](https://github.com/brucetoo/ListVideoPlay/blob/master/videoplayer/src/main/java/com/brucetoo/videoplayer/FloatLayerView.java) added into {@link android.view.Window#ID_ANDROID_CONTENT} decor view

    ```java
     * It contains:
     * 1. rootLayout which hold all views to be added in {@link FloatLayerView},height must be WRAP_CONTENT
     * 2. {@link #mVideoTopView} that play video
     * 3. {@link #mVideoBottomView} in bottom layer of {@link #mVideoPlayerView}, which can be used to add some mask view...
     * 4. {@link #mVideoTopView} in top layer of {@link #mVideoPlayerView},which can add some video controller view...
    ```

 * Define [IScrollDetector](https://github.com/brucetoo/ListVideoPlay/blob/master/videoplayer/src/main/java/com/brucetoo/videoplayer/scrolldetector/IScrollDetector.java) to observer scroll state

 * Define [IControllerView](https://github.com/brucetoo/ListVideoPlay/blob/master/videoplayer/src/main/java/com/brucetoo/videoplayer/videomanage/controller/IControllerView.java) added in [FloatLayerView](!https://github.com/brucetoo/ListVideoPlay/blob/master/videoplayer/src/main/java/com/brucetoo/videoplayer/FloatLayerView.java)
  ```java
        /**
        * Normal video controller view added in {@link com.brucetoo.videoplayer.VideoTracker#mVideoTopView}
        * when attach to portrait(normal) tracker view
        */
       View normalScreenController(IViewTracker tracker);
       /**
        * Detail video controller view added in {@link com.brucetoo.videoplayer.VideoTracker#mVideoTopView}
        * when attach to detail tracker view
        */
       View detailScreenController(IViewTracker tracker);
       /**
        * Full screen video controller view added in {@link com.brucetoo.videoplayer.VideoTracker#mVideoTopView}
        * when attach to landscape screen
        */
       View fullScreenController(IViewTracker tracker);
       /**
        * Loading state controller view added in {@link com.brucetoo.videoplayer.VideoTracker#mVideoTopView}
        * when video is preparing
        */
       View loadingController(IViewTracker tracker);

       /**
        * Another controller view need be added in {@link com.brucetoo.videoplayer.VideoTracker#mVideoTopView}
        */
       View anotherController(IViewTracker tracker);

  ```

More details see [VideoPlayer](https://github.com/brucetoo/ListVideoPlay/tree/master/videoplayer/src/main/java/com/brucetoo/videoplayer)

## USAGE

### Tracker

   * Tracker attach

   ```java

    Tracker
         .attach(activity) // A activity only has a single IViewTracker
         .trackView(trackView) // Set track view,let IViewTracker's getFollowerView() can observe it scroll change
         .into(IScrollDetector) // Observe tracker view's parent scroll state change, see demo in ListScrollDetector
         .controller(IControllerView)// Switch video controller view in different scene,see demo in DefaultControllerView
         .visibleListener(this);// Observe tracker view visible rect changed in screen.
   ```
   > **NOTE**: [IScrollDetector](https://github.com/brucetoo/ListVideoPlay/blob/master/videoplayer/src/main/java/com/brucetoo/videoplayer/scrolldetector/IScrollDetector.java) and [IControllerView](https://github.com/brucetoo/ListVideoPlay/blob/master/videoplayer/src/main/java/com/brucetoo/videoplayer/videomanage/controller/IControllerView.java) will still **BE THE SAME** when change tracker view by [Tracker#changeTrackView](https://github.com/brucetoo/ListVideoPlay/blob/master/videoplayer/src/main/java/com/brucetoo/videoplayer/Tracker.java#L108)
    unless we re-call [Tracker#into](https://github.com/brucetoo/ListVideoPlay/blob/master/videoplayer/src/main/java/com/brucetoo/videoplayer/IViewTracker.java#L79) or [Trakcer#controller](https://github.com/brucetoo/ListVideoPlay/blob/master/videoplayer/src/main/java/com/brucetoo/videoplayer/IViewTracker.java#L88) again to bind new one

   * Tracker detach

   ```java
    Tracker.detach(getActivity()); //just remove all view in decor view
   ```

   * Tracker destroy

   ```java
    Tracker.destroy(getActivity()); //remove all view in decor view and clear all instanses
   ```

   * Change tracker view

   ```java
   Tracker.changeTrackView(getActivity(),newTrackerView);// re-bound to new tracker view
   ```

### Video Play Control

   * Register item changed listener

   ```java
        Tracker.addPlayerItemChangeListener(this);
   ```

   * Register video play listener

   ```java
        Tracker.addVideoPlayerListener(this);
   ```

   * Start play new one with [MetaData](https://github.com/brucetoo/ListVideoPlay/blob/master/videoplayer/src/main/java/com/brucetoo/videoplayer/videomanage/meta/MetaData.java)

   ```java
        Tracker.playNewVideo(this, mVideoPlayView, mMetaData.getVideoUrl());
   ```
   >  [MetaData](https://github.com/brucetoo/ListVideoPlay/blob/master/videoplayer/src/main/java/com/brucetoo/videoplayer/videomanage/meta/MetaData.java) can bind origin date information along with [IViewTracler](https://github.com/brucetoo/ListVideoPlay/blob/master/videoplayer/src/main/java/com/brucetoo/videoplayer/IViewTracker.java)


   * Stop playing one

   ```java
        Tracker.stopAnyPlayback();
   ```


All Demo you can see in [ListSupportFragment](https://github.com/brucetoo/ListVideoPlay/blob/master/app/src/main/java/com/brucetoo/listvideoplay/demo/ListSupportFragment.java) and [DetailFragment](https://github.com/brucetoo/ListVideoPlay/blob/master/app/src/main/java/com/brucetoo/listvideoplay/demo/DetailFragment.java)

## Known issues

   * When tracker view be re-used in ListView/RecyclerView may cause **re-tracked**

## THANKS

[VideoPlayerManager](https://github.com/danylovolokh/VideoPlayerManager): Changes happened in many places,
so there is no plan PR to origin lib.

[VideoControllerView](https://github.com/brucetoo/VideoControllerView): For more feature about video play control

## License

Copyright 2016 - 2017 Bruce too

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

See [LICENSE](LICENSE) file for details.