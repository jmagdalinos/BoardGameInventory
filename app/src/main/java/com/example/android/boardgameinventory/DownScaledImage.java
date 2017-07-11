package com.example.android.boardgameinventory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Helper class that down-scales an image to reduce memory usage
 *
 */

public class DownScaledImage extends AsyncTask<Void, Void, Bitmap>{

    private Uri mImageUri;
    private Context mContext;
    private int mReqHeight;
    private int mReqWidth;

    public DownScaledImage (Context context, Uri imageUri, int reqHeight, int reqWidth) {
        mContext = context;
        mImageUri = imageUri;
        mReqHeight = reqHeight;
        mReqWidth = reqWidth;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        /**
         * Uses the image Uri to get the image, decode it and down-scale it to reduce memory usage
         */
            // If the imageUri is null, return early
            if (mImageUri == null) {
                return null;
            }

            // First decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream imageStream = null;
            Bitmap finalBitmap = null;

            try {
                // Get input stream
                imageStream = mContext.getContentResolver().openInputStream(mImageUri);
                // Get original bitmap
                BitmapFactory.decodeStream(imageStream, null, options);

                // Raw height and width of image
                final int height = options.outHeight;
                final int width = options.outWidth;
                int inSampleSize = 1;

                // Check if the image is larger than the required height & width
                if (height > mReqHeight || width > mReqWidth) {
                    final int halfHeight = height / 2;
                    final int halfWidth = width / 2;

                    // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                    // height and width larger than the requested height and width.
                    while ((halfHeight / inSampleSize) >= mReqHeight &&
                            (halfWidth / inSampleSize) >= mReqWidth) {
                        inSampleSize *= 2;
                    }
                }
                // Decode bitmap with inSampleSize set
                options.inSampleSize = inSampleSize;
                options.inJustDecodeBounds = false;

                // Get new input stream and use inSampleSize to down-scale
                imageStream = mContext.getContentResolver().openInputStream(mImageUri);
                // Get final bitmap
                finalBitmap = BitmapFactory.decodeStream(imageStream, null, options);
                return finalBitmap;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return finalBitmap;
        }
    }
}