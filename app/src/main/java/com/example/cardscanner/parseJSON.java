package com.example.cardscanner;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class parseJSON {

    private static final String TAG = parseJSON.class.getSimpleName();
    public static String currentJsonStr;

    public static HashMap<String, Student> codeToStudent;
    public static Student currentStudent;

    public static HashMap<String, StudentMetroInfo> codeToStudentMetro;
    public static StudentMetroInfo currentStudentMetroCard;
    public static boolean metroCard = false;


    public static void fetchStudentInfoFromCode(String studentCode, Context context) {

        if (codeToStudent == null) //first time only, supposing we're dealing with one JSON file only
            loadJSONIntoMap(context);

        currentStudent = codeToStudent.get(studentCode);

    }

    public static void fetchStudentInfoFromCodeMetro(String studentCode, Context context) {

        if (codeToStudentMetro == null) //first time only, supposing we're dealing with one JSON file only
            loadJSONIntoMapMetro(context);

        currentStudentMetroCard = codeToStudentMetro.get(studentCode);


    }

    private static void loadJSONIntoMap(Context context) {
        //load the json string from the json file of names list
        currentJsonStr = loadJSONFromAssets(context);

        try {
            //parse the json string
            JSONArray jsonArray = new JSONArray(currentJsonStr);
            codeToStudent = new HashMap<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject currentStudent = (JSONObject) jsonArray.get(i);

                int BN, Section;
                String Code, Name, Grade;
                Code = currentStudent.getString("Code");
                BN = currentStudent.getInt("BN");
                Section = currentStudent.getInt("Section");
                Name = currentStudent.getString("Name");
                Grade = currentStudent.getString("Grade");

                //add the current student to the map
                Student student = new Student(Name, Grade, BN, Code, Section);
                codeToStudent.put(Code, student);

            }


        } catch (JSONException e) {
            Log.e(TAG, "Something wrong with parsing the JSON");
            e.printStackTrace();
        }

    }

    private static void loadJSONIntoMapMetro(Context context) {
        //load the json string from the json file of names list
        currentJsonStr = loadJSONFromAssetsMetro(context);

        try {
            //parse the json string
            JSONArray jsonArray = new JSONArray(currentJsonStr);
            codeToStudentMetro = new HashMap<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject currentStudent = (JSONObject) jsonArray.get(i);

                int numTripsLeft;
                String Code, Name, Faculty, University;
                Code = currentStudent.getString("Code");
                Name = currentStudent.getString("Name");
                Faculty = currentStudent.getString("Faculty");
                University = currentStudent.getString("University");
                numTripsLeft = currentStudent.getInt("TripsLeft");

                //add the current student to the map
                StudentMetroInfo student = new StudentMetroInfo(Name, Code, University, Faculty, numTripsLeft);
                codeToStudentMetro.put(Code, student);
            }


        } catch (JSONException e) {
            Log.e(TAG, "Something wrong with parsing the JSON");
            e.printStackTrace();
        }

    }

    private static String loadJSONFromAssets(Context context) {

        String jsonStr = null;
        try {
            InputStream is = context.getAssets().open("JSON/CSE19_NamesList.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            jsonStr = new String(buffer, StandardCharsets.UTF_8);

        } catch (IOException ex) {
            Log.e(TAG, "couldn't load the json string");
            ex.printStackTrace();
            return null;
        }

        Log.i(TAG, "parsed the JSON successfully!");
        return jsonStr;

    }


    private static String loadJSONFromAssetsMetro(Context context) {

        String jsonStr = null;
        try {
            InputStream is = context.getAssets().open("JSON/Metro_NamesList.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            jsonStr = new String(buffer, StandardCharsets.UTF_8);

        } catch (IOException ex) {
            Log.e(TAG, "couldn't load the json string");
            ex.printStackTrace();
            return null;
        }

        Log.i(TAG, "parsed the JSON successfully!");
        return jsonStr;

    }

}
