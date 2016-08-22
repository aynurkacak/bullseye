# Bullseye
Touch-to-record video library for Android like Instagram

![alt text][logo2]
![alt text][logo]

[logo]: https://github.com/aynurkacak/bullseye/blob/master/screenshots/camera.png "Video Screen"
[logo2]: https://github.com/aynurkacak/bullseye/blob/master/screenshots/photo.png "Photo Screen"

## Features

- Touch to record video
- Taking photos
- Select photo from Android gallery
- Back and front camera
- Flash mode

## Installation

Add this to app level `build.gradle`

```sh
dependencies {
  compile 'com.aynurkacak:bullseye:1.0.0'
}
```

## Usage

You have to add two activities in your `AndroidManifest.xml`

```xml
<activity android:name="com.aynurkacak.bullseye.CameraActivity"/>
<activity android:name="com.aynurkacak.bullseye.PreviewActivity"/>
```

Opening camera in java code:

```java
public static int REQUEST_CODE = 100;

new Bullseye(this)
        .maxVideoDuration(15)
        .themeColor(android.R.color.black)
        .selectColor(R.color.content_bar_color)
        .videoQuality(Bullseye.QUALITY_HIGH)
        .photoIcon(R.drawable.rec_icon)
        .videoIcon(R.drawable.video_rec_icon)
        .minVideoDuration(3)
        .previewPlayIcon(R.drawable.play_button)
        .previewTrashIcon(R.drawable.trash)
        .resultVideoDir(getExternalFilesDir(null).getAbsolutePath())
        .startCamera(REQUEST_CODE);
```

Receiving results in java code:

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE) {
        if (resultCode == RESULT_OK) {
            Log.d("photo_dir", data.getStringExtra("image"));
            Log.d("video_dir", data.getStringExtra("video"));
        }
    }
}
```
