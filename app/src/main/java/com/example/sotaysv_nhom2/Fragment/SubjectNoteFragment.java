package com.example.sotaysv_nhom2.Fragment;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sotaysv_nhom2.AlarmReceiver;
import com.example.sotaysv_nhom2.Adapters.NoteAdapter;
import com.example.sotaysv_nhom2.Models.Note;
import com.example.sotaysv_nhom2.NoteActivity;
import com.example.sotaysv_nhom2.R;
import com.example.sotaysv_nhom2.SQLlite.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SubjectNoteFragment extends Fragment {

    private TextView tvSubjectName;
    private RecyclerView rvNotes;
    private FloatingActionButton fabAdd;
    private Toolbar toolbar;

    private NoteAdapter noteAdapter;
    private List<Note> listSubjectNotes;
    private DatabaseHelper databaseHelper;

    private String currentSubjectName = "";
    private int currentSubjectId = -1; // SỬA: Dùng int ID thay vì String code

    // SỬA: Nhận tham số int id
    public static SubjectNoteFragment newInstance(String subjectName, int subjectId) {
        SubjectNoteFragment fragment = new SubjectNoteFragment();
        Bundle args = new Bundle();
        args.putString("SUBJECT_NAME", subjectName);
        args.putInt("SUBJECT_ID", subjectId); // Put Int
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subject_note, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.toolbar_subject_note);
        tvSubjectName = view.findViewById(R.id.tv_subject_name_display);
        rvNotes = view.findViewById(R.id.rv_subject_notes);
        fabAdd = view.findViewById(R.id.fab_add_subject_note);

        // Setup Toolbar
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager() != null) getParentFragmentManager().popBackStack();
        });

        // Lấy dữ liệu từ Bundle
        if (getArguments() != null) {
            currentSubjectName = getArguments().getString("SUBJECT_NAME", "Môn học");
            // SỬA: Lấy Int ID
            currentSubjectId = getArguments().getInt("SUBJECT_ID", -1);
            tvSubjectName.setText("📘 " + currentSubjectName);
        }

        databaseHelper = new DatabaseHelper(getContext());
        listSubjectNotes = new ArrayList<>();
        rvNotes.setLayoutManager(new LinearLayoutManager(getContext()));

        // --- SETUP ADAPTER ---
        noteAdapter = new NoteAdapter(listSubjectNotes, new NoteAdapter.NoteClickListener() {
            @Override
            public void onNoteClick(Note note) {
                openNoteEditor(note);
            }

            @Override
            public void onNoteLongClick(Note note) {
                showDeleteDialog(note);
            }

            @Override
            public void onSelectionChanged(int count) {
            }
        });

        rvNotes.setAdapter(noteAdapter);

        fabAdd.setOnClickListener(v -> createNewSubjectNote());

        loadData();
    }

    @Override public void onResume() { super.onResume(); loadData(); }

    private void loadData() {
        if (databaseHelper == null) return;
        listSubjectNotes.clear();
        List<Note> allNotes = databaseHelper.getAllNotes();

        // SỬA: Logic lọc theo ID (int)
        for (Note note : allNotes) {
            if (note.getSubjectId() == currentSubjectId) { // So sánh int
                listSubjectNotes.add(note);
            }
        }
        noteAdapter.notifyDataSetChanged();
    }

    // --- HỘP THOẠI XÓA ĐƠN ---
    private void showDeleteDialog(Note note) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa ghi chú?")
                .setMessage("Bạn có chắc chắn muốn xóa ghi chú: \"" + note.getTitle() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // 1. Xóa trong Database
                    databaseHelper.deleteNote(note.getId());

                    // 2. Hủy báo thức (nếu có)
                    cancelAlarm(note.getId());

                    // 3. Cập nhật lại list hiển thị
                    loadData();

                    Toast.makeText(getContext(), "Đã xóa!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void cancelAlarm(int noteId) {
        if (getContext() == null) return;
        AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(getContext(), noteId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (am != null) am.cancel(pi);

        // Hủy các báo thức lặp
        for(int i=1; i<=7; i++) {
            PendingIntent piW = PendingIntent.getBroadcast(getContext(), noteId * 100 + i, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            if (am != null) am.cancel(piW);
        }
    }

    private void createNewSubjectNote() {
        openNoteEditor(null);
    }

    private void openNoteEditor(Note note) {
        Intent intent = new Intent(getActivity(), NoteActivity.class);
        if (note == null) {
            // THÊM MỚI
            intent.putExtra("IS_UPDATE", false);
            // SỬA: Truyền SUBJECT_ID (int) thay vì CODE
            intent.putExtra("SUBJECT_ID", currentSubjectId);
            intent.putExtra("PREFILL_TITLE", "Ghi chú: " + currentSubjectName);
        } else {
            // CẬP NHẬT
            intent.putExtra("IS_UPDATE", true);
            intent.putExtra("ID", note.getId());
            intent.putExtra("TITLE", note.getTitle());
            intent.putExtra("CONTENT", note.getContent());
            intent.putExtra("ALARM_TIME", note.getAlarmTime());
            intent.putExtra("REPEAT_TYPE", note.getRepeatType());
            // SỬA: Truyền SUBJECT_ID (int)
            intent.putExtra("SUBJECT_ID", note.getSubjectId());
        }
        startActivity(intent);
    }
}