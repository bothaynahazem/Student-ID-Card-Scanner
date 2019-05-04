package com.example.cardscanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImgProcPipeline {

    private static String TAG = ImgProcPipeline.class.getSimpleName();

    public static Bitmap currentBitmap;
    private static Bitmap croppedImgBitmap;
    private static Mat scannedCardMat;

    public static File loadClassifierData(Context context) {

        File faceClassifierFile = null;
        InputStream is;
        FileOutputStream os;


        try {
            //loading the XML file of the classifier (the trained data)
            is = context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            File cascadeDir = context.getDir("cascade", context.MODE_PRIVATE);
            faceClassifierFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");

            os = new FileOutputStream(faceClassifierFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            is.close();
            os.close();

        } catch (IOException e) {
            Log.i(TAG, "Face cascade not found");
        }

        return faceClassifierFile;
    }


    public static void detectFaceInCard(Mat src, Mat dst, Context currentContext) {

        //creates the face classifier xml file to be used by the face detection method
        File faceClassifierFile = loadClassifierData(currentContext);

        CascadeClassifier faceDetector = new CascadeClassifier(faceClassifierFile.getAbsolutePath());

        if (faceDetector.empty()) {

            Log.e(TAG, "****** Cascade Classifier Wasn't loaded ******");
        } else {
            Log.i(TAG, "Loaded cascade classifier from "
                    + faceClassifierFile.getAbsolutePath());
        }

        //gray img --> equalize histogram --> input to the classifier
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(src, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayFrame, grayFrame);

        MatOfRect facesDetected = new MatOfRect();
        faceDetector.detectMultiScale(grayFrame, facesDetected); //the detected faces are returned in facesDetected

        if (facesDetected.empty()) {
            Toast.makeText(currentContext, "No faces detected in this card", Toast.LENGTH_SHORT);
            Log.e(TAG, "Successfully scanned but no faces detected!");
            return;
        }


        Rect[] faces = facesDetected.toArray();
        Rect rect = faces[0].clone();

        // This loop is for when there're faces detected from the patterns in the card
        // same as when one sees a face in the clouds
        if (faces.length > 1) {
            for (Rect face : faces) {
                if (face.area() > rect.area())
                    rect = face.clone();
            }
        }

        int newX = rect.x - (int) (rect.width * 0.4);
        int newY = rect.y - (int) (rect.height * 0.4);
        int newWidth = (int) (rect.width * 1.8);
        int newHeight = (int) (rect.height * 1.8);

        if (newX + newWidth > src.width()) //the cropped part will surpass the edges of the card
        {
            newWidth = src.width() - newX;
        }

        if (newY + newHeight > src.height()) //not necessary for the faculty's cards but could be for other types of cards
        {
            newHeight = src.height() - newY;
        }


        Rect theCroppedFace = new Rect(newX, newY, newWidth, newHeight);

        Mat faceImgOnly = new Mat(src, theCroppedFace);

        faceImgOnly.copyTo(dst);

    }

}
