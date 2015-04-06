package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by treetender on 4/5/15.
 */
public class FlickrFetcher {
    public static final String TAG = "FlickrFetcher";

    private static final String API_KEY = "558d77f708dd3a379603984bf599623a";
    private static final String ENDPOINT = "http://api.flickr.com/services/rest/";
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String PARAM_EXTRAS = "extras";
    private static final String EXTRA_SMALL_URL = "url_s";

    byte[] getUrlBytes(String urlspec) throws IOException {
        URL url = new URL(urlspec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        }
        finally {
            connection.disconnect();
        }
    }

    public String getUrl(String urlspec) throws IOException {
        return new String(getUrlBytes(urlspec));
    }

    public void fetchItems() {
        try {
            String url = Uri.parse(ENDPOINT).buildUpon()
                            .appendQueryParameter("method", METHOD_GET_RECENT)
                            .appendQueryParameter("api_key", API_KEY)
                            .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                            .build().toString();
            String xmlString = getUrl(url);
            Log.i(TAG, "Received xml: " + xmlString);
        }
        catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
    }
}
