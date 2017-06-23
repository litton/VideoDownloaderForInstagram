package com.zxmark.videodownloader.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.facebook.ads.NativeAd;
import com.zxmark.videodownloader.adapter.MainDownloadingRecyclerAdapter;
import com.zxmark.videodownloader.bean.VideoBean;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanlitao on 6/22/17.
 */

public class DownloadContentItem implements BaseColumns {

    public static String TABLE_NAME = "download_content";


    public static final int PAGE_MIME_TYPE_IMAGE = 0;
    public static final int PAGE_MIME_TYPE_VIDEO = 1;
    public static final int TYPE_NORMAL_ITEM = 0;
    public static final int TYPE_HEADER_ITEM = 1;
    public static final int TYPE_HOWTO_ITEM = 2;
    public static final int TYPE_FACEBOOK_AD = 3;

    public static final int PAGE_STATUS_DOWNLOADING = 0;
    public static final int PAGE_STATUS_DOWNLOAD_FAILED = 1;
    public static final int PAGE_STATUS_DOWNLOAD_FINISHED = 2;


    public static Uri CONTENT_URI = Uri.parse("content://" + DownloaderContentProvider.AUTHORITY + "/" + TABLE_NAME);

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.imob.videodownloader.content";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.imob.videodownloader.content";

    public static final String PAGE_URL = "page_url";
    public static final String PAGE_HOME = "page_home";
    public static final String PAGE_THUMBNAIL = "page_thumbnail";
    public static final String PAGE_TITLE = "page_title";
    public static final String PAGE_DESCRIPTION = "page_desc";
    public static final String HASH_TAGS = "hash_tags";
    public static final String PAGE_MIME_TYPE = "mime_type";
    public static final String PAGE_DOWNLOAD_FILE_COUNT = "count";
    public static final String PAGE_STATUS = "page_status";

    public static final String DEFAULT_ORDERBY = _ID + "  DESC";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + _ID + " INTEGER PRIMARY KEY,"
            + PAGE_URL + " VARCHAR(255)," + PAGE_HOME + " VARCHAR(255)," + PAGE_THUMBNAIL + " VARCHAR(255)," + PAGE_TITLE + " VARCHAR(255) ," + PAGE_DESCRIPTION + " VARCHAR(255) ," + HASH_TAGS + " VARCHAR(255)," + PAGE_MIME_TYPE + " int default 0," + PAGE_DOWNLOAD_FILE_COUNT + " int," + PAGE_STATUS + " int default 0);";


    public int pageId;
    public String pageURL;
    public String pageHOME;
    public String pageThumb;
    public String pageTitle;
    public String pageDesc;
    public String pageTags;
    public int mimeType;
    public int fileCount;
    public int pageStatus;

    public int itemType = TYPE_NORMAL_ITEM;

    private ContentValues mContentValues;

    public DownloadContentItem() {

    }

    public void setPageURL(String pageURL) {
        this.pageURL = pageURL;
    }

    public void setPageHome(String pageHome) {
        this.pageHOME = pageHome;
    }

    public void setPageThumb(String thumb) {
        this.pageThumb = thumb;
    }


    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public void setPageDesc(String pageDesc) {
        this.pageDesc = pageDesc;
    }

    public void setPageTags(String pageTags) {
        this.pageTags = pageTags;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public void setPageStatus(int status) {
        this.pageStatus = status;
    }

    public void setMimeType(int mimeType) {
        this.mimeType = mimeType;
    }

    public int getMimeType() {
        return getVideoCount() > 0 ? PAGE_MIME_TYPE_VIDEO : PAGE_MIME_TYPE_IMAGE;
    }


    public static DownloadContentItem fromCusor(Cursor cursor) {
        DownloadContentItem item = new DownloadContentItem();
        item.pageId = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContentItem._ID));
        item.pageURL = cursor.getString(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_URL));
        item.pageHOME = cursor.getString(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_HOME));
        item.pageThumb = cursor.getString(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_THUMBNAIL));
        item.pageTitle = cursor.getString(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_TITLE));
        item.pageDesc = cursor.getString(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_DESCRIPTION));
        item.pageTags = cursor.getString(cursor.getColumnIndexOrThrow(DownloadContentItem.HASH_TAGS));
        item.mimeType = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_MIME_TYPE));
        item.fileCount = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_DOWNLOAD_FILE_COUNT));
        LogUtil.e("item", "fromCursor.fileCount=" + item.fileCount);
        item.pageStatus = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadContentItem.PAGE_STATUS));
        item.itemType = TYPE_NORMAL_ITEM;
        return item;
    }

    public static ContentValues from(DownloadContentItem item) {
        ContentValues cv = new ContentValues();
        cv.put(PAGE_URL, item.pageURL);
        cv.put(PAGE_HOME, item.getPageHome());
        cv.put(PAGE_THUMBNAIL, item.pageThumb);
        cv.put(PAGE_TITLE, item.pageTitle);
        cv.put(PAGE_DESCRIPTION, item.pageDesc);
        cv.put(HASH_TAGS, item.pageTags);
        cv.put(PAGE_MIME_TYPE, item.getMimeType());
        cv.put(PAGE_DOWNLOAD_FILE_COUNT, item.getFileCount());
        LogUtil.e("item", "pageHome:" +item.getPageHome());
        cv.put(PAGE_STATUS, item.pageStatus);
        return cv;
    }


    public List<String> futureVideoList;
    public List<String> futureImageList;

    public String getPageHome() {
        return DownloadUtil.getDownloadItemDirectory(pageURL);
    }


    public void addVideo(String path) {
        if (futureVideoList == null) {
            futureVideoList = new ArrayList<>();
        }
        if (!futureVideoList.contains(path)) {
            futureVideoList.add(path);
        }
    }

    public void addImage(String path) {
        if (futureImageList == null) {
            futureImageList = new ArrayList<>();
        }

        if (mimeType == PAGE_MIME_TYPE_VIDEO) {
            if (pageThumb != null && pageThumb.equals(path)) {
                return;
            }
        }
        if (!futureImageList.contains(path)) {
            futureImageList.add(path);
        }
    }

    public List<String> getVideoList() {
        return futureVideoList;
    }

    public List<String> getImageList() {
        return futureImageList;
    }


    public int getVideoCount() {
        return futureVideoList == null ? 0 : futureVideoList.size();
    }

    public int getImageCount() {
        return futureImageList == null ? 0 : futureImageList.size();
    }

    public int getFileCount() {
        return getVideoCount() + getImageCount();
    }

    public List<String> getDownloadContentList() {
        List<String> dataList = new ArrayList<>();
        if (getVideoCount() > 0) {
            dataList.addAll(futureVideoList);
        }
        if (getImageCount() > 0) {
            dataList.addAll(futureImageList);
        }
        return dataList;
    }


    public boolean haveVideos() {
        return futureVideoList != null && futureVideoList.size() > 0;
    }

    public boolean haveImages() {
        return futureImageList != null && futureImageList.size() > 0;
    }


    public String getTargetDirectory(String fileUrl) {
        return DownloadUtil.getDownloadTargetDir(getPageHome(), DownloadUtil.getFileNameByUrl(fileUrl));
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DownloadContentItem) {
            DownloadContentItem bean = (DownloadContentItem) obj;
            if (itemType == TYPE_NORMAL_ITEM) {
                return pageURL.equals(bean.pageURL);
            } else {
                if (itemType == DownloadContentItem.TYPE_FACEBOOK_AD) {
                    return false;
                }
                if (itemType == DownloadContentItem.TYPE_HOWTO_ITEM) {
                    if (itemType == bean.itemType) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public NativeAd facebookNativeAd;
}
