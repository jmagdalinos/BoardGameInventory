package com.example.android.boardgameinventory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Helper class that down-scales an image to reduce memory usage
 *
 */

public class DownScaledImage {

    /**
     * Make constructor private so that no one can initiate it by mistake
     */
    private DownScaledImage() {
    }

    /**
     * Uses the image Uri to get the image, decode it and down-scale it to reduce memory usage
     *
     * @param context   the context of the activity, used to get resources and the content resolver
     * @param imageUri  the image uri of the original image
     * @param reqHeight the height of the target ImageView
     * @param reqWidth  the width of the target ImageView
     * @return reduced-size bitmap
     */
    public static Bitmap prepareBitmap(Context context, Uri imageUri, int reqHeight, int reqWidth) {
        // If the imageUri is null, return early
        if (imageUri == null) {
            return null;
        }

        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = null;
        Bitmap finalBitmap = null;

        try {
            // Get input stream
            imageStream = context.getContentResolver().openInputStream(imageUri);
            // Get original bitmap
            BitmapFactory.decodeStream(imageStream, null, options);

            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            // Check if the image is larger than the required height & width
            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) >= reqHeight &&
                        (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }
            // Decode bitmap with inSampleSize set
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;

            // Get new input stream and use inSampleSize to down-scale
            imageStream = context.getContentResolver().openInputStream(imageUri);
            // Get final bitmap
            finalBitmap = BitmapFactory.decodeStream(imageStream, null, options);
            return finalBitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return finalBitmap;
        }
    }
}