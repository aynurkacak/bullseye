package com.aynurkacak.bullseye;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.mp4parser.BasicContainer;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by aynurkacak on 12/08/16.
 */
public class CameraActivity extends FragmentActivity implements View.OnClickListener, View.OnTouchListener {

    CameraPreview mPreview;
    ImageView ivFlash, ivFrontCam;
    Button btLibrary, btPhoto, btVideo, btNext;
    TextView tvName;
    ProgressBar pbVideo;
    ImageView ivRec;
    FrameLayout previewLayout;
    RelativeLayout rlTopBar, rlRecBar;
    LinearLayout llContentBar;
    ProgressTimer progressTimer; // For video time
    File mFile; // photo file
    Camera mCamera;
    MediaRecorder mMediaRecorder;
    ProgressDialog pdMerge;

    public static int LIBRARY_REQUEST = 1;

    boolean isFrontCam = false;
    boolean isRecording = false;
    boolean isVideoRecorded = false;
    int camId = 0;
    int timerProgress = 150;

    enum FlashType {FLASH_OFF, FLASH_ON, FLASH_AUTO}

    enum ContentType {PHOTO, VIDEO}

    FlashType flashType;
    ContentType contentType = ContentType.PHOTO;

    int minDuration;
    int maxDuration;
    String videoDirectory;
    int videoQuality;
    int themeColor;
    int selectColor;
    int photoIcon;
    int videoIcon;
    int previewPlayIcon;
    int previewTrashIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getIntentExtras();
        init();
    }

    private void getIntentExtras() {
        videoDirectory = getIntent().getStringExtra(Utils.VIDEO_DIR);
        minDuration = getIntent().getIntExtra(Utils.VIDEO_MIN_DURATION, 0);
        maxDuration = getIntent().getIntExtra(Utils.VIDEO_MAX_DURATION, 0);
        videoQuality = getIntent().getIntExtra(Utils.VIDEO_QUALITY, 0);
        themeColor = getIntent().getIntExtra(Utils.THEME_COLOR, 0);
        selectColor = getIntent().getIntExtra(Utils.SELECT_COLOR, 0);
        photoIcon = getIntent().getIntExtra(Utils.PHOTO_ICON, 0);
        videoIcon = getIntent().getIntExtra(Utils.VIDEO_ICON, 0);
        previewPlayIcon = getIntent().getIntExtra(Utils.PREVIEW_PLAY_ICON, 0);
        previewTrashIcon = getIntent().getIntExtra(Utils.PREVIEW_TRASH_ICON, 0);
    }

    private void init() {
        ivRec = (ImageView) findViewById(R.id.iv_rec);
        previewLayout = (FrameLayout) findViewById(R.id.camera_preview);
        btLibrary = (Button) findViewById(R.id.bt_library);
        btNext = (Button) findViewById(R.id.bt_next);
        tvName = (TextView) findViewById(R.id.tv_post_type);
        btPhoto = (Button) findViewById(R.id.bt_photo);
        btVideo = (Button) findViewById(R.id.bt_video);
        pbVideo = (ProgressBar) findViewById(R.id.pb_video);
        ivFlash = (ImageView) findViewById(R.id.iv_flash);
        ivFrontCam = (ImageView) findViewById(R.id.iv_front);
        rlTopBar = (RelativeLayout) findViewById(R.id.rl_post_top_bar);
        rlRecBar = (RelativeLayout) findViewById(R.id.rl_rec_bar);
        llContentBar = (LinearLayout) findViewById(R.id.ll_content_select_bar);

        timerProgress = maxDuration * 10;
        pbVideo.setProgress(0);
        pbVideo.setMax(timerProgress);
        progressTimer = new ProgressTimer(timerProgress * 100, 100);

        ivRec.setOnClickListener(this);
        btPhoto.setOnClickListener(this);
        btVideo.setOnClickListener(this);
        btLibrary.setOnClickListener(this);
        btNext.setOnClickListener(this);
        ivFrontCam.setOnClickListener(this);
        ivFlash.setOnClickListener(this);

        ivRec.setOnTouchListener(this);

        pbVideo.setVisibility(View.GONE);
        mCamera = getCameraInstance(camId);
        mPreview = new CameraPreview(this, mCamera);
        previewLayout.addView(mPreview);

        rlTopBar.setBackgroundColor(ContextCompat.getColor(this, themeColor));
        rlRecBar.setBackgroundColor(ContextCompat.getColor(this, selectColor));
        llContentBar.setBackgroundColor(ContextCompat.getColor(this, themeColor));
        btPhoto.setBackgroundColor(ContextCompat.getColor(this, selectColor));
        flashType = FlashType.FLASH_OFF;
        ivRec.setImageResource(photoIcon);

        pdMerge = new ProgressDialog(this);
        pdMerge.setTitle("Please Wait");
        pdMerge.setMessage("Your video is preparing...");
        pdMerge.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdMerge.setCanceledOnTouchOutside(false);

        boolean hasFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (hasFlash) {
            ivFlash.setVisibility(View.VISIBLE);
        }
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            ivFrontCam.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.iv_rec) {
            if (contentType == ContentType.PHOTO) {
                mCamera.takePicture(null, null, mPicture);
            }
        }
        else if (i == R.id.bt_photo) {
            if (contentType == ContentType.VIDEO)
                photoClick();

        }
        else if (i == R.id.bt_video) {
            if (contentType == ContentType.PHOTO)
                videoClick();

        }
        else if (i == R.id.bt_next) {
            if (contentType == ContentType.VIDEO) {
                if (isVideoRecorded) {
                    if (timerProgress <= ((maxDuration * 10) - (minDuration * 10))) {
                        new PrepareVideos(this).execute();
                    }
                    else {
                        Utils.showToast(this, getString(R.string.min_video));
                    }
                }
                else {
                    Utils.showToast(this, getString(R.string.content_error_str));
                }
            }
            else {
                new PrepareVideos(this).execute();
            }

        }
        else if (i == R.id.bt_library) {
            Intent selectIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(selectIntent, LIBRARY_REQUEST);

        }
        else if (i == R.id.iv_front) {
            if (isFrontCam) {
                isFrontCam = false;
                releaseCamera();
                camId = 0;
                mCamera = getCameraInstance(camId);
                mPreview.refreshCamera(mCamera);
            }
            else {
                isFrontCam = true;
                releaseCamera();
                camId = findFrontFacingCamera();
                mCamera = getCameraInstance(camId);
                mPreview.refreshCamera(mCamera);
            }

        }
        else if (i == R.id.iv_flash) {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                return;
            }
            Camera.Parameters p = mCamera.getParameters();
            switch (flashType) {
                case FLASH_OFF:
                    ivFlash.setImageResource(R.drawable.flash_on);
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    flashType = FlashType.FLASH_AUTO;
                    break;
                case FLASH_ON:
                    ivFlash.setImageResource(R.drawable.flash_auto);
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    flashType = FlashType.FLASH_OFF;
                    break;
                case FLASH_AUTO:
                    ivFlash.setImageResource(R.drawable.flash_off);
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    flashType = FlashType.FLASH_ON;
                    break;
            }
            mCamera.setParameters(p);
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (contentType == ContentType.VIDEO) {
                    if (!isRecording) {
                        if (prepareVideoRecorder()) {
                            mMediaRecorder.start();
                            isRecording = true;
                            progressTimer = new ProgressTimer(timerProgress * 100, 100);
                            progressTimer.start();
                            isVideoRecorded = true;
                        }
                        else {
                            releaseMediaRecorder();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (contentType == ContentType.VIDEO) {
                    if (isRecording) {
                        mMediaRecorder.stop();  // stop the recording
                        releaseMediaRecorder(); // release the MediaRecorder object
                        mCamera.lock();         // take camera access back from MediaRecorder
                        isRecording = false;
                        progressTimer.cancel();
                    }
                }
                break;
        }
        return false;
    }

    //region PHOTO VIDEO
    private void photoClick() {
        if (getFragmentManager().getBackStackEntryCount() != 0) {
            getFragmentManager().popBackStack();
            boolean hasFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
            if (hasFlash) {
                ivFlash.setVisibility(View.VISIBLE);
            }
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
                ivFrontCam.setVisibility(View.VISIBLE);
            }
        }
        contentType = ContentType.PHOTO;
        btPhoto.setBackgroundColor(ContextCompat.getColor(this, selectColor));
        btVideo.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        tvName.setText(getResources().getString(R.string.photo_str));
        pbVideo.setVisibility(View.GONE);
        btNext.setVisibility(View.GONE);
        ivRec.setImageResource(photoIcon);
        btPhoto.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        btVideo.setTextColor(ContextCompat.getColor(this, R.color.deactive_text_color));
        isRecording = false;
    }

    private void videoClick() {
        if (getFragmentManager().getBackStackEntryCount() != 0) {
            getFragmentManager().popBackStack();
            boolean hasFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
            if (hasFlash) {
                ivFlash.setVisibility(View.VISIBLE);
            }
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
                ivFrontCam.setVisibility(View.VISIBLE);
            }
        }
        contentType = ContentType.VIDEO;
        btPhoto.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        btVideo.setBackgroundColor(ContextCompat.getColor(this, selectColor));
        tvName.setText(getResources().getString(R.string.video_str));
        pbVideo.setVisibility(View.VISIBLE);
        timerProgress = maxDuration * 10;
        pbVideo.setProgress(0);
        btNext.setVisibility(View.VISIBLE);
        deleteFilesDir(getExternalFilesDir(null).getAbsolutePath());
        ivRec.setImageResource(videoIcon);
        btPhoto.setTextColor(ContextCompat.getColor(this, R.color.deactive_text_color));
        btVideo.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        isRecording = false;
    }

    //endregion

    //region MEDIA RECORDER
    private boolean prepareVideoRecorder() {
        if (mCamera == null) {
            releaseCamera();
            mCamera = getCameraInstance(camId);
            Camera.Parameters params = mCamera.getParameters();
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes != null) {
                Log.i("video", Build.MODEL);
                if (((Build.MODEL.startsWith("GT-I950")) || (Build.MODEL.endsWith("SCH-I959"))
                        || (Build.MODEL.endsWith("MEIZU MX3"))) && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
                else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }
                else if ((Build.MODEL.startsWith("GT"))) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
                else
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
            }
            mCamera.setParameters(params);
            mCamera.setDisplayOrientation(90);
        }

        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        if (isFrontCam) {
            mMediaRecorder.setOrientationHint(270);
        }
        else
            mMediaRecorder.setOrientationHint(90);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(camId, videoQuality));
        mMediaRecorder.setOutputFile(getVideoFile(this).toString());
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private File getVideoFile(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(context.getExternalFilesDir(null) + File.separator + "VID_" + timeStamp + ".mp4");
        return mediaFile;
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    //endregion

    private int findFrontFacingCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.d("", "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    //region PICTURE
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = null;
            try {
                pictureFile = Utils.createImageFile(CameraActivity.this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mFile = pictureFile;
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("", "Error accessing file: " + e.getMessage());
            }

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bm = BitmapFactory.decodeFile(pictureFile.getPath(), bmOptions);
            Matrix matrix = new Matrix();
            if (isFrontCam) {
                float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
                Matrix matrixMirrorY = new Matrix();
                matrixMirrorY.setValues(mirrorY);
                matrix.postConcat(matrixMirrorY);
                matrix.preRotate(270);
            }
            else {
                matrix.setRotate(90);
            }
            try {
                Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                byte[] byteArray = stream.toByteArray();
                FileOutputStream fos = null;
                fos = new FileOutputStream(pictureFile);
                fos.write(byteArray);
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(CameraActivity.this, PreviewActivity.class);
            intent.putExtra("video_path", "");
            intent.putExtra("image_path", pictureFile.getPath());
            intent.putExtra(Utils.THEME_COLOR, themeColor);
            intent.putExtra(Utils.PREVIEW_PLAY_ICON, previewPlayIcon);
            intent.putExtra(Utils.PREVIEW_TRASH_ICON, previewTrashIcon);
            startActivityForResult(intent, Utils.CAMERA_ACTIVITY_REQUEST);
            mPreview.refreshCamera(mCamera);


        }
    };

    public static Camera getCameraInstance(int cameraId) {
        Camera c = null;
        try {
            c = Camera.open(cameraId);
        } catch (Exception e) {
            Log.e("camera instance", e.getMessage());
        }
        return c;
    }

    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LIBRARY_REQUEST) {
            if (resultCode == RESULT_OK) {
                //region GALERY
                Uri selectedImageUri = data.getData();
                try {
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
                    Bitmap scaled = Utils.scaleDown(yourSelectedImage, 480, true);
                    File imageFile = Utils.createImageFile(this);
                    if (imageFile == null) {
                        Log.d("", "Error creating media file, check storage permissions: ");
                        return;
                    }
                    try {
                        FileOutputStream fos = new FileOutputStream(imageFile);
                        scaled.compress(Bitmap.CompressFormat.JPEG, 70, fos);
                        fos.close();

                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        Bundle bundle = new Bundle();

                        bundle.putString("video_path", "");
                        bundle.putString("image_path", imageFile.getPath());
                        bundle.putInt(Utils.THEME_COLOR, themeColor);
                        bundle.putInt(Utils.PREVIEW_PLAY_ICON, previewPlayIcon);
                        bundle.putInt(Utils.PREVIEW_TRASH_ICON, previewTrashIcon);

                        PreviewFragment previewFragment = new PreviewFragment();
                        previewFragment.setArguments(bundle);
                        fragmentTransaction.add(R.id.fl_container, previewFragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();


                    } catch (FileNotFoundException e) {
                        Log.d("", "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d("", "Error accessing file: " + e.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //endregion
            }
        }
        else if (requestCode == Utils.CAMERA_ACTIVITY_REQUEST) {
            Intent resultData = new Intent();
            if (data != null) {
                if (data.getStringExtra("image") != null) {
                    resultData.putExtra("image", data.getStringExtra("image"));
                }
                if (data.getStringExtra("video") != null) {
                    resultData.putExtra("video", data.getStringExtra("video"));
                }
            }
            else {
                resultData.putExtra("image", "");
                resultData.putExtra("video", "");
            }
            setResult(RESULT_OK, resultData);
            this.finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCamera = getCameraInstance(camId);
            mPreview.refreshCamera(mCamera);
            timerProgress = 150;
            pbVideo.setProgress(0);
        }
    }

    private void deleteFilesDir(String path) {
        File dir = new File(path);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
    }

    public class ProgressTimer extends CountDownTimer {

        public ProgressTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            int progress = (int) (l / 100);
            pbVideo.setProgress(pbVideo.getMax() - progress);
            timerProgress = progress;
        }

        @Override
        public void onFinish() {
            pbVideo.setProgress(pbVideo.getMax());
            if (isRecording) {
                mMediaRecorder.stop();  // stop the recording
                releaseMediaRecorder(); // release the MediaRecorder object
                mCamera.lock();         // take camera access back from MediaRecorder
                isRecording = false;
            }
            new PrepareVideos(CameraActivity.this).execute();
        }
    }

    class PrepareVideos extends AsyncTask {
        Activity activity;

        public PrepareVideos(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdMerge.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            mergeVideos();
            pdMerge.dismiss();
            return null;
        }

        public void mergeVideos() {
            try {
                File parentDir = new File(getExternalFilesDir(null).getAbsolutePath());
                List<String> videosPathList = new ArrayList<>();
                File[] files = parentDir.listFiles();
                for (File file : files) {
                    videosPathList.add(file.getAbsolutePath());
                }

                List<Movie> inMovies = new ArrayList<>();
                for (int i = 0; i < videosPathList.size(); i++) {
                    String filePath = videosPathList.get(i);
                    try {
                        Movie movie = MovieCreator.build(filePath);
                        if (movie != null)
                            inMovies.add(movie);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                List<Track> videoTracks = new LinkedList<Track>();
                List<Track> audioTracks = new LinkedList<Track>();
                for (Movie m : inMovies) {
                    for (Track t : m.getTracks()) {
                        try {
                            if (t.getHandler().equals("soun")) {
                                audioTracks.add(t);
                            }
                            if (t.getHandler().equals("vide")) {
                                videoTracks.add(t);
                            }
                        } catch (Exception e) {

                        }
                    }
                }
                Movie result = new Movie();
                if (audioTracks.size() > 0) {
                    result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
                }
                if (videoTracks.size() > 0) {
                    result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
                }
                BasicContainer out = (BasicContainer) new DefaultMp4Builder().build(result);
                File f = null;
                String finalVideoPath;
                try {
                    f = setUpVideoFile(videoDirectory);
                    finalVideoPath = f.getAbsolutePath();

                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    finalVideoPath = null;
                }
                WritableByteChannel fc = new RandomAccessFile(finalVideoPath, "rw").getChannel();
                out.writeContainer(fc);
                fc.close();
                deleteFilesDir(getExternalFilesDir(null).getAbsolutePath());

                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(finalVideoPath, MediaStore.Images.Thumbnails.MINI_KIND);
                String filename = Utils.createImageFile(CameraActivity.this).getAbsolutePath();
                FileOutputStream bitmapOut = null;
                try {
                    bitmapOut = new FileOutputStream(filename);
                    thumb.compress(Bitmap.CompressFormat.PNG, 100, bitmapOut);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (bitmapOut != null) {
                            bitmapOut.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Intent intent = new Intent(activity, PreviewActivity.class);
                intent.putExtra("video_path", finalVideoPath);
                intent.putExtra("image_path", filename);
                intent.putExtra(Utils.THEME_COLOR, themeColor);
                intent.putExtra(Utils.PREVIEW_PLAY_ICON, previewPlayIcon);
                intent.putExtra(Utils.PREVIEW_TRASH_ICON, previewTrashIcon);
                startActivityForResult(intent, Utils.CAMERA_ACTIVITY_REQUEST);

            } catch (Exception e) {
                e.printStackTrace();

                finish();
            }
        }

        File setUpVideoFile(String directory) throws IOException {
            File videoFile = null;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                File storageDir = new File(directory);
                if (storageDir != null) {
                    if (!storageDir.mkdirs()) {
                        if (!storageDir.exists()) {
                            Log.d("CameraSample", "failed to create directory");
                            return null;
                        }
                    }
                }
                videoFile = File.createTempFile("video_" + System.currentTimeMillis() + "_", ".mp4", storageDir);
            }
            return videoFile;
        }

        private void deleteFilesDir(String path) {
            File dir = new File(path);
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(dir, children[i]).delete();
                }
            }
        }
    }

}
