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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.brucetoo.listvideoplay.R;
import com.brucetoo.listvideoplay.videomanage.controller.VideoControllerView;
import com.brucetoo.listvideoplay.videomanage.controller.ViewAnimator;
import com.brucetoo.listvideoplay.videomanage.manager.SingleVideoPlayerManager;
import com.brucetoo.listvideoplay.videomanage.manager.VideoPlayerManager;
import com.brucetoo.listvideoplay.videomanage.meta.CurrentItemMetaData;
import com.brucetoo.listvideoplay.videomanage.meta.MetaData;
import com.brucetoo.listvideoplay.videomanage.ui.MediaPlayerWrapper;
import com.brucetoo.listvideoplay.videomanage.ui.VideoPlayerView;
import com.squareup.picasso.Picasso;

/**
 * Created by Bruce Too
 * On 10/20/16.
 * At 23:33
 */

public class RecyclerViewFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "RecyclerViewFragment";
    private DisableRecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private FrameLayout mVideoFloatContainer;
    private View mVideoPlayerBg;
    private ImageView mVideoCoverMask;
    private VideoPlayerView mVideoPlayerView;
    private View mVideoLoadingView;
    private ProgressBar mVideoProgressBar;

    private View mCurrentPlayArea;
    private VideoControllerView mCurrentVideoControllerView;
    private int mCurrentActiveVideoItem = -1;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int mCurrentBuffer;

    /**
     * Prevent {@link #stopPlaybackImmediately} be called too many times
     */
    private boolean mCanTriggerStop = true;

    private VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(null);

    /**
     * Stop video have two scenes
     * 1.click to stop current video and start a new video
     * 2.when video item is dismiss or ViewPager changed ? tab changed ? ...
     */
    private boolean mIsClickToStop;

    private float mOriginalHeight;

    private float mMoveDeltaY;

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
            moveDelta = mMoveDeltaY;
            Log.e(TAG, "ListView moveDelta :" + moveDelta + "");
        }

        float translationY = moveDelta + (!click ? mOriginalHeight : 0);

        Log.e(TAG, "startMoveFloatContainer > moveDelta:" + moveDelta + " before getTranslationY:" + mVideoFloatContainer.getTranslationY()
                + " mOriginalHeight:" + mOriginalHeight + " translationY:" + translationY);

        ViewAnimator.putOn(mVideoFloatContainer).translationY(translationY);

        Log.i(TAG, "startMoveFloatContainer < after getTranslationY:" + mVideoFloatContainer.getTranslationY());
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recycler_view, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mVideoFloatContainer = (FrameLayout) view.findViewById(R.id.layout_float_video_container);
        mVideoPlayerBg = view.findViewById(R.id.video_player_bg);
        mVideoCoverMask = (ImageView) view.findViewById(R.id.video_player_mask);
        mVideoPlayerView = (VideoPlayerView) view.findViewById(R.id.video_player_view);
        mVideoLoadingView = view.findViewById(R.id.video_progress_loading);
        mVideoProgressBar = (ProgressBar) view.findViewById(R.id.video_progress_bar);

        mRecyclerView = (DisableRecyclerView) view.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mRecyclerView.setAdapter(new RecyclerAdapter(this));

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
            }

            @Override
            public void onInfoMainThread(int what) {
                Log.e(MediaPlayerWrapper.VIDEO_TAG, "check play onInfoMainThread what:" + what);
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
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    mVideoLoadingView.setVisibility(View.VISIBLE);
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    mVideoLoadingView.setVisibility(View.GONE);
                }
            }
        });
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

    private boolean checkMediaPlayerInvalid() {
        return mVideoPlayerView != null && mVideoPlayerView.getMediaPlayer() != null;
    }


    RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

        int totalDy;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            Log.e(TAG, "onScrollStateChanged state:" + newState);
            if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                mOriginalHeight = mVideoFloatContainer.getTranslationY();
                totalDy = 0;
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            /**
             * NOTE: RecyclerView will callback this method when {@link RecyclerViewFragment#onConfigurationChanged(Configuration)}
             * happened,so handle this special scene.
             */
            if(mPlayerControlListener.isFullScreen()) return;

            //Calculate the total scroll distance of RecyclerView
            totalDy += dy;
            mMoveDeltaY = -totalDy;
            Log.e(TAG, "onScrolled scrollY:" + -totalDy);
            startMoveFloatContainer(false);

            if (mCurrentActiveVideoItem < mLayoutManager.findFirstVisibleItemPosition()
                    || mCurrentActiveVideoItem > mLayoutManager.findLastVisibleItemPosition()) {
                if (mCanTriggerStop) {
                    mCanTriggerStop = false;
                    stopPlaybackImmediately();
                }
            }
        }
    };

    public void stopPlaybackImmediately() {

        mIsClickToStop = false;

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

            mRecyclerView.setEnableScroll(true);

        } else {

            layoutParams.height = Utils.getDeviceHeight(getActivity());
            layoutParams.width = Utils.getDeviceWidth(getActivity());

            Log.e(TAG, "onConfigurationChanged translationY:" + mVideoFloatContainer.getTranslationY());
            ViewAnimator.putOn(mVideoFloatContainer).translationY(0);

            // Hide status
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            mRecyclerView.setEnableScroll(false);
        }
        mVideoFloatContainer.setLayoutParams(layoutParams);
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

            //move container view
            startMoveFloatContainer(true);

            mVideoLoadingView.setVisibility(View.VISIBLE);
            mVideoPlayerView.setVisibility(View.INVISIBLE);

            //play video
            mVideoPlayerManager.playNewVideo(new CurrentItemMetaData(model.position, v), mVideoPlayerView, model.videoUrl);

        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {

        View.OnClickListener listener;

        public RecyclerAdapter(View.OnClickListener listener) {
            this.listener = listener;
        }

        @Override
        public RecyclerAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_view, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerAdapter.ItemViewHolder holder, int position) {
            VideoModel model = ListDataGenerater.datas.get(position);
            holder.name.setText("Just Video " + position);
            Picasso.with(getActivity()).load(model.coverImage)
                    .placeholder(R.drawable.shape_place_holder)
                    .into(holder.cover);
            model.position = position;
            holder.playArea.setTag(model);
            holder.playArea.setOnClickListener(listener);
        }

        @Override
        public int getItemCount() {
            return ListDataGenerater.datas.size();
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {

            public TextView name;
            public ImageView cover;
            public View playArea;

            public ItemViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.tv_video_name);
                cover = (ImageView) itemView.findViewById(R.id.img_cover);
                playArea = itemView.findViewById(R.id.layout_play_area);
            }
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
            if(mPlayerControlListener != null){

                if(mCurrentVideoControllerView.isShowing()){
                    mVideoProgressBar.setVisibility(View.GONE);
                }else {
                    mVideoProgressBar.setVisibility(View.VISIBLE);
                }

                int position = mPlayerControlListener.getCurrentPosition();
                int duration = mPlayerControlListener.getDuration();
                if(duration != 0) {
                    long pos = 1000L * position / duration;
                    int percent = mPlayerControlListener.getBufferPercentage() * 10;
                    mVideoProgressBar.setProgress((int) pos);
                    mVideoProgressBar.setSecondaryProgress(percent);
                    mHandler.postDelayed(this,1000);
                }
            }
        }
    };

}
