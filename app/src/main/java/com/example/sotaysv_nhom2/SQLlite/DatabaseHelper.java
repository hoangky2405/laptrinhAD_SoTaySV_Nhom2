package com.example.sotaysv_nhom2.SQLlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.sotaysv_nhom2.Models.Note;
import com.example.sotaysv_nhom2.Models.Subject;
import java.util.ArrayList;
import java.util.List;

// Đã xóa import java.util.UUID

public class DatabaseHelper extends SQLiteOpenHelper {
    // Nếu bạn muốn reset lại DB mới hoàn toàn, hãy đổi tên file hoặc tăng version lên
    private static final String DATABASE_NAME = "SoTaySinhVien_NoUUID.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_SUBJECT = "subjects";
    private static final String TABLE_NOTE = "notes";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Bảng Subject: Bỏ cột 'code'
        db.execSQL("CREATE TABLE " + TABLE_SUBJECT + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "coefficient TEXT, " +
                "credits INTEGER, " +
                "score REAL, " +
                "semester INTEGER)");

        // Bảng Note: Bỏ cột 'code', thay 'subject_code' bằng 'subject_id' (INTEGER)
        db.execSQL("CREATE TABLE " + TABLE_NOTE + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "content TEXT, " +
                "alarm_time TEXT, " +
                "date_time TEXT, " +
                "repeat_type INTEGER, " +
                "subject_id INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBJECT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE);
        onCreate(db);
    }

    // --- SUBJECT ---

    public void addSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // ID tự tăng nên không cần put vào đây
        values.put("name", subject.getName());
        values.put("coefficient", subject.getCoefficient());
        values.put("credits", subject.getCredits());
        values.put("score", subject.getScore10());
        values.put("semester", subject.getSemester());

        db.insert(TABLE_SUBJECT, null, values);
        db.close();
    }

    public void updateSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", subject.getName());
        values.put("coefficient", subject.getCoefficient());
        values.put("credits", subject.getCredits());
        values.put("score", subject.getScore10());
        values.put("semester", subject.getSemester());

        db.update(TABLE_SUBJECT, values, "id = ?", new String[]{String.valueOf(subject.getId())});
        db.close();
    }

    public void deleteSubject(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 1. Xóa tất cả Note liên quan đến Subject này trước (dùng subject_id)
        db.delete(TABLE_NOTE, "subject_id = ?", new String[]{String.valueOf(id)});

        // 2. Xóa Subject
        db.delete(TABLE_SUBJECT, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Subject> getAllSubjects() {
        List<Subject> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SUBJECT + " ORDER BY semester ASC", null);

        if (cursor.moveToFirst()) {
            do {
                // Thứ tự cột: 0:id, 1:name, 2:coefficient, 3:credits, 4:score, 5:semester
                list.add(new Subject(
                        cursor.getInt(0), // id
                        cursor.getString(1), // name
                        cursor.getString(2), // coefficient
                        cursor.getInt(3),    // credits
                        cursor.getDouble(4), // score
                        cursor.getInt(5)     // semester
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    // --- NOTE ---

    public void addNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("title", note.getTitle());
        values.put("content", note.getContent());
        values.put("alarm_time", note.getAlarmTime());
        values.put("date_time", note.getDateTime());
        values.put("repeat_type", note.getRepeatType());
        values.put("subject_id", note.getSubjectId()); // Lưu subject_id dạng Int

        db.insert(TABLE_NOTE, null, values);
        db.close();
    }

    public void updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("title", note.getTitle());
        values.put("content", note.getContent());
        values.put("alarm_time", note.getAlarmTime());
        values.put("date_time", note.getDateTime());
        values.put("repeat_type", note.getRepeatType());
        values.put("subject_id", note.getSubjectId());

        db.update(TABLE_NOTE, values, "id = ?", new String[]{String.valueOf(note.getId())});
        db.close();
    }

    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTE, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Note> getAllNotes() {
        List<Note> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Sắp xếp theo ID giảm dần (mới nhất lên đầu)
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NOTE + " ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                // Thứ tự cột: 0:id, 1:title, 2:content, 3:alarm, 4:date, 5:repeat, 6:subject_id
                list.add(new Note(
                        cursor.getInt(0),      // id
                        cursor.getString(1),   // title
                        cursor.getString(2),   // content
                        cursor.getString(3),   // alarm_time
                        cursor.getString(4),   // date_time
                        cursor.getInt(5),      // repeat_type
                        cursor.getInt(6)       // subject_id
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}