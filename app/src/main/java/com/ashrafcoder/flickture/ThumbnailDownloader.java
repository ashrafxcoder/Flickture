package com.ashrafcoder.flickture;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.ashrafcoder.flickture.flickr.FlickrFetchr;

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = ThumbnailDownloader.class.getName();
    private static final int MESSAGE_DOWNLOAD = 0;

    private Handler mHandler;
    private Map<T,String> requestMap = Collections.synchronizedMap(new HashMap<T,String>());
    private Handler mResponseHandler;
    private Listener<T> mListener;
    //LruCache added to increase the performance and decrease network bandwidth
    private LruCache<String, Bitmap> mMemoryCache;

    // Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    // Use 1/4th of the available memory for this memory cache.
    final int cacheSize = maxMemory / 4;



    public interface Listener<Handle> {
        void onThumbnailDownloaded(Handle handle, Bitmap thumbnail);
    }
    
    public void setListener(Listener<T> listener) {
        mListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }
    
    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    @SuppressWarnings("unchecked")
                    T handle = (T)msg.obj;
                    Log.i(TAG, "Got a request for url: " + requestMap.get(handle));
                    handleRequest(handle);
                }
            }
        };
    }

    private void handleRequest(final T handle) {
        try {
            final String url = requestMap.get(handle);
            if (url == null) 
                return;


            final Bitmap bitmap = getBitmapFromMemCache(url);
            if (bitmap != null) {
                //mImageView.setImageBitmap(bitmap);
                postResult(handle, url, bitmap);
            } else {
                byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                Bitmap bitmap2 = BitmapFactory
                        .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                addBitmapToMemoryCache(url, bitmap2);
                postResult(handle, url, bitmap2);
            }

        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

    private void postResult(final T handle, final String url, final Bitmap bitmap) {
        mResponseHandler.post(new Runnable() {
            public void run() {
                if (requestMap.get(handle) != url)
                    return;

                requestMap.remove(handle);
                mListener.onThumbnailDownloaded(handle, bitmap);
            }
        });
    }

    public void queueThumbnail(T handle, String url) {
        requestMap.put(handle, url);
        
        mHandler
            .obtainMessage(MESSAGE_DOWNLOAD, handle)
            .sendToTarget();
    }
    
    public void clearQueue() {
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
}
