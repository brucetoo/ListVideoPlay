package com.brucetoo.videoplayer.videomanage.controller;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.brucetoo.videoplayer.R;
import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

/**
 * Created by Bruce Too
 * On 7/12/16.
 * At 16:09
 */
public class VideoControllerView extends FrameLayout implements VideoGestureListener {

    private static final String TAG = "VideoControllerView";

    private static final int HANDLER_ANIMATE_OUT = 1;// out animate
    private static final int HANDLER_UPDATE_PROGRESS = 2;//cycle update progress
    private static final long PROGRESS_SEEK = 500;
    private static final long ANIMATE_TIME = 300;
    private static final long AUTO_HIDE_TIME = 3000;

    private View mRootView; // root view of this
    private SeekBar mSeekBar; //seek bar for video
    private TextView mEndTime, mCurrentTime;
    private boolean mIsShowing;//controller view showing
    private boolean mIsDragging; //is dragging seekBar
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private GestureDetector mGestureDetector;//gesture detector

    private Activity mContext;
    private boolean mCanSeekVideo;
    private boolean mCanControlVolume;
    private boolean mCanControlBrightness;
    private String mVideoTitle;
    private MediaPlayerControlListener mMediaPlayerControlListener;
    private ViewGroup mAnchorView;
    private View mVideoView;

    @DrawableRes
    private int mExitIcon;
    @DrawableRes
    private int mPauseIcon;
    @DrawableRes
    private int mPlayIcon;
    @DrawableRes
    private int mShrinkIcon;
    @DrawableRes
    private int mStretchIcon;

    //top layout
    private View mTopLayout;
    private ImageButton mBackButton;
    private TextView mTitleText;

    //center layout
    private View mCenterLayout;
    private ImageView mCenterImage;
    private ProgressBar mCenterProgress;
    private float mCurBrightness = -1;
    private int mCurVolume = -1;
    private AudioManager mAudioManager;
    private int mMaxVolume;

    //bottom layout
    private View mBottomLayout;
    private ImageButton mPauseButton;
    private ImageButton mFullscreenButton;

    private Handler mHandler = new ControllerViewHandler(this);

    public VideoControllerView(Builder builder) {
        super(builder.context);
        this.mContext = builder.context;
        this.mMediaPlayerControlListener = builder.mediaPlayerControlListener;

        this.mVideoTitle = builder.videoTitle;
        this.mCanSeekVideo = builder.canSeekVideo;
        this.mCanControlVolume = builder.canControlVolume;
        this.mCanControlBrightness = builder.canControlBrightness;
        this.mExitIcon = builder.exitIcon;
        this.mPauseIcon = builder.pauseIcon;
        this.mPlayIcon = builder.playIcon;
        this.mStretchIcon = builder.stretchIcon;
        this.mShrinkIcon = builder.shrinkIcon;
        this.mVideoView = builder.videoView;

        setAnchorView(builder.anchorView);
        this.mVideoView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                toggleControllerView();
                return mMediaPlayerControlListener.isFullScreen();
            }
        });
    }

    public static class Builder {

        private Activity context;
        private boolean canSeekVideo = true;
        private boolean canControlVolume = true;
        private boolean canControlBrightness = true;
        private String videoTitle = "";
        private MediaPlayerControlListener mediaPlayerControlListener;
        private ViewGroup anchorView;
        private View videoView;
        @DrawableRes
        private int exitIcon = R.drawable.video_top_back;
        @DrawableRes
        private int pauseIcon = R.drawable.ic_media_pause;
        @DrawableRes
        private int playIcon = R.drawable.ic_media_play;
        @DrawableRes
        private int shrinkIcon = R.drawable.ic_media_fullscreen_shrink;
        @DrawableRes
        private int stretchIcon = R.drawable.ic_media_fullscreen_stretch;

        //Required
        public Builder(@Nullable Activity context, @Nullable MediaPlayerControlListener mediaControlListener){
            this.context = context;
            this.mediaPlayerControlListener = mediaControlListener;
        }
        public Builder with(@Nullable Activity context) {
            this.context = context;
            return this;
        }

        public Builder withMediaControlListener(@Nullable MediaPlayerControlListener mediaControlListener) {
            this.mediaPlayerControlListener = mediaControlListener;
            return this;
        }

        //Options
        public Builder withVideoTitle(String videoTitle) {
            this.videoTitle = videoTitle;
            return this;
        }

        public Builder withVideoView(@Nullable View videoView){
            this.videoView = videoView;
            return this;
        }

        public Builder exitIcon(@DrawableRes int exitIcon) {
            this.exitIcon = exitIcon;
            return this;
        }

        public Builder pauseIcon(@DrawableRes int pauseIcon) {
            this.pauseIcon = pauseIcon;
            return this;
        }

        public Builder playIcon(@DrawableRes int playIcon) {
            this.playIcon = playIcon;
            return this;
        }

        public Builder shrinkIcon(@DrawableRes int shrinkIcon) {
            this.shrinkIcon = shrinkIcon;
            return this;
        }

        public Builder stretchIcon(@DrawableRes int stretchIcon) {
            this.stretchIcon = stretchIcon;
            return this;
        }

        public Builder canSeekVideo(boolean canSeekVideo) {
            this.canSeekVideo = canSeekVideo;
            return this;
        }

        public Builder canControlVolume(boolean canControlVolume) {
            this.canControlVolume = canControlVolume;
            return this;
        }

        public Builder canControlBrightness(boolean canControlBrightness) {
            this.canControlBrightness = canControlBrightness;
            return this;
        }

        public VideoControllerView build(@Nullable ViewGroup anchorView) {
            this.anchorView = anchorView;
            return new VideoControllerView(this);
        }

    }

    /**
     * Handler prevent leak memory.
     */
    private static class ControllerViewHandler extends Handler {
        private final WeakReference<VideoControllerView> mView;

        ControllerViewHandler(VideoControllerView view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoControllerView view = mView.get();
            if (view == null || view.mMediaPlayerControlListener == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case HANDLER_ANIMATE_OUT:
                    Log.e(TAG, "handleMessage: HANDLER_ANIMATE_OUT" );
                    view.hide();
                    break;
                case HANDLER_UPDATE_PROGRESS://cycle update seek bar progress
                    pos = view.setSeekProgress();
                    if (!view.mIsDragging && view.mIsShowing && view.mMediaPlayerControlListener.isPlaying()) {//just in case
                        //cycle update
                        msg = obtainMessage(HANDLER_UPDATE_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }

    /**
     * Inflate view from exit xml layout
     *
     * @return the root view of {@link VideoControllerView}
     */
    private View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflate.inflate(R.layout.layout_media_controller, null);
        initControllerView();

        return mRootView;
    }

    /**
     * find all views inside {@link VideoControllerView}
     * and init params
     */
    private void initControllerView() {
        //top layout
        mTopLayout = mRootView.findViewById(R.id.layout_top);
        mBackButton = (ImageButton) mRootView.findViewById(R.id.top_back);
        mBackButton.setImageResource(mExitIcon);
        if (mBackButton != null) {
            mBackButton.requestFocus();
            mBackButton.setOnClickListener(mBackListener);
        }

        mTitleText = (TextView) mRootView.findViewById(R.id.top_title);

        //center layout
        mCenterLayout = mRootView.findViewById(R.id.layout_center);
        mCenterLayout.setVisibility(GONE);
        mCenterImage = (ImageView) mRootView.findViewById(R.id.image_center_bg);
        mCenterProgress = (ProgressBar) mRootView.findViewById(R.id.progress_center);

        //bottom layout
        mBottomLayout = mRootView.findViewById(R.id.layout_bottom);
        mPauseButton = (ImageButton) mRootView.findViewById(R.id.bottom_pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mFullscreenButton = (ImageButton) mRootView.findViewById(R.id.bottom_fullscreen);
        if (mFullscreenButton != null) {
            mFullscreenButton.requestFocus();
            mFullscreenButton.setOnClickListener(mFullscreenListener);
        }

        mSeekBar = (SeekBar) mRootView.findViewById(R.id.bottom_seekbar);
        if (mSeekBar != null) {
            mSeekBar.setOnSeekBarChangeListener(mSeekListener);
            mSeekBar.setMax(1000);
        }

        mEndTime = (TextView) mRootView.findViewById(R.id.bottom_time);
        mCurrentTime = (TextView) mRootView.findViewById(R.id.bottom_time_current);

        //init formatter
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }


    public void showWithTitle(String videoTitle){
        this.mVideoTitle = videoTitle;
        show();
    }

    /**
     * show controller view
     */
    private void show() {

        if (!mIsShowing && mAnchorView != null) {

            //3s to auto hide controller view
            mHandler.removeCallbacks(mHideRunnable);
            mHandler.postDelayed(mHideRunnable,AUTO_HIDE_TIME);

            //add controller view to bottom of the AnchorView
            FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            adjustUiDisplay();
            mAnchorView.addView(VideoControllerView.this, tlp);

            mIsShowing = true;
            mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS);
            //don't need animate
//            ViewAnimator.putOn(mTopLayout)
//                    .waitForSize(new ViewAnimator.Listeners.Size() {
//                        @Override
//                        public void onSize(ViewAnimator viewAnimator) {
//                            viewAnimator.animate()
//                                    .translationY(-mTopLayout.getHeight(), 0)
//                                    .duration(ANIMATE_TIME)
//                                    .andAnimate(mBottomLayout)
//                                    .translationY(mBottomLayout.getHeight(), 0)
//                                    .duration(ANIMATE_TIME)
//                                    .start(new ViewAnimator.Listeners.Start() {
//                                        @Override
//                                        public void onStart() {
//                                            mIsShowing = true;
//                                            mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS);
//                                        }
//                                    });
//                        }
//                    });
        }

        setSeekProgress();
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
        }
        togglePausePlay();
        toggleFullScreen();
        //update progress
        mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS);

    }

    /**
     * adjust ui display in landscape and portrait
     */
    private void adjustUiDisplay() {
        ViewGroup.LayoutParams params = mBottomLayout.getLayoutParams();
        RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) mTitleText.getLayoutParams();
        if(mMediaPlayerControlListener.isFullScreen()){
            mPauseIcon = R.drawable.ic_media_pause;
            mPlayIcon = R.drawable.ic_media_play;
            mPauseButton.setImageResource(mPauseIcon);
            params.height = (int) getResources().getDimension(R.dimen.video_bottom_landscape);
            params1.topMargin = (int) getResources().getDimension(R.dimen.video_top_landscape);
            mBackButton.setVisibility(VISIBLE);
        }else {
            mPauseIcon = R.drawable.ic_media_pause_small;
            mPlayIcon = R.drawable.ic_media_play_small;
            mPauseButton.setImageResource(mPauseIcon);
            params.height = (int) getResources().getDimension(R.dimen.video_bottom_portrait);
            params1.topMargin = (int) getResources().getDimension(R.dimen.video_top_portrait);
            mBackButton.setVisibility(GONE);
        }
        mBottomLayout.setLayoutParams(params);
        //ADD ?
        mTitleText.setLayoutParams(params1);
    }

    /**
     * toggle {@link VideoControllerView} show or not
     * this can be called when {@link View#onTouchEvent(MotionEvent)} happened
     */
    public void toggleControllerView() {

        if (!isShowing()) {
            show();
        } else {
            //animate out controller view
            Message msg = mHandler.obtainMessage(HANDLER_ANIMATE_OUT);
            //remove exist one first
            mHandler.removeMessages(HANDLER_ANIMATE_OUT);
            mHandler.sendMessageDelayed(msg, AUTO_HIDE_TIME);
        }
    }

    public void removeAllCallBacks(){
        hide();
    }


    /**
     * if {@link VideoControllerView} is visible
     *
     * @return showing or not
     */
    public boolean isShowing() {
        return mIsShowing;
    }

    /**
     * hide controller view with animation
     * With custom animation
     */
    public void hide() {
        if (mAnchorView == null) {
            return;
        }

        mAnchorView.removeView(VideoControllerView.this);
        mHandler.removeMessages(HANDLER_UPDATE_PROGRESS);
        mIsShowing = false;
        //don't need animate
//        ViewAnimator.putOn(mTopLayout)
//                .animate()
//                .translationY(-mTopLayout.getHeight())
//                .duration(ANIMATE_TIME)
//
//                .andAnimate(mBottomLayout)
//                .translationY(mBottomLayout.getHeight())
//                .duration(ANIMATE_TIME)
//                .end(new ViewAnimator.Listeners.End() {
//                    @Override
//                    public void onEnd() {
//                        mAnchorView.removeView(VideoControllerView.this);
//                        mHandler.removeMessages(HANDLER_UPDATE_PROGRESS);
//                        mIsShowing = false;
//                    }
//                });
    }

    /**
     * convert string to time
     *
     * @param timeMs time to be formatted
     * @return 00:00:00
     */
    private String stringToTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * set {@link #mSeekBar} progress
     * and video play time {@link #mCurrentTime}
     *
     * @return current play position
     */
    private int setSeekProgress() {
        if (mMediaPlayerControlListener == null || mIsDragging) {
            return 0;
        }

        int position = mMediaPlayerControlListener.getCurrentPosition();
        int duration = mMediaPlayerControlListener.getDuration();
        if (mSeekBar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mSeekBar.setProgress((int) pos);
            }
            //get buffer percentage
            int percent = mMediaPlayerControlListener.getBufferPercentage();
            //set buffer progress
            mSeekBar.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringToTime(duration));
        if (mCurrentTime != null) {
            Log.e(TAG, "position:" + position + " -> duration:" + duration);
            mCurrentTime.setText(stringToTime(position));
            if(mMediaPlayerControlListener.isComplete()){
                mCurrentTime.setText(stringToTime(duration));
                mSeekBar.setProgress(1000);
            }
        }
        mTitleText.setText(mVideoTitle);
        mPauseButton.setImageResource(mMediaPlayerControlListener.isPlaying() ? mPauseIcon : mPlayIcon);
        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mHandler.removeCallbacks(mHideRunnable);
        mHandler.postDelayed(mHideRunnable,100);

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                mCurVolume = -1;
                mCurBrightness = -1;
                mCenterLayout.setVisibility(GONE);
                mPauseButton.setVisibility(VISIBLE);
//                break;// do need bread,should let gestureDetector to handle event
            default://gestureDetector handle other MotionEvent
                if(mGestureDetector != null)
                    mGestureDetector.onTouchEvent(event);
        }

        //ListView need handle touchEvent,so portrait screen need return false
        return mMediaPlayerControlListener.isFullScreen();

    }

    /**
     * toggle pause or play
     */
    private void togglePausePlay() {
        if (mRootView == null || mPauseButton == null || mMediaPlayerControlListener == null) {
            return;
        }

        if (mMediaPlayerControlListener.isPlaying()) {
            mPauseButton.setImageResource(mPauseIcon);
        } else {
            mPauseButton.setImageResource(mPlayIcon);
        }
    }

    /**
     * toggle full screen or not
     */
    public void toggleFullScreen() {
        if (mRootView == null || mFullscreenButton == null || mMediaPlayerControlListener == null) {
            return;
        }

        if (mMediaPlayerControlListener.isFullScreen()) {
            mFullscreenButton.setImageResource(mShrinkIcon);
        } else {
            mFullscreenButton.setImageResource(mStretchIcon);
        }
    }

    private void doPauseResume() {
        if (mMediaPlayerControlListener == null) {
            return;
        }

        if (mMediaPlayerControlListener.isPlaying()) {
            mMediaPlayerControlListener.pause();
        } else {
            mMediaPlayerControlListener.start();
        }
        togglePausePlay();
    }

    private void doToggleFullscreen() {
        if (mMediaPlayerControlListener == null) {
            return;
        }

        mMediaPlayerControlListener.toggleFullScreen();
    }

    /**
     * Seek bar drag listener
     */
    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            show();
            mIsDragging = true;
            mHandler.removeMessages(HANDLER_UPDATE_PROGRESS);
            mHandler.removeCallbacks(mHideRunnable);
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mMediaPlayerControlListener == null) {
                return;
            }

            if (!fromuser) {
                return;
            }

            long duration = mMediaPlayerControlListener.getDuration();
            long newPosition = (duration * progress) / 1000L;
//            mMediaPlayerControlListener.seekTo((int) newPosition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringToTime((int) newPosition));
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            long duration = mMediaPlayerControlListener.getDuration();
            long newPosition = (duration * bar.getProgress()) / 1000L;
            mMediaPlayerControlListener.seekTo((int) newPosition);
            mIsDragging = false;
            setSeekProgress();
            togglePausePlay();
            show();
            mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS);
            mHandler.removeCallbacks(mHideRunnable);
            mHandler.postDelayed(mHideRunnable,AUTO_HIDE_TIME);
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mSeekBar != null) {
            mSeekBar.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }


    /**
     * set top back click listener
     */
    private View.OnClickListener mBackListener = new View.OnClickListener() {
        public void onClick(View v) {
            mMediaPlayerControlListener.exit();
        }
    };


    /**
     * set pause click listener
     */
    private View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show();
        }
    };

    /**
     * set full screen click listener
     */
    private View.OnClickListener mFullscreenListener = new View.OnClickListener() {
        public void onClick(View v) {
            doToggleFullscreen();
            show();
        }
    };

    /**
     * setMediaPlayerControlListener update play state
     *
     * @param mediaPlayerListener self
     */
    public void setMediaPlayerControlListener(MediaPlayerControlListener mediaPlayerListener) {
        mMediaPlayerControlListener = mediaPlayerListener;
        togglePausePlay();
        toggleFullScreen();
    }

    /**
     * set anchor view
     *
     * @param view view that hold controller view
     */
    private void setAnchorView(ViewGroup view) {
        mAnchorView = view;
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        //remove all before add view
        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);

        setGestureListener();
    }

    /**
     * set gesture listen to control media player
     * include screen brightness and volume of video
     * and seek video play
     */
    private void setGestureListener() {

        if(mCanControlVolume) {
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }

        mGestureDetector = new GestureDetector(mContext, new ViewGestureListener(mContext, this));
    }


    @Override
    public void onSingleTap() {
        toggleControllerView();
    }

    @Override
    public void onHorizontalScroll(boolean seekForward) {
        if (mCanSeekVideo) {
            if (seekForward) {// seek forward
                seekForWard();
            } else {  //seek backward
                seekBackWard();
            }
        }
    }

    private void seekBackWard() {
        if (mMediaPlayerControlListener == null) {
            return;
        }

        int pos = mMediaPlayerControlListener.getCurrentPosition();
        pos -= PROGRESS_SEEK;
        mMediaPlayerControlListener.seekTo(pos);
        setSeekProgress();

        show();
    }

    private void seekForWard() {
        if (mMediaPlayerControlListener == null) {
            return;
        }

        int pos = mMediaPlayerControlListener.getCurrentPosition();
        pos += PROGRESS_SEEK;
        mMediaPlayerControlListener.seekTo(pos);
        setSeekProgress();

        show();
    }

    @Override
    public void onVerticalScroll(float percent, int direction) {
        //only landscape can control volume and brightness
        if(mMediaPlayerControlListener.isFullScreen()) {
            if (direction == ViewGestureListener.SWIPE_LEFT) {
                if (mCanControlBrightness) {
                    mCenterImage.setImageResource(R.drawable.video_bright_bg);
                    mPauseButton.setVisibility(GONE);
                    updateBrightness(percent);
                }
            } else {
                if (mCanControlVolume) {
                    mCenterImage.setImageResource(R.drawable.video_volume_bg);
                    mPauseButton.setVisibility(GONE);
                    updateVolume(percent);
                }
            }
        }
    }

    /**
     * update volume by seek percent
     *
     * @param percent seek percent
     */
    private void updateVolume(float percent) {

        mCenterLayout.setVisibility(VISIBLE);

        if (mCurVolume == -1) {
            mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mCurVolume < 0) {
                mCurVolume = 0;
            }
        }

        int volume = (int) (percent * mMaxVolume) + mCurVolume;
        if (volume > mMaxVolume) {
            volume = mMaxVolume;
        }

        if (volume < 0) {
            volume = 0;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);

        int progress = volume * 100 / mMaxVolume;
        mCenterProgress.setProgress(progress);
    }

    /**
     * update brightness by seek percent
     *
     * @param percent seek percent
     */
    private void updateBrightness(float percent) {

        if (mCurBrightness == -1) {
            mCurBrightness = mContext.getWindow().getAttributes().screenBrightness;
            if (mCurBrightness <= 0.01f) {
                mCurBrightness = 0.01f;
            }
        }

        mCenterLayout.setVisibility(VISIBLE);

        WindowManager.LayoutParams attributes = mContext.getWindow().getAttributes();
        attributes.screenBrightness = mCurBrightness + percent;
        if (attributes.screenBrightness >= 1.0f) {
            attributes.screenBrightness = 1.0f;
        } else if (attributes.screenBrightness <= 0.01f) {
            attributes.screenBrightness = 0.01f;
        }
        mContext.getWindow().setAttributes(attributes);

        float p = attributes.screenBrightness * 100;
        mCenterProgress.setProgress((int) p);

    }

    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };


    /**
     * Interface of Media Controller View Which can be callBack
     * when {@link android.media.MediaPlayer} or some other media
     * players work
     */
    public interface MediaPlayerControlListener {
        /**
         * start play video
         */
        void start();

        /**
         * pause video
         */
        void pause();

        /**
         * get video total time
         *
         * @return total time
         */
        int getDuration();

        /**
         * get video current position
         *
         * @return current position
         */
        int getCurrentPosition();

        /**
         * seek video to exactly position
         *
         * @param position position
         */
        void seekTo(int position);

        /**
         * video is playing state
         *
         * @return is video playing
         */
        boolean isPlaying();

        /**
         * video is complete
         * @return complete or not
         */
        boolean isComplete();

        /**
         * get buffer percent
         *
         * @return percent
         */
        int getBufferPercentage();

        /**
         * video is full screen
         * in order to control image src...
         *
         * @return fullScreen
         */
        boolean isFullScreen();

        /**
         * toggle fullScreen
         */
        void toggleFullScreen();

        /**
         * exit media player
         */
        void exit();
    }
}