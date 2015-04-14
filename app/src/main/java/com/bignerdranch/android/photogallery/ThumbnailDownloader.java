package com.bignerdranch.android.photogallery;
import android.os.HandlerThread;
import android.util.Log;
import android.os.Handler;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import android.os.Message;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.annotation.SuppressLint;

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
    
    
    public ThumbnailDownloader() {
        super(TAG);
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
    
    private void handleRequest(Token token) {
        try {
            final String url = requestMap.get(token);
            if (url == null) return;
            
            byte[] bitmapBytes = new FlickrFetcher().getUrlBytes(url);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap Created");
        }
        catch(IOException ioe) {
            Log.e(TAG, "Failed to download image", ioe);
        }
    }
    
    
}
