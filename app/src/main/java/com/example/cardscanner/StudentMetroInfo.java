package com.example.cardscanner;

public class StudentMetroInfo {
    private String Name, Code, University, Faculty;
    private int numTripsLeft;

    public StudentMetroInfo(String N, String C, String U, String F, int T) {
        Name = N;
        Code = C;
        University = U;
        Faculty = F;
        numTripsLeft = T;
    }

    public int getNumTripsLeft() {
        return numTripsLeft;
    }

    public String getCode() {
        return Code;
    }

    public String getFaculty() {
        return Faculty;
    }

    public String getName() {
        return Name;
    }

    public String getUniversity() {
        return University;
    }
}
