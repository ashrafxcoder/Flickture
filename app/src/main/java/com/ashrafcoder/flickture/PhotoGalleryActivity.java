package com.ashrafcoder.flickture;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.GridView;

import com.ashrafcoder.flickture.flickr.FlickrFetchr;
import com.ashrafcoder.flickture.fragment.PhotoGalleryFragment;
import com.ashrafcoder.flickture.fragment.SingleFragmentActivity;
import com.ashrafcoder.flickture.model.GalleryItem;

import java.util.ArrayList;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    private static final String TAG = PhotoGalleryActivity.class.getName();

    @Override
    public Fragment createFragment() {
        return new PhotoGalleryFragment();
    }


    @Override
    public void onNewIntent(Intent intent) {
        PhotoGalleryFragment fragment = (PhotoGalleryFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i(TAG, "Received a new search query: " + query);
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putString(FlickrFetchr.PREF_SEARCH_QUERY, query)
                    .commit();
        }
        fragment.updateItems();
    }
}
