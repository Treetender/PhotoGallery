package com.bignerdranch.android.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ThumbnailDownloader<Token> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    
    Handler mHandler, mResponseHandler;
    Map<Token,String> requestMap = Collections.synchronizedMap(new HashMap<Token,String>());
    Listener<Token> mListener;
  
    public interface Listener<Token> {
        void onDownloadedThumbnail(Token token, Bitmap bitmap);
    }
    
    public void setListener(Listener<Token> listener) {
        mListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler){
        super(TAG);
        mResponseHandler = responseHandler;
    }
    
    public void queueThumbnail(Token token, String url) {
         requestMap.put(token,url);
         mHandler.obtainMessage(MESSAGE_DOWNLOAD, token).sendToTarget();
    }
    
    @SuppressLint("HanderLeak")
    @Override
    protected void onLooperPrepared()
    {
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if(msg.what == MESSAGE_DOWNLOAD) {
                    @SuppressWarnings("unchecked")
                    Token token = (Token)msg.obj;
                    Log.i(TAG, "Received request for URL: " + requestMap.get(token));
                    handleRequest(token);
                }
            }
        };
    }

    public void clearQueue() {
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }
    
    private void handleRequest(final Token token) {
        try {
            final String url = requestMap.get(token);
            if (url == null) return;
            
            byte[] bitmapBytes = new FlickrFetcher().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap Created");
            
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(!requestMap.get(token).equals(url))
                        return;
                    requestMap.remove(token);
                    mListener.onDownloadedThumbnail(token, bitmap);
                }
            });
        }
        catch(IOException ioe) {
            Log.e(TAG, "Failed to download image", ioe);
        }
    }
}
