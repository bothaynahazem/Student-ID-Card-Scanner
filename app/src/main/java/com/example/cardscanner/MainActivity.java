package com.example.cardscanner;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Context currentContext;

    /* Tesseract Library variables */
    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString();
    public static AssetManager assetManager;
    public static tessOCR tessOCR;


    /* Intents permission requests codes */
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int GALLERY_REQUEST_CODE = 5;

    /* Files paths */
    public static String currentPhotoPath;
    public static String currentScannedCardPhotoPath;
    public static String currentFacePhotoPath;

    public static Bitmap capturedImg;
    public static Bitmap scannedCard;
    public static Bitmap detectedFace;

    public static File scannedCardImgFile;
    public static File detectedFaceImgFile;

    /*  Tesseract OCR result*/
    public static String recognizedStudentCode;

    /*  Image Processing Pipeline related variables  */
    public static Mat inputImgMat;
    public static Mat scannedCardMat;
    public static Mat detectedFaceMat;
    public static double NEW_HEIGHT = 500.0;
    public static int FINAL_WIDTH = 1000;
    public static int FINAL_HEIGHT = 600;

    /* For executing the img processing method on another thread as it takes time */
    private MyAsyncTask myAsyncTask;


    //Loading the OpenCV library onto the Android app
    static {
        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "Loaded OpenCV successfully.");
        } else {
            Log.i(TAG, "OpenCV wasn't loaded successfully.");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentContext = getApplicationContext();
        assetManager = getAssets();
        tessOCR = new tessOCR(assetManager);

        Button galleryBtn = findViewById(R.id.btn_choose_from_gallery);
        Button cameraBtn = findViewById(R.id.btn_take_photo);
        final Button viewScannedPhotoBtn = findViewById(R.id.btn_review_scanned);

        /* Choose photo from gallery */
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flushPaths(); //flush the imgs paths -if exist- for a better interacting app cycle
                pickImgFromGallery();
            }
        });

        /* Capture the photo using the built-in camera */
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flushPaths(); //flush the imgs paths -if exist- for a better interacting app cycle
                dispatchTakePictureIntent();
            }

        });


        /* Send the photo path of the scanned card img to be parsed in the ReviewScannedCard activity */
        viewScannedPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (capturedImg == null) //no photo was selected or taken
                {
                    Toast T = Toast.makeText(currentContext,
                            R.string.no_photo_chosen,
                            Toast.LENGTH_SHORT);
                    T.show();
                    return;
                } else {
                    if (currentScannedCardPhotoPath == null) {

                        myAsyncTask = new MyAsyncTask(getApplicationContext());
                        myAsyncTask.execute();
                    }


                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        /*  CAMERA INTENT  */
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            try {

                capturedImg = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(currentPhotoPath));
                Toast T = Toast.makeText(currentContext,
                        R.string.photo_added_success,
                        Toast.LENGTH_SHORT);
                T.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        /*  PICK FROM GALLERY INTENT  */
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                Uri selectedImageURI = data.getData();

                currentPhotoPath = selectedImageURI.getPath();
                capturedImg = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageURI);

                Toast T = Toast.makeText(currentContext,
                        R.string.photo_added_success,
                        Toast.LENGTH_SHORT);
                T.show();

            } catch (IOException e) {
                Log.e(TAG, "Couldn't parse the image from gallery");
            }
        }
    }


    //for a better app cycle experience
    private void flushPaths() {
        currentPhotoPath = null;
        currentFacePhotoPath = null;
        currentScannedCardPhotoPath = null;
    }

    //choose img from gallery
    private void pickImgFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png", "image/jpg"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }


    /*  Creates the img file and starts the phone's camera,
        hence used only once when it's prompted */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null; // the img file
            try {
                photoFile = createImageFile();

            } catch (IOException ex) {
                Log.e("CREATE FILE", "Error occurred while creating the image file");
            }

            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }


        }

    }

    /* Create a unique img file for the captured/chosen img */
    public File createImageFile() throws IOException {

        //Img file name must be unique
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPG_" + timeStamp + "_";

        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

        currentPhotoPath = "file:" + imageFile.getAbsolutePath();

        return imageFile;
    }

    /* Create a unique img file for the scanned card img */
    public static File createImageFileCardScanned(Context context) throws IOException {
        //Img file name must be unique
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPG_" + timeStamp + "SCANNED_CARD";

        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

        currentScannedCardPhotoPath = "file:" + imageFile.getAbsolutePath();

        return imageFile;
    }

    /* Create a unique img file for the detected face img */
    public static File createImageFileFaceDetected(Context context) throws IOException {
        //Img file name must be unique
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPG_" + timeStamp + "FACE";

        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

        currentFacePhotoPath = "file:" + imageFile.getAbsolutePath();

        return imageFile;
    }


    /*  a method that writes a bitmap to a certain file, used for storing the scanned imgs */
    public static File writeBitmapToFile(Bitmap bm, File theFile) throws IOException {

        //Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        byte[] bitMapData = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = new FileOutputStream(theFile);
        fos.write(bitMapData);
        fos.flush();
        fos.close();

        return theFile;
    }

    /*  IMG PROCESSING RELATED METHODS  */
    public static void processBitmapImg(Bitmap capturedImg, Context context) {

        //First, convert the chosen img's bitmap into type "Mat" to be processed by OpenCV
        Mat mat = new Mat();
        Bitmap bm = capturedImg.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bm, mat);

        inputImgMat = new Mat();
        mat.copyTo(inputImgMat);

        //Second, call the scanCard img processing method to get the scanned card mat
        scannedCardMat = new Mat();
        ImgProcPipeline.scanCard(inputImgMat, scannedCardMat, NEW_HEIGHT, FINAL_WIDTH, FINAL_HEIGHT);

        //Third, call the detectFaceInCard method to get the detected face mat
        detectedFaceMat = new Mat();
        ImgProcPipeline.detectFaceInCard(scannedCardMat, detectedFaceMat, context);

        //Fourth, if the img processing pipeline was successful, the scannedCard mat is converted to bitmap to be usable by the app
        if (!scannedCardMat.empty()) {
            scannedCard = null;
            scannedCard = Bitmap.createBitmap(scannedCardMat.width(), scannedCardMat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(scannedCardMat, scannedCard);
        } else {
            Log.e(TAG, "Something wrong with scanning the card image");
            Toast T = Toast.makeText(context, R.string.toast_no_scanned_img, Toast.LENGTH_LONG);
            T.show();
            return;
        }

        //Fifth, if there were a detected face, its mat should also be converted to bitmap
        if (!detectedFaceMat.empty()) {
            detectedFace = null;
            detectedFace = Bitmap.createBitmap(detectedFaceMat.width(), detectedFaceMat.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(detectedFaceMat, detectedFace);
        } else {
            Log.e(TAG, "No face detected");
            Toast T = Toast.makeText(context, R.string.toast_no_face_found, Toast.LENGTH_LONG);
            T.show();
        }

        //Sixth, the resulting bitmaps are mapped to their corresponding files
        mapBitmapsToTheirFiles(context);

        //Seventh, the scanned card is passed through an OCR to recognize the code on the card
        recognizeText();

    }

    /*  put all the bitmaps in their corresponding files */
    public static void mapBitmapsToTheirFiles(Context context) {
        try {
            scannedCardImgFile = createImageFileCardScanned(context);
            scannedCardImgFile = writeBitmapToFile(scannedCard, scannedCardImgFile);


            if (detectedFace != null) {
                detectedFaceImgFile = createImageFileFaceDetected(context);
                detectedFaceImgFile = writeBitmapToFile(detectedFace, detectedFaceImgFile);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error in creating a file (scanned card or detected face)");
        }
    }

    //OCR method
    public static void recognizeText() {

        Bitmap currentCroppedBM = ImgProcPipeline.getCroppedImg();
        recognizedStudentCode = tessOCR.getResults(currentCroppedBM);
    }


}



