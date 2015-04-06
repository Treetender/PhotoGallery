package com.bignerdranch.android.photogallery;

import android.support.v4.app.Fragment;

/**
 * Created by treetender on 4/5/15.
 */
public class PhotoGalleryActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new PhotoGalleryFragment();
    }
}
