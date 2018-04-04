package com.example.android.emojify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

/**
 * Created by figengungor on 4/2/2018.
 */

public class Emojifier {

    //https://developers.google.com/vision/android/detect-faces-tutorial

    private static final String TAG = Emojifier.class.getSimpleName();
    private static final float EMOJI_SCALE_FACTOR = 0.8f;

    public static Bitmap detectFacesAndOverlayEmoji(Context context, Bitmap bitmap) {

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

        if(faces.size()<0){
            Toast.makeText(context, "No face detected!", Toast.LENGTH_SHORT).show();
        }

        //Add proper emoji to detected faces in bitmap

        Bitmap resultBitmap = bitmap;

        for (int i = 0; i < faces.size(); i++) {
            int emojiDrawable = 0;
            Bitmap emojiBitmap = null;
            Face face = faces.valueAt(i);
            Emoji emoji = whichEmoji(face);
            switch (emoji) {
                case SMILING:
                    emojiDrawable = R.drawable.smile;
                    break;
                case FROWNING:
                    emojiDrawable = R.drawable.frown;
                    break;
                case LEFT_WINK:
                    emojiDrawable = R.drawable.leftwink;
                    break;
                case RIGHT_WINK:
                    emojiDrawable = R.drawable.rightwink;
                    break;
                case CLOSED_EYE_SMILING:
                    emojiDrawable = R.drawable.closed_smile;
                    break;
                case LEFT_WINK_FROWNING:
                    emojiDrawable = R.drawable.leftwinkfrown;
                    break;
                case RIGHT_WINK_FROWNING:
                    emojiDrawable = R.drawable.rightwinkfrown;
                    break;
                case CLOSED_EYE_FROWNING:
                    emojiDrawable = R.drawable.closed_frown;
                    break;
                default:
                    emojiDrawable = 0;
                    break;

            }
            if (emojiDrawable != 0) {
                emojiBitmap = BitmapFactory.decodeResource(context.getResources(), emojiDrawable);
                resultBitmap = addBitmapToFace(resultBitmap, emojiBitmap, face);
            }

        }

        detector.release();

        return resultBitmap;

    }

    private static Emoji whichEmoji(Face face) {

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

        return emoji;

    }

    /**
     * Combines the original picture with the emoji bitmaps
     *
     * @param backgroundBitmap The original picture
     * @param emojiBitmap      The chosen emoji
     * @param face             The detected face
     * @return The final bitmap, including the emojis over the faces
     */
    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face) {

        // Initialize the results bitmap to be a mutable copy of the original image
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(), backgroundBitmap.getConfig());

        // Scale the emoji so it looks better on the face
        float scaleFactor = EMOJI_SCALE_FACTOR;

        // Determine the size of the emoji to match the width of the face and preserve aspect ratio
        int newEmojiWidth = (int) (face.getWidth() * scaleFactor);
        int newEmojiHeight = emojiBitmap.getHeight() *
                newEmojiWidth / emojiBitmap.getWidth();


        // Scale the emoji
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false);

        // Determine the emoji position so it best lines up with the face
        float emojiPositionX =
                (face.getPosition().x + face.getWidth() / 2) - emojiBitmap.getWidth() / 2;
        float emojiPositionY =
                (face.getPosition().y + face.getHeight() / 2) - emojiBitmap.getHeight() / 3;

        // Create the canvas and draw the bitmaps to it
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);

        return resultBitmap;
    }
}
