package com.ashrafcoder.flickture;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;

import com.ashrafcoder.flickture.fragment.PhotoGalleryFragment;
import com.ashrafcoder.flickture.fragment.SingleFragmentActivity;
import com.ashrafcoder.flickture.model.GalleryItem;

import java.util.ArrayList;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return new PhotoGalleryFragment();
    }
}
