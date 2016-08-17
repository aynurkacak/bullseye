package com.aynurkacak.bullseye;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by aynurkacak on 12/08/16.
 */
public class Utils {
    public static String VIDEO_DIR = "video_dir";
    public static String VIDEO_MIN_DURATION = "video_min_duration";
    public static String VIDEO_MAX_DURATION = "video_max_duration";
    public static String VIDEO_QUALITY = "video_quality";
    public static String THEME_COLOR = "theme_color";
    public static String SELECT_COLOR = "select_color";
    public static String PHOTO_ICON = "photo_icon";
    public static String VIDEO_ICON = "video_icon";
    public static String PREVIEW_PLAY_ICON = "preview_play_icon";
    public static String PREVIEW_TRASH_ICON = "preview_trash_icon";

    public static int CAMERA_ACTIVITY_REQUEST = 2;

    public static void showToast(Activity activity, String message) {
        try {
            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast_layout, (ViewGroup) activity.findViewById(R.id.toast_layout_root));
            TextView text = (TextView) layout.findViewById(R.id.tv_error_text);
            text.setText(message);
            Toast toast = new Toast(activity);
            toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();
        } catch (Exception e) {
        }
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min((float) maxImageSize / realImage.getWidth(), (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());
        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width, height, filter);
        return newBitmap;
    }

    public static File createImageFile(Activity activity) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(activity.getExternalFilesDir(null) + File.separator + "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }

}
