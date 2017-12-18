package com.brucetoo.listvideoplay.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;

import com.brucetoo.listvideoplay.R;
import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.TransitionManager;

/**
 * Created by Bruce Too
 * On 18/12/2017.
 * At 21:37
 */

public class ConstraintFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_constraint_start,container,false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.backgroundImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShow){
                    hide((ConstraintLayout) view);
                    isShow = false;
                }else {
                    show((ConstraintLayout) view);
                    isShow = true;
                }
            }
        });
    }

    boolean isShow;
    private void show(ConstraintLayout root){
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(getContext(), R.layout.fragment_constraint_end);
        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
        changeBounds.setDuration(1200);

        TransitionManager.beginDelayedTransition(root,changeBounds);

        constraintSet.applyTo((ConstraintLayout) root);
    }

    private void hide(ConstraintLayout root){
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(getContext(), R.layout.fragment_constraint_start);
        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
        changeBounds.setDuration(1200);

        TransitionManager.beginDelayedTransition(root,changeBounds);

        constraintSet.applyTo((ConstraintLayout) root);
    }
}
