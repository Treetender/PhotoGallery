package com.bignerdranch.android.photogallery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.support.v7.widget.SearchView;
import java.util.ArrayList;

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
            Activity activity = getActivity();
            if (activity == null)
                return new ArrayList<>();

            String query = PreferenceManager.getDefaultSharedPreferences(activity)
                                            .getString(FlickrFetcher.PREF_SEARCH_QUERY, null);
            if (query != null)
                return new FlickrFetcher().search(query);
            else
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
        setHasOptionsMenu(true);
        updateItems();
        
        mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            @Override
            public void onDownloadedThumbnail(ImageView imageView, Bitmap bitmap) {
                if(isVisible()) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
        Log.i(TAG, "Background Thread Started");
    }

    public void updateItems() {
        new FetchItemsTask().execute();
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
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailThread.clearQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mGridView = (GridView)v.findViewById(R.id.gridView);
        
        setupAdapter();
        return v;
    }

    @Override
    @TargetApi(11)
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            MenuItem searchItem = menu.findItem(R.id.menu_item_search);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(
                    new ComponentName(getActivity().getApplicationContext(), PhotoGalleryActivity.class)));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_search:
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                    getActivity().onSearchRequested();
                return true;
            case R.id.menu_item_clear:
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                                 .edit()
                                 .putString(FlickrFetcher.PREF_SEARCH_QUERY, null)
                                 .commit();
                updateItems();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
