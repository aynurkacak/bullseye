package com.aynurkacak.bullseye;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import java.io.File;

/**
 * Created by aynurkacak on 15/08/16.
 */
public class PreviewActivity extends Activity implements View.OnClickListener {
    String videoPath, imagePath;
    ImageView ivPlay, ivPhoto, ivDelete;
    VideoView vvContent;
    Button btNext;
    RelativeLayout rlBottom;

    int previewPlayIcon;
    int previewTrashIcon;
    int themeColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        videoPath = getIntent().getStringExtra("video_path");
        imagePath = getIntent().getStringExtra("image_path");
        previewPlayIcon = getIntent().getIntExtra(Utils.PREVIEW_PLAY_ICON, 0);
        previewTrashIcon = getIntent().getIntExtra(Utils.PREVIEW_TRASH_ICON, 0);
        themeColor = getIntent().getIntExtra(Utils.THEME_COLOR, 0);
        init();
    }

    private void init() {
        ivPlay = (ImageView) findViewById(R.id.previre_play);
        ivDelete = (ImageView) findViewById(R.id.bt_cancel);
        ivPhoto = (ImageView) findViewById(R.id.iv_photo);
        vvContent = (VideoView) findViewById(R.id.vv_content);
        btNext = (Button) findViewById(R.id.bt_ok);
        rlBottom = (RelativeLayout) findViewById(R.id.rl_bottom);

        ivPlay.setOnClickListener(this);
        ivDelete.setOnClickListener(this);
        btNext.setOnClickListener(this);

        ivPlay.setImageResource(previewPlayIcon);
        ivDelete.setImageResource(previewTrashIcon);
        rlBottom.setBackgroundColor(ContextCompat.getColor(this,themeColor));

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        ivPhoto.setImageBitmap(bitmap);

        if (videoPath.equals("")) {
            ivPlay.setVisibility(View.GONE);
            vvContent.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.bt_cancel) {
            if (vvContent.isPlaying()) {
                vvContent.stopPlayback();
            }
            File file = new File(videoPath);
            file.delete();
            file = new File(imagePath);
            file.delete();
            this.finish();

        }
        else if (i == R.id.previre_play) {
            ivPlay.setVisibility(View.GONE);
            ivPhoto.setVisibility(View.GONE);
            vvContent.setVideoPath(videoPath);
            vvContent.start();

        }
        else if (i == R.id.bt_ok) {
            Intent resultData = new Intent();
            resultData.putExtra("image", imagePath);
            resultData.putExtra("video", videoPath);
            setResult(RESULT_OK, resultData);
            this.finish();
        }
    }
}