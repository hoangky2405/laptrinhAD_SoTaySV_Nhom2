package com.example.sotaysv_nhom2.Models;

public class Note {
    private int id;
    private String title;       // Tiêu đề
    private String content;     // Nội dung
    private String alarmTime;   // Giờ báo thức (VD: "08:00")
    private String dateTime;    // Ngày giờ tạo (VD: "25/11 - 10:30")
    private int repeatType;     // 0: Một lần, 1: Hàng ngày, 2: Hàng tuần
    private int subjectId;      // ID của môn học liên kết (0 nếu là note thường)

    // Constructor đầy đủ (7 tham số)
    public Note(int id, String title, String content, String alarmTime, String dateTime, int repeatType, int subjectId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.alarmTime = alarmTime;
        this.dateTime = dateTime;
        this.repeatType = repeatType;
        this.subjectId = subjectId;
    }

    public Note( String title, String content, String alarmTime, String dateTime, int repeatType, int subjectId) {
        this.title = title;
        this.content = content;
        this.alarmTime = alarmTime;
        this.dateTime = dateTime;
        this.repeatType = repeatType;
        this.subjectId = subjectId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getAlarmTime() {
        return alarmTime;
    }
    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }
    public String getDateTime() {
        return dateTime;
    }
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
    public int getRepeatType() {
        return repeatType;
    }
    public void setRepeatType(int repeatType) {
        this.repeatType = repeatType;
    }
    public int getSubjectId() {
        return subjectId;
    }
    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }
    public String getTime() {
        return alarmTime;
    }
}