package com.brucetoo.listvideoplay.videomanage.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Bruce Too
 * On 8/25/16.
 * At 10:15
 * ListView Normal ViewHolder
 */
public class ViewHolder {

    private SparseArray<View> holder;
    public View convertView;

    public ViewHolder() {
        holder = new SparseArray<View>();
    }

    public static ViewHolder newInstance(Context context, View convertView, ViewGroup parent, int layoutId) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            holder.convertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
            holder.convertView.setTag(layoutId,holder);
        } else {
            holder = (ViewHolder) convertView.getTag(layoutId);
        }
        return holder;
    }

    public ViewHolder setView(int id, View view) {
        holder.put(id, view);
        return this;
    }

    public View getView() {
        return convertView;
    }

    public <T extends View> T getView(int viewId) {
        return retrieveView(viewId);
    }

    @SuppressWarnings("unchecked")
    private <T extends View> T retrieveView(int viewId) {
        View view = holder.get(viewId);
        if (view == null) {
            view = convertView.findViewById(viewId);
            holder.put(viewId, view);
        }
        return (T) view;
    }

    public ViewHolder setText(int id, String value) {
        TextView view = retrieveView(id);
        view.setText(value);
        return this;
    }

    public ViewHolder setText(int id, SpannableString value) {
        TextView view = retrieveView(id);
        view.setText(value);
        return this;
    }

    public ViewHolder setText(int id, int valueResId) {
        TextView view = retrieveView(id);
        view.setText(valueResId);
        return this;
    }

    public ViewHolder setImageResource(int viewId, int imageResId) {
        ImageView view = retrieveView(viewId);
        view.setImageResource(imageResId);
        return this;
    }

    public ViewHolder setBackgroundColor(int viewId, int color) {
        View view = retrieveView(viewId);
        view.setBackgroundColor(color);
        return this;
    }

    public ViewHolder setBackgroundRes(int viewId, int backgroundRes) {
        View view = retrieveView(viewId);
        view.setBackgroundResource(backgroundRes);
        return this;
    }

    public ViewHolder setBackgroundDrawable(int viewId, Drawable drawable) {
        View view = retrieveView(viewId);
        view.setBackgroundDrawable(drawable);
        return this;
    }

    public ViewHolder setTextColor(int viewId, int textColor) {
        TextView view = retrieveView(viewId);
        view.setTextColor(textColor);
        return this;
    }

    public ViewHolder setTextColorRes(int viewId, int textColorRes) {
        TextView view = retrieveView(viewId);
        view.setTextColor(textColorRes);
        return this;
    }

    public ViewHolder setImageDrawable(int viewId, Drawable drawable) {
        ImageView view = retrieveView(viewId);
        view.setImageDrawable(drawable);
        return this;
    }

    public ViewHolder setImageBitmap(int viewId, Bitmap bitmap) {
        ImageView view = retrieveView(viewId);
        view.setImageBitmap(bitmap);
        return this;
    }

    @SuppressLint("NewApi")
    public ViewHolder setAlpha(int viewId, float value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            retrieveView(viewId).setAlpha(value);
        } else {
            // Pre-honeycomb hack to set Alpha value
            AlphaAnimation alpha = new AlphaAnimation(value, value);
            alpha.setDuration(0);
            alpha.setFillAfter(true);
            retrieveView(viewId).startAnimation(alpha);
        }
        return this;
    }

    public ViewHolder setVisible(int viewId, boolean visible) {
        View view = retrieveView(viewId);
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }

    public ViewHolder linkify(int viewId) {
        TextView view = retrieveView(viewId);
        Linkify.addLinks(view, Linkify.ALL);
        return this;
    }

    public ViewHolder setTypeface(int viewId, Typeface typeface) {
        TextView view = retrieveView(viewId);
        view.setTypeface(typeface);
        view.setPaintFlags(view.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        return this;
    }

    public ViewHolder setTypeface(Typeface typeface, int... viewIds) {
        for (int viewId : viewIds) {
            TextView view = retrieveView(viewId);
            view.setTypeface(typeface);
            view.setPaintFlags(view.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        }
        return this;
    }

    public ViewHolder setProgress(int viewId, int progress) {
        ProgressBar view = retrieveView(viewId);
        view.setProgress(progress);
        return this;
    }

    public ViewHolder setProgress(int viewId, int progress, int max) {
        ProgressBar view = retrieveView(viewId);
        view.setMax(max);
        view.setProgress(progress);
        return this;
    }

    public ViewHolder setMax(int viewId, int max) {
        ProgressBar view = retrieveView(viewId);
        view.setMax(max);
        return this;
    }
}
