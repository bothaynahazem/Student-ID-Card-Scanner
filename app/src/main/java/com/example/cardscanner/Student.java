package com.example.cardscanner;

public class Student {
    private String Name, Grade, Code;
    private int BN, Section;

    public Student(String N, String G, int B, String C, int S) {
        Name = N;
        Grade = G;
        BN = B;
        Code = C;
        Section = S;
    }

    public int getBN() {
        return BN;
    }

    public String getCode() {
        return Code;
    }

    public String getGrade() {
        return Grade;
    }

    public int getSection() {
        return Section;
    }

    public String getName() {
        return Name;
    }
}
