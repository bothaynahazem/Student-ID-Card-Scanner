package com.example.cardscanner;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class CardInspection extends AppCompatActivity {

    private static final String TAG = CardInspection.class.getSimpleName();
    public static Bitmap imgBM;
    private String imgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (ReviewScannedCard.cardTypeChoice == 0)
            setContentView(R.layout.activity_card_inspection);
        else
            setContentView(R.layout.activity_metro_card_inspect);


        ImageView faceImg = findViewById(R.id.img_the_face_photo);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("faceURI"))
            imgPath = extras.getString("faceURI");

        if (imgPath != null) {
            try {
                imgBM = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(),
                        Uri.parse(imgPath));

            } catch (IOException e) {
                e.printStackTrace();
            }


            if (imgBM != null)
                faceImg.setImageBitmap(imgBM);
        }


        if (ReviewScannedCard.cardTypeChoice == 0) {

            TextView studentName = findViewById(R.id.txt_stud_name);
            TextView studentCode = findViewById(R.id.txt_stud_code);
            TextView studentYear = findViewById(R.id.txt_stud_year);
            TextView studentSection = findViewById(R.id.txt_stud_section);
            TextView studentBN = findViewById(R.id.txt_stud_bn);
            TextView studentGrade = findViewById(R.id.txt_stud_grade);


            String sName, sCode, sGrade, sYear;
            int sBN, sSection;

            if (parseJSON.currentStudent != null) {
                sName = parseJSON.currentStudent.getName();
                sCode = parseJSON.currentStudent.getCode();
                sGrade = parseJSON.currentStudent.getGrade();
                sSection = parseJSON.currentStudent.getSection();
                sBN = parseJSON.currentStudent.getBN();
                sYear = Integer.toString(sBN).substring(0, 1) + "th"; //4th for example

                studentName.setText(sName);
                studentCode.setText(sCode);
                studentYear.setText(sYear);
                studentSection.setText(Integer.toString(sSection));
                studentBN.setText(Integer.toString(sBN));
                studentGrade.setText(sGrade);

            } else {
                Log.e(TAG, "something wrong with the recognized code");
                Toast T = Toast.makeText(
                        getApplicationContext(),
                        R.string.toast_no_info_inspected,
                        Toast.LENGTH_SHORT);
                T.show();
            }
        } else { //Metro card

            TextView studentMetroName = findViewById(R.id.txt_metro_stud_name);
            TextView studentMetroCode = findViewById(R.id.txt_metro_stud_code);
            TextView studentFaculty = findViewById(R.id.txt_metro_faculty);
            TextView studentUniversity = findViewById(R.id.txt_metro_university);
            TextView numTripsLeft = findViewById(R.id.txt_metro_num_trips_left);


            String sName, sCode, sFaculty, sUni;
            int sNumTripsLeft;

            if (parseJSON.currentStudentMetroCard != null) {
                sName = parseJSON.currentStudentMetroCard.getName();
                sCode = parseJSON.currentStudentMetroCard.getCode();
                sFaculty = parseJSON.currentStudentMetroCard.getFaculty();
                sUni = parseJSON.currentStudentMetroCard.getUniversity();
                sNumTripsLeft = parseJSON.currentStudentMetroCard.getNumTripsLeft();


                studentMetroName.setText(sName);
                studentFaculty.setText(sFaculty);
                studentMetroCode.setText(sCode);
                studentUniversity.setText(sUni);
                numTripsLeft.setText(Integer.toString(sNumTripsLeft));

            } else {
                Log.e(TAG, "something wrong with the recognized code");
                Toast T = Toast.makeText(
                        getApplicationContext(),
                        R.string.toast_no_info_inspected,
                        Toast.LENGTH_SHORT);
                T.show();
            }

            ReviewScannedCard.cardTypeChoice = -1; //flush the choice


        }
    }
}
