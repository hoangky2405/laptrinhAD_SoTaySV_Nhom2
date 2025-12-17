package com.example.sotaysv_nhom2.Models;

public class Subject {
    private int id;
    // Đã bỏ trường code
    private String name;
    private String coefficient;
    private int credits;
    private double score10;
    private int semester;

    // Constructor đầy đủ (dùng khi lấy từ DB ra)
    public Subject(int id, String name, String coefficient, int credits, double score10, int semester) {
        this.id = id;
        this.name = name;
        this.coefficient = coefficient;
        this.credits = credits;
        this.score10 = score10;
        this.semester = semester;
    }

    // Constructor rút gọn (dùng khi thêm mới, ID tự tăng)
    public Subject(String name, String coefficient, int credits, double score10, int semester) {
        this.name = name;
        this.coefficient = coefficient;
        this.credits = credits;
        this.score10 = score10;
        this.semester = semester;
    }

    // --- Getters & Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(String coefficient) {
        this.coefficient = coefficient;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public double getScore10() {
        return score10;
    }

    public void setScore10(double score10) {
        this.score10 = score10;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public boolean isStudying() {
        return score10 < 0;
    }
}