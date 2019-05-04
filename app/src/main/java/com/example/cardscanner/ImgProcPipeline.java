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
    
        private static void resizeHeight(Mat src, Mat dst, double height) {
        int w = src.width();
        int h = src.height();
        double r = height / (float) (h);
        Size dim = new Size((int) (w * r), height);
        Imgproc.resize(src, dst, dim, Imgproc.INTER_AREA);
    }


    private static Pair<Point, Integer> getMinX(ArrayList<Point> Points) {
        Point tempX = Points.get(0).clone();
        int index = 0;
        for (int i = 0; i < Points.toArray().length; i++) {
            if (Points.get(i).x < tempX.x) {
                tempX.x = Points.get(i).x;
                tempX.y = Points.get(i).y;
                index = i;
            }
        }
        return new Pair<>(tempX, index);
    }

    private static double getMaxPerimeter(List<MatOfPoint> contours) {
        double maxPerimeter = Imgproc.arcLength(new MatOfPoint2f(contours.get(0).toArray()), true);
        if (contours.toArray().length > 1) {
            for (MatOfPoint c : contours) {
                double perimeter = Imgproc.arcLength(new MatOfPoint2f(c.toArray()), true);
                if (perimeter > maxPerimeter) {
                    maxPerimeter = perimeter;
                }
            }
        }
        return maxPerimeter;
    }


    private static MatOfPoint2f getFourBoundaryPts(Mat input_img, double NEW_HEIGHT) {


        //Resize the img to be able to find the contours
        double ratio = input_img.height() / NEW_HEIGHT;
        resizeHeight(input_img, input_img, NEW_HEIGHT);

        Mat gray_img = new Mat();
        Mat blurred_img = new Mat();
        Mat edges_img = new Mat();

        /*  EDGES IMAGE (White pixels --> card edges, Black pixels --> Otherwise) */
        Imgproc.cvtColor(input_img, gray_img, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray_img, blurred_img, new Size(5, 5), 0);
        Imgproc.Canny(blurred_img, edges_img, 70, 200);


        /*  DEBUGGING to view the edges img bitmap  */
        Bitmap edges = null;
        edges = Bitmap.createBitmap(edges_img.width(), edges_img.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(edges_img, edges);
        /*  DEBUGGING  */


        /*   FINDING CONTOURS OF CARD   */
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges_img, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


        MatOfPoint2f cardContour = new MatOfPoint2f();


        boolean cardContourFound = false; //flag
        double maxPerimeter = getMaxPerimeter(contours);
        int counter = 3; //try for 3 times only in order not to fall into an infinite loop
        while (counter >= 0) {
            for (MatOfPoint c : contours) {
                double perimeter = Imgproc.arcLength(new MatOfPoint2f(c.toArray()), true);
                MatOfPoint2f approximatePolygon = new MatOfPoint2f();
                Imgproc.approxPolyDP(new MatOfPoint2f(c.toArray()), approximatePolygon, 0.02 * perimeter, true);

                if (approximatePolygon.toArray().length == 4) {

                    if (contours.toArray().length == 1) {

                        cardContour = approximatePolygon;
                        cardContourFound = true;
                        counter = -1;
                        break;

                    } else {
                        if (perimeter == maxPerimeter && maxPerimeter > 600) {
                            cardContour = approximatePolygon;
                            cardContourFound = true;
                            counter = -1;
                            break;
                        }
                    }
                }
            }

            if (!cardContourFound) {
                //fill the card's segments
                Mat filledEdges = new Mat();
                for (int i = 0; i < contours.toArray().length; i++) {
                    Imgproc.drawContours(edges_img, contours, i, new Scalar(255, 255, 255), Imgproc.FILLED);
                }

                //apply morphological close operation to let it have one contour only
                Mat Kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3));
                Imgproc.morphologyEx(edges_img, filledEdges, Imgproc.MORPH_CLOSE, Kernel, new Point(-1, -1), 10);


                //find contours again after the close operation
                contours = new ArrayList<>();
                hierarchy = new Mat();
                Imgproc.findContours(filledEdges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

                maxPerimeter = getMaxPerimeter(contours);
                counter--;
            }

        }


        if (cardContour.empty()) {
            return null;
        }

        ArrayList<Point> resizedPoints = new ArrayList<>();
        for (Point P : cardContour.toArray()) {
            Point temp = new Point();
            temp.x = P.x * ratio;
            temp.y = P.y * ratio;

            resizedPoints.add(temp);
        }

        /*  REARRANGING THE CORNER POINTS FOUND --> to be suitable for the next method  */
        Point tempMinX1 = new Point();
        int idx1 = 0;
        Pair<Point, Integer> minX1;
        minX1 = getMinX(resizedPoints);

        tempMinX1 = minX1.first.clone();
        idx1 = minX1.second;
        resizedPoints.remove(idx1);


        Point tempMinX2 = new Point();
        int idx2 = 0;
        Pair<Point, Integer> minX2;
        minX2 = getMinX(resizedPoints);

        tempMinX2 = minX2.first.clone();
        idx2 = minX2.second;
        resizedPoints.remove(idx2);


        Point tl, bl, tr, br;
        if (tempMinX1.y > tempMinX2.y) {
            bl = tempMinX1;
            tl = tempMinX2;
        } else {
            tl = tempMinX1;
            bl = tempMinX2;
        }

        if (resizedPoints.get(0).y > resizedPoints.get(1).y) {
            br = resizedPoints.get(0);
            tr = resizedPoints.get(1);
        } else {
            tr = resizedPoints.get(0);
            br = resizedPoints.get(1);
        }

        /*  The order accepted by the transformation fn is as follows: { tl, bl, br, tr }  */
        ArrayList<Point> resizedPointsNew = new ArrayList<>();
        resizedPointsNew.add(tl);
        resizedPointsNew.add(bl);
        resizedPointsNew.add(br);
        resizedPointsNew.add(tr);


        MatOfPoint2f result = new MatOfPoint2f();
        result.fromList(resizedPointsNew);

        if (result.size() == new Size(0, 0)) {
            Log.e(TAG, "The MatOfPoint2f doesn't contain the pts.");
        }

        return result;
    }
    private static void transformCardToRect(Mat input_img, Mat output_img, MatOfPoint2f src, int FINAL_WIDTH, int FINAL_HEIGHT) {

        // src(cardFourPtsContour is a matrix containing the corner points of the image to transform
        //dst is a matrix containing the points that the corners in src are going to be mapped into

        if (src.width() == 0 && src.height() == 0) //no pts were retrieved
        {
            Log.e(TAG, "No points were received from the \"getFourBoundaryPts\" method!");
        }

        int w = input_img.width();
        int h = input_img.height();

        MatOfPoint2f dst = new MatOfPoint2f(
                new Point(0, 0), // tl
                new Point(0, FINAL_HEIGHT), // bl
                new Point(FINAL_WIDTH, FINAL_HEIGHT), // br
                new Point(FINAL_WIDTH, 0) // tr

        );

        Mat P = new Mat();  //P is a temp variable
        P = Imgproc.getPerspectiveTransform(src, dst);

        //src –img1- input image.
        //dst –output- output image that has the size dsize and the same type as src .
        //M –P- 3\times 3 transformation matrix.
        //size – size of the output image -  can be greater than the output image and corners around image will be black.
        Imgproc.warpPerspective(input_img, output_img, P, new Size(FINAL_WIDTH, FINAL_HEIGHT));
    }


    private static void rotateImg(Mat src, Mat dst) {

        if (src.cols() <= src.rows()) //already rotated
        {
            src.copyTo(dst); //hence don't rotate it
        }

        int angleNeg90 = 270; //when the img is taken in a "portrait mode", it's rotated by 270
        double scale = 1.0;

        Point center = new Point(src.cols() / 2.0, src.rows() / 2.0); //centre to be rotated about

        Mat M = Imgproc.getRotationMatrix2D(center, angleNeg90, scale);

        Size newSize = new Size(src.cols(), src.rows());
        Imgproc.warpAffine(src, dst, M, newSize);

    }

    
    
}
