package com.brucetoo.listvideoplay.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.brucetoo.listvideoplay.MainActivity;
import com.brucetoo.listvideoplay.R;
import com.brucetoo.videoplayer.IViewTracker;
import com.brucetoo.videoplayer.Tracker;
import com.brucetoo.videoplayer.VisibleChangeListener;
import com.brucetoo.videoplayer.scrolldetector.ListScrollDetector;
import com.joanzapata.android.BaseAdapterHelper;
import com.joanzapata.android.QuickAdapter;
import com.squareup.picasso.Picasso;

/**
 * Created by Bruce Too
 * On 05/04/2017.
 * At 09:28
 */

public class ListSupportFragment extends Fragment implements View.OnClickListener, VisibleChangeListener {

    public static final String TAG = "ListSupportFragment";
    private ListView mListView;
    private ImageView mImageTop;
    private TextView mTextCalculator;
    private static final float VISIBLE_THRESHOLD = 0.5f;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_support, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = (ListView) view.findViewById(R.id.list_view);
        QuickAdapter<VideoModel> adapter = new QuickAdapter<VideoModel>(getActivity(), R.layout.item_list_view, ListDataGenerater.datas) {
            @Override
            protected void convert(BaseAdapterHelper helper, VideoModel item) {
                ImageView imageCover = (ImageView) helper.getView(R.id.img_cover);
                Picasso.with(getActivity())
                    .load(item.coverImage)
                    .into(imageCover);
                imageCover.setOnClickListener(ListSupportFragment.this);
            }
        };
        mListView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        Tracker.attach(getActivity()).trackView(v).into(new ListScrollDetector(mListView)).visibleListener(this);
        ((MainActivity) getActivity()).addDetailFragment();
    }


    @Override
    public void onVisibleChange(float visibleRatio, IViewTracker tracker) {
        Log.e(TAG, "onVisibleChange : edge -> " + tracker.getEdgeString());
        //only care about vertical scroll
        if(tracker.getEdge() != IViewTracker.LEFT_EDGE || tracker.getEdge() != IViewTracker.RIGHT_EDGE) {
//            if (visibleRatio <= 0.8) {
//                tracker.getVideoLayerView().setVisibility(View.INVISIBLE);
//            } else {
//                tracker.getVideoLayerView().setVisibility(View.VISIBLE);
//            }
        }
    }
}
