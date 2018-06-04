package io.stanwood.framework.dialog;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

public class ImagePreloader implements LifecycleObserver {

    private boolean isDelivered;
    private Lifecycle lifecycle;
    private LoaderCallback callback;
    private int counter = 0;
    private RequestListener requestListener = new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            counter--;
            if (counter == 0) {
                enable();
            }
            return false;
        }
    };

    private ImagePreloader(Context context, List<String> urls, Lifecycle lifecycle, LoaderCallback callback) {
        counter = urls.size();
        for (int size = urls.size(), i = 0; i < size; i++) {
            Glide.with(context).load(urls.get(i)).listener(requestListener).preload();
        }
        this.lifecycle = lifecycle;
        this.callback = callback;
        lifecycle.addObserver(this);
    }

    static ImagePreloader create(Context context, List<String> urls, Lifecycle lifecycle, LoaderCallback callback) {
        return new ImagePreloader(context, urls, lifecycle, callback);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void destroy() {
        lifecycle.removeObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void resume() {
        enable();
    }

    void enable() {
        if (counter == 0 && !isDelivered && lifecycle.getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            isDelivered = true;
            callback.onLoadFinished();
        }
    }

    public interface LoaderCallback {
        void onLoadFinished();
    }
}