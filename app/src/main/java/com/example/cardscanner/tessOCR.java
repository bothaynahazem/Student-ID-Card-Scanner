package com.example.cardscanner;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class tessOCR {

    public static final String lang = "eng";

    private static final String TAG = "TESSERACT";
    private AssetManager assetManager;

    private TessBaseAPI mTess;

    public tessOCR(AssetManager assetManager) {

        Log.i(TAG, MainActivity.DATA_PATH);

        this.assetManager = assetManager;

        String[] paths = new String[]{MainActivity.DATA_PATH, MainActivity.DATA_PATH + "/tessdata/"};

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }
        }

        if (!(new File(MainActivity.DATA_PATH + "/tessdata/" + lang + ".traineddata")).exists()) {
            try {
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                OutputStream out = new FileOutputStream(new File(MainActivity.DATA_PATH + "/tessdata/", lang + ".traineddata"));

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }

        mTess = new TessBaseAPI();
        mTess.setDebug(true);
        mTess.init(MainActivity.DATA_PATH, lang);

    }

}
