package com.example.sotaysv_nhom2;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog; // 1. Import thư viện Dialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.sotaysv_nhom2.Models.Note;
import com.example.sotaysv_nhom2.Models.Subject;
import com.example.sotaysv_nhom2.SQLlite.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CalculateTargetActivity extends AppCompatActivity {

    // Views
    private AutoCompleteTextView edtName;
    private TextInputEditText edtCredits, edtTx1, edtTx2, edtTx3;
    private TextInputLayout layoutTx3;
    private Spinner spinnerTargetGrade, spinnerCoefficient;
    private TextView tvResult;
    private Button btnCalculate, btnSaveNote;

    // Data & Helpers
    private DatabaseHelper dbHelper;
    private List<Subject> allSubjectsList;
    private ArrayAdapter<String> coefficientAdapter;

    // Data Sources
    private final String[] listNormal = {"20-20", "15-15", "10-20"}; // Cho <= 4 tín
    private final String[] listSpecial = {"10-10-20"}; // Cho > 4 tín
    private boolean isSpecialMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_target);

        // --- 1. SETUP TOOLBAR ---
        Toolbar toolbar = findViewById(R.id.toolbar_calculate);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // --- 2. INIT DATA ---
        dbHelper = new DatabaseHelper(this);
        allSubjectsList = dbHelper.getAllSubjects();

        // --- 3. MAP VIEWS ---
        edtName = findViewById(R.id.edt_subject_name_target);
        edtCredits = findViewById(R.id.edt_credits_target);
        spinnerCoefficient = findViewById(R.id.spinner_coefficient_target);

        edtTx1 = findViewById(R.id.edt_tx1);
        edtTx2 = findViewById(R.id.edt_tx2);
        edtTx3 = findViewById(R.id.edt_tx3);
        layoutTx3 = findViewById(R.id.layout_tx3);

        spinnerTargetGrade = findViewById(R.id.spinner_target_grade);
        tvResult = findViewById(R.id.tv_result_target);
        btnCalculate = findViewById(R.id.btn_calculate_target);
        btnSaveNote = findViewById(R.id.btn_save_note);

        // --- 4. SETUP SPINNERS ---
        setupTargetSpinner();
        updateCoefficientSpinner(false);

        // --- 5. EVENTS ---
        setupCreditsWatcher();
        setupSubjectSuggestion();

        btnCalculate.setOnClickListener(v -> calculateRequiredScore(false));

        // SỬA ĐỔI: Gọi hàm check trước khi lưu
        btnSaveNote.setOnClickListener(v -> checkBeforeSave());
    }

    // --- SETUP DATA CHO SPINNER MỤC TIÊU ---
    private void setupTargetSpinner() {
        List<String> grades = new ArrayList<>();
        grades.add("A  (8.5)"); grades.add("B+ (8.0)"); grades.add("B  (7.0)");
        grades.add("C+ (6.5)"); grades.add("C  (5.5)"); grades.add("D+ (5.0)"); grades.add("D  (4.0)");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, grades);
        spinnerTargetGrade.setAdapter(adapter);
    }

    // --- LOGIC ĐỔI LIST HỆ SỐ DỰA TRÊN TÍN CHỈ ---
    private void updateCoefficientSpinner(boolean useSpecialList) {
        if (this.isSpecialMode == useSpecialList && spinnerCoefficient.getAdapter() != null) return;

        this.isSpecialMode = useSpecialList;
        String[] currentList = useSpecialList ? listSpecial : listNormal;

        coefficientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, currentList);
        spinnerCoefficient.setAdapter(coefficientAdapter);
        spinnerCoefficient.setSelection(0);
    }

    private void setupCreditsWatcher() {
        edtCredits.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty()) {
                    try {
                        int credits = Integer.parseInt(s.toString());
                        if(credits >= 4) {
                            layoutTx3.setVisibility(View.VISIBLE);
                            layoutTx3.setHint("GK");
                            updateCoefficientSpinner(true);
                        } else {
                            layoutTx3.setVisibility(View.GONE);
                            edtTx3.setText("");
                            updateCoefficientSpinner(false);
                        }
                    } catch (NumberFormatException e) {
                        layoutTx3.setVisibility(View.GONE);
                    }
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    // --- LOGIC GỢI Ý VÀ TỰ ĐIỀN THÔNG TIN MÔN CŨ ---
    private void setupSubjectSuggestion() {
        Set<String> subjectNames = new HashSet<>();
        for (Subject sub : allSubjectsList) {
            if (sub.getName() != null) subjectNames.add(sub.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(subjectNames));
        edtName.setAdapter(adapter);

        edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String inputName = s.toString().trim();
                Subject foundSubject = null;
                for (Subject sub : allSubjectsList) {
                    if (sub.getName().equalsIgnoreCase(inputName)) {
                        foundSubject = sub;
                        break;
                    }
                }

                if (foundSubject != null) {
                    edtCredits.setText(String.valueOf(foundSubject.getCredits()));
                    String savedCoeff = foundSubject.getCoefficient();
                    if(savedCoeff != null) {
                        edtCredits.post(() -> {
                            for(int i=0; i < coefficientAdapter.getCount(); i++) {
                                if(coefficientAdapter.getItem(i).toString().equals(savedCoeff)){
                                    spinnerCoefficient.setSelection(i);
                                    break;
                                }
                            }
                        });
                    }

                    edtCredits.setEnabled(false);
                    spinnerCoefficient.setEnabled(false);

                    // --- [NEW] CẢNH BÁO NẾU MÔN ĐÃ CÓ ĐIỂM ---
                    // Giả sử điểm >= 0 là đã học xong (-1 là chưa học)
                    if (foundSubject.getScore10() >= 0) {
                        Toast.makeText(CalculateTargetActivity.this,
                                "Môn này đã kết thúc với điểm số: " + foundSubject.getScore10(),
                                Toast.LENGTH_LONG).show();

                        // Có thể hiển thị cảnh báo lên giao diện nếu muốn
                        tvResult.setText("Đã có điểm: " + foundSubject.getScore10());
                        tvResult.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    }

                } else {
                    // KHÔNG TÌM THẤY: Mở khóa
                    edtCredits.setEnabled(true);
                    spinnerCoefficient.setEnabled(true);
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private double getValidScore(TextInputEditText edt, String fieldName) throws Exception {
        String txt = edt.getText() == null ? "" : edt.getText().toString().trim();
        if (txt.isEmpty()) throw new Exception("Vui lòng nhập " + fieldName);
        double score = Double.parseDouble(txt);
        if (score < 0 || score > 10) throw new Exception(fieldName + " phải từ 0-10");
        return score;
    }

    private double[] calculateLogic() throws Exception {
        double targetScore = 8.5;
        int pos = spinnerTargetGrade.getSelectedItemPosition();
        double[] values = {8.5, 8.0, 7.0, 6.5, 5.5, 5.0, 4.0};
        if(pos >= 0 && pos < values.length) targetScore = values[pos];

        double tx1 = getValidScore(edtTx1, "TX1");
        double tx2 = getValidScore(edtTx2, "TX2");

        String coeffStr = spinnerCoefficient.getSelectedItem().toString();
        String[] parts = coeffStr.split("-");

        double weightedSum = 0;
        double examPercent = 0;

        if (parts.length == 2) {
            double p1 = Double.parseDouble(parts[0]) / 100.0;
            double p2 = Double.parseDouble(parts[1]) / 100.0;
            weightedSum = (tx1 * p1) + (tx2 * p2);
            examPercent = 1.0 - (p1 + p2);
        }
        else if (parts.length == 3) {
            if (layoutTx3.getVisibility() != View.VISIBLE) throw new Exception("Lỗi hệ thống: Chưa hiện ô GK");
            double tx3 = getValidScore(edtTx3, "GK");
            double p1 = Double.parseDouble(parts[0]) / 100.0;
            double p2 = Double.parseDouble(parts[1]) / 100.0;
            double p3 = Double.parseDouble(parts[2]) / 100.0;
            weightedSum = (tx1 * p1) + (tx2 * p2) + (tx3 * p3);
            examPercent = 1.0 - (p1 + p2 + p3);
        }

        double requiredExam = (targetScore - weightedSum) / examPercent;
        return new double[]{requiredExam, targetScore};
    }

    private void calculateRequiredScore(boolean isSilent) {
        try {
            double[] result = calculateLogic();
            double requiredY = result[0];

            if (requiredY > 10) {
                tvResult.setText("Cần thi: " + String.format("%.2f", requiredY) + "\n(Bất khả thi 😭)");
                tvResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else if (requiredY <= 0) {
                tvResult.setText("Đã đạt mục tiêu! ✅");
                tvResult.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvResult.setText(String.format("%.2f", requiredY));
                tvResult.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            }
        } catch (Exception e) {
            if(!isSilent) Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            tvResult.setText("---");
        }
    }

    // --- HÀM KIỂM TRA TRƯỚC KHI LƯU ---
    private void checkBeforeSave() {
        String subjectName = edtName.getText().toString().trim();
        if(subjectName.isEmpty()) {
            Toast.makeText(this, "Chưa nhập tên môn!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tìm xem môn này đã có trong list chưa
        Subject foundSubject = null;
        for (Subject sub : allSubjectsList) {
            if (sub.getName().equalsIgnoreCase(subjectName)) {
                foundSubject = sub;
                break;
            }
        }

        // Nếu tìm thấy và ĐÃ CÓ ĐIỂM (finalScore >= 0)
        if (foundSubject != null && foundSubject.getScore10() >= 0) {
            // Hiện Dialog xác nhận
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận lưu")
                    .setMessage("Môn '" + subjectName + "' đã có điểm tổng kết (" + foundSubject.getScore10() + ").\n\nBạn có chắc chắn muốn lưu thêm dự tính cho môn đã học xong này không?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Lưu luôn", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Người dùng chọn OK -> Tiến hành lưu
                            performSaveNote(subjectName);
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        } else {
            // Môn mới hoặc chưa có điểm -> Lưu luôn
            performSaveNote(subjectName);
        }
    }

    private void performSaveNote(String subjectName) {
        try {
            // Validate lại
            if (edtCredits.getText().toString().isEmpty()) throw new Exception("Nhập tín chỉ!");

            // Tính toán lại để lấy số liệu
            double[] result = calculateLogic();
            double requiredExam = result[0];

            String coeffStr = spinnerCoefficient.getSelectedItem().toString();
            int credits = Integer.parseInt(edtCredits.getText().toString());

            // 1. TÌM HOẶC TẠO MÔN HỌC (Logic cũ)
            int subjectId = -1;
            Subject existingSubject = null;
            for (Subject s : allSubjectsList) {
                if (s.getName().equalsIgnoreCase(subjectName)) {
                    existingSubject = s;
                    break;
                }
            }

            if (existingSubject != null) {
                subjectId = existingSubject.getId();
            } else {
                Subject newSub = new Subject(subjectName, coeffStr, credits, -1.0, 1);
                dbHelper.addSubject(newSub);

                // Lấy lại ID vừa tạo
                List<Subject> updatedList = dbHelper.getAllSubjects();
                for(Subject s : updatedList) {
                    if(s.getName().equalsIgnoreCase(subjectName)) {
                        subjectId = s.getId();
                        break;
                    }
                }
                Toast.makeText(this, "Đã tạo môn mới: " + subjectName, Toast.LENGTH_SHORT).show();
            }

            // 2. TẠO NỘI DUNG GHI CHÚ
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            String time = sdf.format(new Date());

            StringBuilder content = new StringBuilder();
            content.append("Cấu trúc điểm: ").append(coeffStr).append("\n");
            content.append("• TX1: ").append(edtTx1.getText()).append("\n");
            content.append("• TX2: ").append(edtTx2.getText()).append("\n");
            if(layoutTx3.getVisibility() == View.VISIBLE) {
                content.append("• GK: ").append(edtTx3.getText()).append("\n");
            }
            content.append("----------------\n");
            content.append(" Mục tiêu: ").append(spinnerTargetGrade.getSelectedItem()).append("\n");
            String status;
            if (requiredExam > 10) status = "Không thể đạt (Cần > 10)";
            else if (requiredExam <= 0) status = "Đã chắc chắn đạt ";
            else status = "Cần thi: " + String.format("%.2f", requiredExam);
            content.append("").append(status);
            // 3. LƯU VÀO DB
            Note note = new Note("Dự tính: " + subjectName, content.toString(), "#4CAF50", time, 0, subjectId
            );
            dbHelper.addNote(note);
            Toast.makeText(this, "Đã lưu ghi chú thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}