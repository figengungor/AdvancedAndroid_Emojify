package com.example.android.emojify;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

/**
 * Created by figengungor on 4/2/2018.
 */

public class Emojifier {

    //https://developers.google.com/vision/android/detect-faces-tutorial

    private static final String TAG = Emojifier.class.getSimpleName();

    public static void detectFaces(Context context, Bitmap bitmap) {

       /*FaceDetector.Builder setTrackingEnabled()
        Enables or disables face tracking, which will maintain a consistent ID for each face when
        processing consecutive frames.

        Improve performance by disabling tracking (which maintains an ID between consecutive
        frames if the same face exists in both of them)

        setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
        Turn on classifications
        */

        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        //Given a bitmap, we can create Frame instance from the bitmap to supply to the detector
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();

        //The detector can be called synchronously with a frame to detect faces:
        SparseArray<Face> faces = detector.detect(frame);

        Log.d(TAG, "Number of detected faces: " + faces.size());

        detector.release();

    }
}
