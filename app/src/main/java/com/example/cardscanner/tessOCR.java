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

    public String getResults(Bitmap croppedImgBitmap) {
        mTess.setImage(croppedImgBitmap);
        String result = "";
        String fullText = mTess.getUTF8Text();


        String[] lines = fullText.split("\\n");
        String line2;
        if (lines.length > 1)
            line2 = lines[1];

        else if (lines.length == 1)
            line2 = lines[0];

        else
            return null;

        line2 = line2.replaceAll("\\s", "");
        Pattern pattern = Pattern.compile("(1[0-9]{6})");
        Matcher matcher = pattern.matcher(line2);

        if (matcher.find()) {
            result = matcher.group(1);
        }

        return result;
    }
    
}
