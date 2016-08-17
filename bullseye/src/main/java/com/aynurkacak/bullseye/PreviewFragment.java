package com.aynurkacak.bullseye;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import java.io.File;

/**
 * Created by aynurkacak on 17/08/16.
 */
public class PreviewFragment extends Fragment implements View.OnClickListener{

    View view;
    String videoPath, imagePath;
    ImageView ivPlay, ivPhoto, ivDelete;
    VideoView vvContent;
    Button btNext;
    RelativeLayout rlBottom;

    int previewPlayIcon;
    int previewTrashIcon;
    int themeColor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_preview, container, false);
        videoPath = getArguments().getString("video_path");
        imagePath = getArguments().getString("image_path");
        previewPlayIcon = getArguments().getInt(Utils.PREVIEW_PLAY_ICON);
        previewTrashIcon = getArguments().getInt(Utils.PREVIEW_TRASH_ICON);
        themeColor = getArguments().getInt(Utils.THEME_COLOR);
        init();
        return view;
    }

    private void init() {
        ivPlay = (ImageView) view.findViewById(R.id.previre_play);
        ivDelete = (ImageView) view.findViewById(R.id.bt_cancel);
        ivPhoto = (ImageView) view.findViewById(R.id.iv_photo);
        vvContent = (VideoView) view.findViewById(R.id.vv_content);
        btNext = (Button) view.findViewById(R.id.bt_ok);
        rlBottom = (RelativeLayout) view.findViewById(R.id.rl_bottom);

        ivPlay.setOnClickListener(this);
        ivDelete.setOnClickListener(this);
        btNext.setOnClickListener(this);

        ivPlay.setImageResource(previewPlayIcon);
        ivDelete.setImageResource(previewTrashIcon);
        rlBottom.setBackgroundColor(ContextCompat.getColor(getActivity(),themeColor));

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
            getFragmentManager().popBackStackImmediate();

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
            getActivity().setResult(getActivity().RESULT_OK, resultData);
            getActivity().finish();
        }
    }
}
