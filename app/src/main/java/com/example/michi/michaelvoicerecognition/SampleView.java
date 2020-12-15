package com.example.michi.michaelvoicerecognition;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by Mischael on 18.09.2017.
 */
public class SampleView extends View {
    private Bitmap mBitmap;
    private Bitmap mBitmap2;
    private Bitmap mBitmap3;
    private Bitmap mBitmap4;
    private Drawable mDrawable;

    private Movie mMovie;
    private long mMovieStart;

    //Set to false to use decodeByteArray
    private static final boolean DECODE_STREAM = false;

    private static byte[] streamToBytes(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = is.read(buffer)) >= 0) {
                os.write(buffer, 0, len);
            }
        } catch (java.io.IOException e) {
        }
        return os.toByteArray();
    }

    public SampleView(Context context) {
        super(context);


        InputStream is ;// = context.getResources().openRawResource(R.drawable.ic_launcher_background);

        if (DECODE_STREAM) {
            mMovie = Movie.decodeStream(is);
        } else {
            //byte[] array = streamToBytes(is);
            //mMovie = Movie.decodeByteArray(array, 0, array.length);
        }
    }


}