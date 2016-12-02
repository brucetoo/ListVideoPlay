package com.brucetoo.listvideoplay.demo;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.brucetoo.listvideoplay.R;
import com.brucetoo.listvideoplay.videomanage.controller.ListScrollDistanceCalculator;
import com.brucetoo.listvideoplay.videomanage.controller.VideoControllerView;
import com.brucetoo.listvideoplay.videomanage.controller.ViewAnimator;
import com.brucetoo.listvideoplay.videomanage.manager.SingleVideoPlayerManager;
import com.brucetoo.listvideoplay.videomanage.manager.VideoPlayerManager;
import com.brucetoo.listvideoplay.videomanage.meta.CurrentItemMetaData;
import com.brucetoo.listvideoplay.videomanage.meta.MetaData;
import com.brucetoo.listvideoplay.videomanage.ui.HighLightMaskView;
import com.brucetoo.listvideoplay.videomanage.ui.MediaPlayerWrapper;
import com.brucetoo.listvideoplay.videomanage.ui.VideoPlayerView;
import com.brucetoo.listvideoplay.videomanage.utils.ViewHolder;
import com.squareup.picasso.Picasso;

/**
 * Created by Bruce Too
 * On 11/03/16.
 * At 10:05
 */

public class ListViewMaskFragment extends Fragment implements AbsListView.OnScrollListener, View.OnClickListener {

    public static final String TAG = "ListViewMaskFragment";

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private DisableListView mListView;
    private FrameLayout mVideoFloatContainer;
    private View mVideoPlayerBg;
    private ImageView mVideoCoverMask;
    private VideoPlayerView mVideoPlayerView;
    private View mVideoLoadingView;
    private ProgressBar mVideoProgressBar;

    private View mCurrentPlayArea;
    private VideoControllerView mCurrentVideoControllerView;
    private int mCurrentActiveVideoItem = -1;
    private int mCurrentBuffer;

    /**
     * Prevent {@link #stopPlaybackImmediately} be called too many times
     */
    private boolean mCanTriggerStop = true;

    private VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(null);

    /**
     * To detect ListView scroll delta
     */
    private ListScrollDistanceCalculator mDistanceListener = new ListScrollDistanceCalculator();

    /**
     * ListView's onScroll callback is super class {@link AbsListView}
     * to control,so let the page itself to determine whether scroll or
     * not by using {@link AbsListView.OnScrollListener#onScrollStateChanged(AbsListView, int)}
     */
    private boolean mUserTouchHappened;

    /**
     * Stop video have two scenes
     * 1.click to stop current video and start a new video
     * 2.when video item is dismiss or ViewPager changed ? tab changed ? ...
     */
    private boolean mIsClickToStop;

    private float mOriginalHeight;

    private HighLightMaskView mHighLightView;

    private boolean mIsVideoPrepared;

    private ViewGroup mRootContentView;

    private void startMoveFloatContainer(boolean click) {

        if (mVideoFloatContainer.getVisibility() != View.VISIBLE) return;
        final float moveDelta;

        if (click) {
            Log.e(TAG, "startMoveFloatContainer > mFloatVideoContainer getTranslationY:" + mVideoFloatContainer.getTranslationY());
            ViewAnimator.putOn(mVideoFloatContainer).translationY(0);

            int[] playAreaPos = new int[2];
            int[] floatContainerPos = new int[2];
            mCurrentPlayArea.getLocationOnScreen(playAreaPos);
            mVideoFloatContainer.getLocationOnScreen(floatContainerPos);
            mOriginalHeight = moveDelta = playAreaPos[1] - floatContainerPos[1];

            Log.e(TAG, "startMoveFloatContainer > mFloatVideoContainer playAreaPos[1]:" + playAreaPos[1] + " floatContainerPos[1]:" + floatContainerPos[1]);
        } else {
            moveDelta = mDistanceListener.getTotalScrollDistance();
            /**
             * NOTE if ListView has divider,{@link ListScrollDistanceCalculator}
             * can't work perfectly when scroll reach to divider view.So find a
             * another way to get the same effect by divider.
             */
            Log.e(TAG, "ListView moveDelta :" + moveDelta + "");
        }

        float translationY = moveDelta + (!click ? mOriginalHeight : 0);

        Log.e(TAG, "startMoveFloatContainer > moveDelta:" + moveDelta + " before getTranslationY:" + mVideoFloatContainer.getTranslationY()
                + " mOriginalHeight:" + mOriginalHeight + " translationY:" + translationY);

        ViewAnimator.putOn(mVideoFloatContainer).translationY(translationY);

        Log.i(TAG, "startMoveFloatContainer < after getTranslationY:" + mVideoFloatContainer.getTranslationY());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_view, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView = (DisableListView) view.findViewById(R.id.list_view);
        mVideoFloatContainer = (FrameLayout) view.findViewById(R.id.layout_float_video_container);
        mVideoPlayerBg = view.findViewById(R.id.video_player_bg);
        mVideoCoverMask = (ImageView) view.findViewById(R.id.video_player_mask);
        mVideoPlayerView = (VideoPlayerView) view.findViewById(R.id.video_player_view);
        mVideoLoadingView = view.findViewById(R.id.video_progress_loading);
        mVideoProgressBar = (ProgressBar) view.findViewById(R.id.video_progress_bar);

        mHighLightView = new HighLightMaskView(getContext());
        mRootContentView = (ViewGroup) getActivity().getWindow().findViewById(android.R.id.content);

        mListView.setAdapter(new ListViewAdapter(this));
        mListView.setOnScrollListener(this);

        mVideoPlayerView.addMediaPlayerListener(new MediaPlayerWrapper.MainThreadMediaPlayerListener() {
            @Override
            public void onVideoSizeChangedMainThread(int width, int height) {

            }

            @Override
            public void onVideoPreparedMainThread() {

                Log.e(MediaPlayerWrapper.VIDEO_TAG, "check play onVideoPreparedMainThread");
                mVideoFloatContainer.setVisibility(View.VISIBLE);
                mVideoPlayerView.setVisibility(View.VISIBLE);
                mVideoLoadingView.setVisibility(View.VISIBLE);
                //for cover the pre Video frame
                mVideoCoverMask.setVisibility(View.VISIBLE);
                mIsVideoPrepared = true;
            }

            @Override
            public void onVideoCompletionMainThread() {

                Log.e(MediaPlayerWrapper.VIDEO_TAG, "check play onVideoCompletionMainThread");

                if (mCurrentPlayArea != null) {
                    mCurrentPlayArea.setClickable(true);
                }

                mVideoFloatContainer.setVisibility(View.INVISIBLE);
                mCurrentPlayArea.setVisibility(View.VISIBLE);
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                ViewAnimator.putOn(mVideoFloatContainer).translationY(0);

                //stop update progress
                mVideoProgressBar.setVisibility(View.GONE);
                mHandler.removeCallbacks(mProgressRunnable);

                mIsVideoPrepared = false;
                if(mHighLightView != null) {
                  mRootContentView.removeView(mHighLightView);
                }
            }

            @Override
            public void onErrorMainThread(int what, int extra) {
                Log.e(MediaPlayerWrapper.VIDEO_TAG, "check play onErrorMainThread");
                if (mCurrentPlayArea != null) {
                    mCurrentPlayArea.setClickable(true);
                    mCurrentPlayArea.setVisibility(View.VISIBLE);
                }
                mVideoFloatContainer.setVisibility(View.INVISIBLE);

                //stop update progress
                mVideoProgressBar.setVisibility(View.GONE);
                mHandler.removeCallbacks(mProgressRunnable);

                mIsVideoPrepared = false;
            }

            @Override
            public void onBufferingUpdateMainThread(int percent) {
                Log.e(MediaPlayerWrapper.VIDEO_TAG, "check play onBufferingUpdateMainThread");
                mCurrentBuffer = percent;
            }

            @Override
            public void onVideoStoppedMainThread() {
                Log.e(MediaPlayerWrapper.VIDEO_TAG, "check play onVideoStoppedMainThread");
                if (!mIsClickToStop) {
                    mCurrentPlayArea.setClickable(true);
                    mCurrentPlayArea.setVisibility(View.VISIBLE);
                }

                //stop update progress
                mVideoProgressBar.setVisibility(View.GONE);
                mHandler.removeCallbacks(mProgressRunnable);

                mIsVideoPrepared = false;
            }

            @Override
            public void onInfoMainThread(int what) {
                Log.e(MediaPlayerWrapper.VIDEO_TAG, "check play onInfoMainThread what:" + what);
              //MEDIA_INFO_VIDEO_RENDERING_START was added in API Level 17 so here has
              //a bug,to fix it, replace MediaPlayer to some else or distinguish 17 up/down
              // < 17 handle this info in onVideoPreparedMainThread
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {

                    //start update progress
                    mVideoProgressBar.setVisibility(View.VISIBLE);
                    mHandler.post(mProgressRunnable);

                    mVideoPlayerView.setVisibility(View.VISIBLE);
                    mVideoLoadingView.setVisibility(View.GONE);
                    mVideoCoverMask.setVisibility(View.GONE);
                    mVideoPlayerBg.setVisibility(View.VISIBLE);
                    createVideoControllerView();

                    mCurrentVideoControllerView.showWithTitle("VIDEO TEST - " + mCurrentActiveVideoItem);

                    //when Surface start rendering video frame,we need start execute high-light logic
                    startHighLight();

                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    mVideoLoadingView.setVisibility(View.VISIBLE);
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    mVideoLoadingView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void startHighLight() {

        View currentItemView = Utils.getViewByPosition(mCurrentActiveVideoItem, mListView);
        mHighLightView.setStartAndEndY(currentItemView);

        if(mHighLightView.getParent() == null) {
            mRootContentView.addView(mHighLightView);
        }
    }

    private void createVideoControllerView() {

        if (mCurrentVideoControllerView != null) {
            mCurrentVideoControllerView.hide();
            mCurrentVideoControllerView = null;
        }

        mCurrentVideoControllerView = new VideoControllerView.Builder(getActivity(), mPlayerControlListener)
                .withVideoTitle("TEST VIDEO")
                .withVideoView(mVideoPlayerView)//to enable toggle display controller view
                .canControlBrightness(true)
                .canControlVolume(true)
                .canSeekVideo(false)
                .exitIcon(R.drawable.video_top_back)
                .pauseIcon(R.drawable.ic_media_pause)
                .playIcon(R.drawable.ic_media_play)
                .shrinkIcon(R.drawable.ic_media_fullscreen_shrink)
                .stretchIcon(R.drawable.ic_media_fullscreen_stretch)
                .build(mVideoFloatContainer);//layout container that hold video play view
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        mDistanceListener.onScrollStateChanged(absListView, scrollState);
        switch (scrollState) {
            case SCROLL_STATE_FLING:
            case SCROLL_STATE_TOUCH_SCROLL:
                mUserTouchHappened = true;
                Log.i(TAG, "onScrollStateChanged state:" + scrollState);

                if(scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    if (mIsVideoPrepared) {
                        mHighLightView.startAlpha2HighLight();
                    }
                }
                break;
            case SCROLL_STATE_IDLE:
                mUserTouchHappened = false;
                //if ListView state is idle,adjust originalHeight of mVideoFloatContainer
                mOriginalHeight = mVideoFloatContainer.getTranslationY();

                if(mIsVideoPrepared){
                    mHighLightView.startAlpha2NormalLight();
                }
                Log.i(TAG, "onScrollStateChanged state:" + scrollState);
                break;
            default:
                break;
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        //User not touch screen but AbsListView callback onScroll
        if (!mUserTouchHappened) return;

        Log.e(TAG, "check stop activeItem:" + mCurrentActiveVideoItem + "  firstVisibleItem:" + firstVisibleItem
                + "  lastVisibleItem:" + (firstVisibleItem + visibleItemCount - 1));

        mDistanceListener.onScroll(absListView, firstVisibleItem, visibleItemCount, totalItemCount);

        startMoveFloatContainer(false);

        //only work in prepared state
        if(mIsVideoPrepared) {
            mHighLightView.updateStartAndEndY(mDistanceListener.getDeltaScrollDistance());
        }

        //NOTE if ListView has header we need subtract header count,and test here

        //This is just detect the playing item is gone and stop it when scroll ListView
        if (mCurrentActiveVideoItem < firstVisibleItem || mCurrentActiveVideoItem > (firstVisibleItem + visibleItemCount - 1)) {

            //remote msg callback first
            if (mCurrentPlayArea != null && mCurrentVideoControllerView != null)
                mCurrentVideoControllerView.removeAllCallBacks();
            //item invisible so stop all playBack
            if (mCanTriggerStop) {
                Log.e(TAG, "check stop activeItem:" + mCurrentActiveVideoItem + "  firstVisibleItem:" + (firstVisibleItem - 1)
                        + "  lastVisibleItem:" + (firstVisibleItem + visibleItemCount - 1));
                mCanTriggerStop = false;
                stopPlaybackImmediately();
            }

        }
    }

    public void stopPlaybackImmediately() {

        mIsClickToStop = false;
        mIsVideoPrepared = false;
        if(mHighLightView.getParent() != null){
            mRootContentView.removeView(mHighLightView);
        }

        if (mCurrentPlayArea != null) {
            mCurrentPlayArea.setClickable(true);
        }

        if (mVideoPlayerManager != null) {
            Log.e(TAG, "check play stopPlaybackImmediately");
            mVideoFloatContainer.setVisibility(View.INVISIBLE);
            mVideoPlayerManager.stopAnyPlayback();
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.layout_play_area) {

            mIsClickToStop = true;
            v.setClickable(false);
            if (mCurrentPlayArea != null) {
                if (mCurrentPlayArea != v) {
                    mCurrentPlayArea.setClickable(true);
                    mCurrentPlayArea.setVisibility(View.VISIBLE);
                    mCurrentPlayArea = v;
                } else {//click same area
                    if (mVideoFloatContainer.getVisibility() == View.VISIBLE) return;
                }
            } else {
                mCurrentPlayArea = v;
            }

            //invisible self ,and make visible when video play completely
            v.setVisibility(View.INVISIBLE);
            if (mCurrentVideoControllerView != null)
                mCurrentVideoControllerView.hide();

            mVideoFloatContainer.setVisibility(View.VISIBLE);
            mVideoCoverMask.setVisibility(View.GONE);
            mVideoPlayerBg.setVisibility(View.GONE);

            VideoModel model = (VideoModel) v.getTag();
            mCurrentActiveVideoItem = model.position;
            mCanTriggerStop = true;
            mIsVideoPrepared = false;
            if(mHighLightView.getParent() != null){
                mRootContentView.removeView(mHighLightView);
            }

            //move container view
            startMoveFloatContainer(true);

            mVideoLoadingView.setVisibility(View.VISIBLE);
            mVideoPlayerView.setVisibility(View.INVISIBLE);

            //play video
            mVideoPlayerManager.playNewVideo(new CurrentItemMetaData(model.position, v), mVideoPlayerView, model.videoUrl);

        }
    }

    private boolean checkMediaPlayerInvalid() {
        return mVideoPlayerView != null && mVideoPlayerView.getMediaPlayer() != null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (mVideoFloatContainer == null) return;

        ViewGroup.LayoutParams layoutParams = mVideoFloatContainer.getLayoutParams();

        mCurrentVideoControllerView.hide();

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            //200 indicate the height of video play area
            layoutParams.height = (int) getResources().getDimension(R.dimen.video_item_portrait_height);
            layoutParams.width = Utils.getDeviceWidth(getActivity());

            ViewAnimator.putOn(mVideoFloatContainer).translationY(mOriginalHeight);

            // Show status bar
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            mListView.setEnableScroll(true);

        } else {

            layoutParams.height = Utils.getDeviceHeight(getActivity());
            layoutParams.width = Utils.getDeviceWidth(getActivity());

            ViewAnimator.putOn(mVideoFloatContainer).translationY(0);

            // Hide status
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            mListView.setEnableScroll(false);

        }
        mVideoFloatContainer.setLayoutParams(layoutParams);
    }

    private VideoControllerView.MediaPlayerControlListener mPlayerControlListener = new VideoControllerView.MediaPlayerControlListener() {
        @Override
        public void start() {
            if (checkMediaPlayerInvalid())
                mVideoPlayerView.getMediaPlayer().start();
        }

        @Override
        public void pause() {
            mVideoPlayerView.getMediaPlayer().pause();
        }

        @Override
        public int getDuration() {
            if (checkMediaPlayerInvalid()) {
                return mVideoPlayerView.getMediaPlayer().getDuration();
            }
            return 0;
        }

        @Override
        public int getCurrentPosition() {
            if (checkMediaPlayerInvalid()) {
                return mVideoPlayerView.getMediaPlayer().getCurrentPosition();
            }
            return 0;
        }

        @Override
        public void seekTo(int position) {
            if (checkMediaPlayerInvalid()) {
                mVideoPlayerView.getMediaPlayer().seekToPosition(position);
            }
        }

        @Override
        public boolean isPlaying() {
            if (checkMediaPlayerInvalid()) {
                return mVideoPlayerView.getMediaPlayer().isPlaying();
            }
            return false;
        }

        @Override
        public boolean isComplete() {
            return false;
        }

        @Override
        public int getBufferPercentage() {
            return mCurrentBuffer;
        }

        @Override
        public boolean isFullScreen() {
            return getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    || getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        }

        @Override
        public void toggleFullScreen() {
            if (isFullScreen()) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                getActivity().setRequestedOrientation(Build.VERSION.SDK_INT < 9 ?
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
        }

        @Override
        public void exit() {
            //TODO to handle exit status
            if (isFullScreen()) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    };

    private class ListViewAdapter extends BaseAdapter {

        View.OnClickListener listener;

        public ListViewAdapter(View.OnClickListener listener) {
            this.listener = listener;
        }

        @Override
        public int getCount() {
            return ListDataGenerater.datas.size();
        }

        @Override
        public Object getItem(int i) {
            return ListDataGenerater.datas.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = ViewHolder.newInstance(getContext(), view, viewGroup, R.layout.item_list_view);
            VideoModel tag = ListDataGenerater.datas.get(i);
            Picasso.with(getActivity()).load(tag.coverImage)
                    .placeholder(R.drawable.shape_place_holder)
                    .into((ImageView) holder.getView(R.id.img_cover));
            tag.position = i;

            holder.getView(R.id.layout_play_area).setTag(tag);
            holder.getView(R.id.layout_play_area).setOnClickListener(listener);
            holder.setText(R.id.tv_video_name, "Just Video " + i);
            return holder.getView();
        }
    }


    /**
     * Runnable for update current video progress
     * 1.start this runnable in {@link MediaPlayerWrapper.MainThreadMediaPlayerListener#onInfoMainThread(int)}
     * 2.stop(remove) this runnable in {@link MediaPlayerWrapper.MainThreadMediaPlayerListener#onVideoStoppedMainThread()}
     * {@link MediaPlayerWrapper.MainThreadMediaPlayerListener#onVideoCompletionMainThread()}
     * {@link MediaPlayerWrapper.MainThreadMediaPlayerListener#onErrorMainThread(int, int)} ()}
     */
    private Runnable mProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPlayerControlListener != null) {

                if (mCurrentVideoControllerView.isShowing()) {
                    mVideoProgressBar.setVisibility(View.GONE);
                } else {
                    mVideoProgressBar.setVisibility(View.VISIBLE);
                }

                int position = mPlayerControlListener.getCurrentPosition();
                int duration = mPlayerControlListener.getDuration();
                if (duration != 0) {
                    long pos = 1000L * position / duration;
                    int percent = mPlayerControlListener.getBufferPercentage() * 10;
                    mVideoProgressBar.setProgress((int) pos);
                    mVideoProgressBar.setSecondaryProgress(percent);
                    mHandler.postDelayed(this, 1000);
                }
            }
        }
    };
}
