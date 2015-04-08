package com.bignerdranch.android.photogallery;

public class GalleryItem
{
    private String mId;
    private String mUrl;
    private String mCaption;

    @Override
    public String toString()
    {
        // TODO: Implement this method
        return mCaption;
    }

    public void setId(String id)
    {
        this.mId = id;
    }

    public String getId()
    {
        return mId;
    }

    public void setUrl(String url)
    {
        this.mUrl = url;
    }

    public String getUrl()
    {
        return mUrl;
    }

    public void setCaption(String caption)
    {
        this.mCaption = caption;
    }

    public String getCaption()
    {
        return mCaption;
    }
}
