package com.bignerdranch.android.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.io.IOException;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

/**
 * Created by treetender on 4/5/15.
 */
public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";
    GridView mGridView;
    ArrayList<GalleryItem> mItems;
    ThumbnailDownloader<ImageView> mThumbnailThread;

    private class FetchItemsTask extends AsyncTask<Void, Void, ArrayList<GalleryItem>> {
        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params) {
            return new FlickrFetcher().fetchItems();
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> result)
        {
             mItems = result;
             setupAdapter();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();
        
        mThumbnailThread = new ThumbnailDownloader<ImageView>();
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
        Log.i(TAG, "Background Thread Started");
    }

    @Override
    public void onDestroy()
    {
        // TODO: Implement this method
        super.onDestroy();
        mThumbnailThread.quit();
        Log.i(TAG, "Background Thread Destroyed");
    }
    
    

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mGridView = (GridView)v.findViewById(R.id.gridView);
        
        setupAdapter();
        return v;
    }
    
    void setupAdapter() {
        if (getActivity() == null || mGridView == null) 
            return;
            
        if(mItems != null) {
            mGridView.setAdapter(new GalleryAdapter(mItems));
        }
        else {
            mGridView.setAdapter(null);
        }
    }
    private class GalleryAdapter extends ArrayAdapter<GalleryItem>{
        public GalleryAdapter(ArrayList<GalleryItem> items) {
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if(convertView == null) {
                convertView = getActivity().getLayoutInflater()
                    .inflate(R.layout.gallery_item, parent, false);
            } 
            ImageView img = (ImageView)convertView.findViewById(R.id.gallery_item_imageView);
            img.setImageResource(android.R.drawable.gallery_thumb);
            
            GalleryItem item = getItem(position);
            mThumbnailThread.queueThumbnail(img, item.getUrl());
            
            return convertView;
        }
    }
}
