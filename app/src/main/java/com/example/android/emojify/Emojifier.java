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

        //Log the number of detected faces
        Log.i(TAG, "Number of detected faces: " + faces.size());

        //Log the rightEyeOpen, leftEyeOpen, smiling probability of faces
        for (int i = 0; i < faces.size(); i++) {
            whichEmoji(faces.valueAt(i));
        }

        detector.release();

    }

    private static void whichEmoji(Face face) {

        float leftEyeProbability = face.getIsLeftEyeOpenProbability();
        float rightEyeProbability = face.getIsRightEyeOpenProbability();
        float smilingProbability = face.getIsSmilingProbability();

        Log.i(TAG, "getProbabilities for face: "
                + "\nLeft eye probability: " + leftEyeProbability
                + "\nRight eye probability: " + rightEyeProbability
                + "\nSmiling probability: " + smilingProbability);

        float EYE_BEING_OPEN_THRESHOLD = 0.5f;
        float SMILE_THRESHOLD = 0.2f;

        boolean smiling, leftEyeClosed, rightEyeClosed;
        Emoji emoji;

        smiling = smilingProbability > SMILE_THRESHOLD;
        leftEyeClosed = leftEyeProbability < EYE_BEING_OPEN_THRESHOLD;
        rightEyeClosed = rightEyeProbability < EYE_BEING_OPEN_THRESHOLD;

        Log.i(TAG, "Status: "
                + "\nLeft eye closed: " + leftEyeClosed
                + "\nRight eye closed: " + rightEyeClosed
                + "\nSmiling: " + smiling);

        if (smiling) {
            if (leftEyeClosed && rightEyeClosed) {
                emoji = Emoji.CLOSED_EYE_SMILING;
            } else if (leftEyeClosed) {
                emoji = Emoji.LEFT_WINK;
            } else if (rightEyeClosed) {
                emoji = Emoji.RIGHT_WINK;
            } else {
                emoji = Emoji.SMILING;
            }

        } else {
            if (leftEyeClosed && rightEyeClosed) {
                emoji = Emoji.CLOSED_EYE_FROWNING;
            } else if (leftEyeClosed) {
                emoji = Emoji.LEFT_WINK_FROWNING;
            } else if (rightEyeClosed) {
                emoji = Emoji.RIGHT_WINK_FROWNING;
            } else {
                emoji = Emoji.FROWNING;
            }
        }
        Log.i(TAG, "whichEmoji: " + emoji);

    }
}
