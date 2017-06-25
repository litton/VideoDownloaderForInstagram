package com.zxmark.videodownloader.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.zxmark.videodownloader.DownloaderBean;
import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.adapter.MainDownloadingRecyclerAdapter;
import com.zxmark.videodownloader.adapter.MainListRecyclerAdapter;
import com.zxmark.videodownloader.bean.VideoBean;
import com.zxmark.videodownloader.db.DBHelper;
import com.zxmark.videodownloader.db.DownloadContentItem;
import com.zxmark.videodownloader.db.DownloaderDBHelper;
import com.zxmark.videodownloader.downloader.DownloadingTaskList;
import com.zxmark.videodownloader.util.ADCache;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.FileComparator;
import com.zxmark.videodownloader.util.Globals;
import com.zxmark.videodownloader.util.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by fanlitao on 17/6/13.
 */

public class VideoHistoryFragment extends Fragment {


    private EditText mUrlEditText;
    private Button mDownloadBtn;
    private RecyclerView mListView;
    private LinearLayoutManager mLayoutManager;
    private List<DownloadContentItem> mDataList;
    private MainListRecyclerAdapter mAdapter;
    private NativeAd mNativeAd;
    private boolean mHaveDeletedUselessFiles = false;
    private DownloadContentItem mAdVideoBean;

    private Handler mMainLooperHandler = new Handler();

    public static VideoHistoryFragment newInstance() {
        VideoHistoryFragment fragment = new VideoHistoryFragment();
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.history_page, container, false);
        return view;
    }

    private View findViewById(int id) {
        return getView().findViewById(id);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView = (RecyclerView) findViewById(R.id.video_history_list);
        mListView.setHasFixedSize(true);
        mListView.setItemAnimator(new DefaultItemAnimator());
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,
                false);

        mListView.setLayoutManager(mLayoutManager);
        initData();
    }

    private void initData() {

        DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                mDataList = DownloaderDBHelper.SINGLETON.getDownloadedTask();

                mAdVideoBean = ADCache.getDefault().getFacebookNativeAd(ADCache.AD_KEY_HISTORY_VIDEO);
                if (mAdVideoBean != null) {
                    if (mDataList.size() > 2) {
                        mDataList.add(2, mAdVideoBean);
                    } else {
                        mDataList.add(mAdVideoBean);
                    }
                }

                if (isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter = new MainListRecyclerAdapter(mDataList, false);
                            mListView.setAdapter(mAdapter);
                            showNativeAd();
                        }
                    });
                }
            }
        });

    }

    public void onAddNewDownloadedFile(String pageURL) {
        LogUtil.v("main", "onAddNewDownloadFile:" + pageURL);
        if (mDataList != null) {
            DownloadContentItem videoBean = DownloaderDBHelper.SINGLETON.getDownloadItemByPageURL(pageURL);
            if (videoBean != null) {
                mDataList.add(0, videoBean);
                mAdapter.notifyItemInserted(0);
                mListView.smoothScrollToPosition(0);
            }
        }
    }

    private void showNativeAd() {
        if(!ADCache.SHOW_AD) {
            return;
        }
        if (mAdVideoBean == null) {
            if (isAdded()) {
                mNativeAd = new NativeAd(getActivity(), "2099565523604162_2099583463602368");
                mNativeAd.setAdListener(new AdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        LogUtil.v("facebook", "onError:" + adError);
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        onFacebookAdLoaded(ad);
                    }

                    @Override
                    public void onAdClicked(Ad ad) {
                        LogUtil.e("facebook", "onAdClicked");
                        mMainLooperHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mAdVideoBean != null) {
                                    ADCache.getDefault().removeClickedAd(mAdVideoBean);
                                    final int position = mDataList.indexOf(mAdVideoBean);
                                    LogUtil.e("facebook2","position:" + position);
                                    if (position >= 0) {
                                        mDataList.remove(position);
                                        mAdapter.notifyItemRemoved(position);
                                        mAdVideoBean = null;
                                    }
                                }
                            }
                        },1000);
                    }

                    @Override
                    public void onLoggingImpression(Ad ad) {

                    }
                });

                mNativeAd.loadAd();
            }
        }
    }

    // The next step is to extract the ad metadata and use its properties
// to build your customized native UI. Modify the onAdLoaded function
// above to retrieve the ad properties. For example:
    public void onFacebookAdLoaded(Ad ad) {
        if (getActivity() == null || isDetached()) {
            return;
        }
        if (ad != mNativeAd) {
            return;
        }

        if (mAdVideoBean == null) {
            mAdVideoBean = new DownloadContentItem();
            mAdVideoBean.itemType = DownloadContentItem.TYPE_FACEBOOK_AD;
            mAdVideoBean.facebookNativeAd = mNativeAd;
            mAdVideoBean.createdTime = System.currentTimeMillis();

            ADCache.getDefault().setFacebookNativeAd(ADCache.AD_KEY_HISTORY_VIDEO, mAdVideoBean);

            if (mDataList != null) {
                if (mDataList.size() == 0) {
                    mDataList.add(mAdVideoBean);
                    mAdapter.notifyItemInserted(0);
                } else {
                    int adPosition = mLayoutManager.findFirstVisibleItemPosition() + 1;
                    if (adPosition < mDataList.size()) {
                        mDataList.add(adPosition, mAdVideoBean);
                        mAdapter.notifyItemInserted(adPosition);
                    }
                }
            }
        }
    }
}
