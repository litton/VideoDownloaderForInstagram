package com.zxmark.videodownloader.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.imobapp.videodownloaderforinstagram.R;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.zxmark.videodownloader.service.DownloadService;
import com.zxmark.videodownloader.util.DownloadUtil;

/**
 * Created by fanlitao on 6/16/17.
 */

public class ItemHeaderHolder extends RecyclerView.ViewHolder {


    public Button showHowToBtn;
    public Button downloadBtn;
    public TextView homeTv;
    public MaterialEditText inputUrl;

    public ItemHeaderHolder(View itemView) {
        super(itemView);
        showHowToBtn = (Button) itemView.findViewById(R.id.btn_howto);
        downloadBtn = (Button) itemView.findViewById(R.id.btn_download);
        homeTv = (TextView) itemView.findViewById(R.id.home_directory);
        homeTv.setText(itemView.getResources().getString(R.string.download_home_lable, DownloadService.DIR));
        inputUrl = (MaterialEditText) itemView.findViewById(R.id.input_url);
    }
}
