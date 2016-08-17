package com.aynurkacak.bullseye;

import android.app.Activity;
import android.content.Intent;
import android.media.CamcorderProfile;
import android.os.Environment;

/**
 * Created by aynurkacak on 12/08/16.
 */
public class Bullseye {

    private Activity activity;
    private String mResultVideoDir = Environment.getExternalStorageDirectory() + "/Bullseye/videos/";
    private int mMinVideoDuration = 3;
    private int mMaxVideoDuration = 15;
    private int mVideoQuality = CamcorderProfile.QUALITY_HIGH;
    private int mThemeColor = android.R.color.black;
    private int mSelectedColor = R.color.content_bar_color;
    private int mPhotoIcon = R.drawable.rec_icon;
    private int mVideoIcon = R.drawable.video_rec_icon;
    private int mPreviewPlayIcon = R.drawable.play_button;
    private int mPreviewTrashIcon = R.drawable.trash;

    public static final int QUALITY_HIGH = CamcorderProfile.QUALITY_HIGH;
    public static final int QUALITY_LOW = CamcorderProfile.QUALITY_LOW;
    public static final int QUALITY_480P = CamcorderProfile.QUALITY_480P;
    public static final int QUALITY_720P = CamcorderProfile.QUALITY_720P;
    public static final int QUALITY_1080P = CamcorderProfile.QUALITY_1080P;

    public Bullseye(Activity act) {
        this.activity = act;
    }

    public Bullseye resultVideoDir(String videoDir) {
        mResultVideoDir = videoDir;
        return this;
    }

    public Bullseye minVideoDuration(int minDuration) {
        mMinVideoDuration = minDuration;
        return this;
    }

    public Bullseye maxVideoDuration(int maxDuration) {
        mMaxVideoDuration = maxDuration;
        return this;
    }

    public Bullseye videoQuality(int quality) {
        mVideoQuality = quality;
        return this;
    }

    public Bullseye themeColor(int color) {
        mThemeColor = color;
        return this;
    }

    public Bullseye selectColor(int color) {
        mSelectedColor = color;
        return this;
    }

    public Bullseye photoIcon(int icon) {
        mPhotoIcon = icon;
        return this;
    }

    public Bullseye videoIcon(int icon) {
        mVideoIcon = icon;
        return this;
    }

    public Bullseye previewPlayIcon(int icon) {
        mPreviewPlayIcon = icon;
        return this;
    }

    public Bullseye previewTrashIcon(int icon) {
        mPreviewTrashIcon = icon;
        return this;
    }

    public void startCamera(int requestCode) {
        Intent intent = new Intent(activity, CameraActivity.class);
        intent.putExtra(Utils.VIDEO_DIR, mResultVideoDir);
        intent.putExtra(Utils.VIDEO_MIN_DURATION, mMinVideoDuration);
        intent.putExtra(Utils.VIDEO_MAX_DURATION, mMaxVideoDuration);
        intent.putExtra(Utils.VIDEO_QUALITY, mVideoQuality);
        intent.putExtra(Utils.THEME_COLOR, mThemeColor);
        intent.putExtra(Utils.SELECT_COLOR, mSelectedColor);
        intent.putExtra(Utils.PHOTO_ICON, mPhotoIcon);
        intent.putExtra(Utils.VIDEO_ICON, mVideoIcon);
        intent.putExtra(Utils.PREVIEW_PLAY_ICON, mPreviewPlayIcon);
        intent.putExtra(Utils.PREVIEW_TRASH_ICON, mPreviewTrashIcon);
        activity.startActivityForResult(intent, requestCode);
    }
}
