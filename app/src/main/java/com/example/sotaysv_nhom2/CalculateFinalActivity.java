package com.example.sotaysv_nhom2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.sotaysv_nhom2.Models.GradeUtils;
import com.example.sotaysv_nhom2.Models.Subject;
import com.example.sotaysv_nhom2.SQLlite.DatabaseHelper;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalculateFinalActivity extends AppCompatActivity {

    private AutoCompleteTextView edtName;
    private EditText edtCredits, edtSemester, edtTx1, edtTx2, edtTx3, edtExam;
    private TextInputLayout inputLayoutTx3;
    private Spinner spinnerCoefficient;

    private TextView tvScore, tvLetter;
    private Button btnCalc, btnSave;
    private DatabaseHelper dbHelper;

    private List<Subject> allSubjectsList;

    // --- CẤU HÌNH DANH SÁCH HỆ SỐ ---
    // Danh sách cho môn thường (<= 4 tín chỉ)
    private final String[] listNormal = {"20-20", "15-15", "10-20"};
    // Danh sách cho môn lớn (> 4 tín chỉ) - Chỉ có 10-10-20
    private final String[] listSpecial = {"10-10-20"};

    private ArrayAdapter<String> spinnerAdapter;
    private boolean isSpecialMode = false; // Biến cờ để kiểm soát trạng thái spinner

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_final);

        // --- SETUP TOOLBAR ---
        Toolbar toolbar = findViewById(R.id.toolbar_final);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // --- KHỞI TẠO DATABASE ---
        dbHelper = new DatabaseHelper(this);
        allSubjectsList = dbHelper.getAllSubjects();

        // --- ÁNH XẠ VIEW ---
        edtName = findViewById(R.id.edt_subject_name);
        edtCredits = findViewById(R.id.edt_credits);
        edtSemester = findViewById(R.id.edt_semester);
        edtTx1 = findViewById(R.id.edt_tx1);
        edtTx2 = findViewById(R.id.edt_tx2);
        edtTx3 = findViewById(R.id.edt_tx3);
        inputLayoutTx3 = findViewById(R.id.layout_tx3); // Layout chứa ô TX3
        edtExam = findViewById(R.id.edt_exam_score);
        spinnerCoefficient = findViewById(R.id.spinnerCoefficient);

        tvScore = findViewById(R.id.tv_final_score);
        tvLetter = findViewById(R.id.tv_final_letter);
        btnCalc = findViewById(R.id.btn_calc);
        btnSave = findViewById(R.id.btn_save);

        // Mặc định ban đầu load list thường
        updateSpinnerData(false);

        // --- 1. SỰ KIỆN NHẬP TÍN CHỈ (QUAN TRỌNG) ---
        edtCredits.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty()) {
                    try {
                        int credits = Integer.parseInt(s.toString());

                        // LOGIC: Nếu Tín chỉ > 4
                        if(credits >= 4) {
                            // 1. Đổi Spinner sang chế độ đặc biệt (Chỉ có 10-10-20)
                            updateSpinnerData(true);

                            // 2. Hiện ô nhập điểm thứ 3
                            inputLayoutTx3.setVisibility(View.VISIBLE);
                            inputLayoutTx3.setHint("Giữa kỳ (x2)");
                        } else {
                            updateSpinnerData(false);

                            inputLayoutTx3.setVisibility(View.GONE);
                            edtTx3.setText("");
                        }
                    } catch (NumberFormatException e) {
                        // Nếu lỗi nhập liệu, mặc định về chế độ thường
                        updateSpinnerData(false);
                        inputLayoutTx3.setVisibility(View.GONE);
                    }
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // --- 2. GỢI Ý MÔN HỌC & TỰ ĐỘNG ĐIỀN ---
        setupSubjectSuggestionAndAutoLock();

        // --- 3. SỰ KIỆN BUTTON ---
        btnCalc.setOnClickListener(v -> calculateScore(false));
        btnSave.setOnClickListener(v -> saveToSubject());
    }

    /**
     * Hàm thay đổi dữ liệu cho Spinner dựa trên chế độ
     * @param useSpecialList true: dùng list 10-10-20, false: dùng list thường
     */
    private void updateSpinnerData(boolean useSpecialList) {
        // Nếu trạng thái chưa đổi thì không cần set lại adapter (tránh lag)
        if (this.isSpecialMode == useSpecialList && spinnerCoefficient.getAdapter() != null) return;

        this.isSpecialMode = useSpecialList;
        String[] currentList = useSpecialList ? listSpecial : listNormal;

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, currentList);
        spinnerCoefficient.setAdapter(spinnerAdapter);
        spinnerCoefficient.setSelection(0);
    }

    private void setupSubjectSuggestionAndAutoLock() {
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
                    // --- TÌM THẤY MÔN CŨ ---

                    // 1. Điền Tín chỉ trước -> Nó sẽ kích hoạt TextWatcher của edtCredits
                    // -> TextWatcher đó sẽ tự động cập nhật Spinner và hiển thị ô TX3 nếu cần
                    edtCredits.setText(String.valueOf(foundSubject.getCredits()));
                    edtSemester.setText(String.valueOf(foundSubject.getSemester()));

                    // 2. Chọn lại đúng item trong Spinner (sau khi adapter đã được update bởi bước 1)
                    String savedCoeff = foundSubject.getCoefficient();
                    if(savedCoeff != null) {
                        for(int i = 0; i < spinnerAdapter.getCount(); i++) {
                            if(spinnerAdapter.getItem(i).toString().equals(savedCoeff)){
                                spinnerCoefficient.setSelection(i);
                                break;
                            }
                        }
                    }

                    // 3. Khóa các trường không cho sửa
                    edtCredits.setEnabled(false);
                    edtSemester.setEnabled(false);
                    spinnerCoefficient.setEnabled(false);
                } else {
                    // --- MÔN MỚI HOẶC ĐỔI TÊN ---
                    edtCredits.setEnabled(true);
                    edtSemester.setEnabled(true);
                    spinnerCoefficient.setEnabled(true);

                    // Trigger lại logic để đảm bảo spinner đúng với số tín chỉ đang hiển thị
                    String curCredits = edtCredits.getText().toString();
                    if(!curCredits.isEmpty()) edtCredits.setText(curCredits);
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private double getValidScore(EditText edt, String fieldName) throws Exception {
        String txt = edt.getText().toString().trim();
        if (txt.isEmpty()) throw new Exception("Vui lòng nhập " + fieldName);
        double score = Double.parseDouble(txt);
        if (score < 0 || score > 10) throw new Exception(fieldName + " phải từ 0-10!");
        return score;
    }

    private double[] calculateData() throws Exception {
        // 1. Lấy điểm nhập vào
        double tx1 = getValidScore(edtTx1, "TX1");
        double tx2 = getValidScore(edtTx2, "TX2");
        double exam = getValidScore(edtExam, "Điểm thi");

        // 2. Lấy hệ số từ Spinner (VD: "20-20" hoặc "10-10-20")
        String coefficientString = spinnerCoefficient.getSelectedItem().toString();
        String[] parts = coefficientString.split("-");

        double finalScore = 0;
        double processScore = 0; // Dùng để hiển thị điểm bộ phận (tham khảo)

        // (VD: 20-20, 15-15...) ---
        if (parts.length == 2) {
            // Parse phần trăm: "20" -> 0.2
            double percentTx1 = Double.parseDouble(parts[0]) / 100.0;
            double percentTx2 = Double.parseDouble(parts[1]) / 100.0;

            double percentExam = 1.0 - (percentTx1 + percentTx2);

            finalScore = (tx1 * percentTx1) + (tx2 * percentTx2) + (exam * percentExam);

            processScore = (tx1 + tx2) / 2;
        }

        //(VD: 10-10-20) ---
        else if (parts.length == 3) {
            // Phải nhập thêm TX3
            if (inputLayoutTx3.getVisibility() != View.VISIBLE) {
                throw new Exception("Hệ thống lỗi: Chưa hiện ô nhập điểm giữa kỳ!");
            }
            double tx3 = getValidScore(edtTx3, "Giữa kỳ");

            double percentTx1 = Double.parseDouble(parts[0]) / 100.0;
            double percentTx2 = Double.parseDouble(parts[1]) / 100.0;
            double percentTx3 = Double.parseDouble(parts[2]) / 100.0; // TX3 (Giữa kỳ)

            double percentExam = 1.0 - (percentTx1 + percentTx2 + percentTx3);

            finalScore = (tx1 * percentTx1) + (tx2 * percentTx2) + (tx3 * percentTx3) + (exam * percentExam);

            processScore = (tx1 + tx2 + tx3 * 2) / 4;
        }

        finalScore = Math.round(finalScore * 100.0) / 100.0;

        return new double[]{processScore, finalScore};
    }

    private void calculateScore(boolean isSaving) {
        try {
            if(isSaving) validateInfo();
            double[] result = calculateData();
            double finalScore = result[1];

            String letter = GradeUtils.convertToLetter(finalScore);
            double scale4 = GradeUtils.convertToScale4(finalScore);

            tvScore.setText(String.format("%.2f", finalScore));
            tvLetter.setText("Điểm chữ: " + letter + " (" + scale4 + ")");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToSubject() {
        try {
            validateInfo();
            double[] result = calculateData();
            double finalScore = result[1];

            String name = edtName.getText().toString().trim();
            int credits = Integer.parseInt(edtCredits.getText().toString());
            int semester = Integer.parseInt(edtSemester.getText().toString());

            // Lấy giá trị spinner an toàn (tránh null)
            Object selectedObj = spinnerCoefficient.getSelectedItem();
            String coefficient = (selectedObj != null) ? selectedObj.toString() : "20-20";

            Subject existingSubject = null;
            for (Subject s : allSubjectsList) {
                if (s.getName().equalsIgnoreCase(name)) {
                    existingSubject = s;
                    break;
                }
            }

            if (existingSubject != null) {
                Subject updateSubject = new Subject(
                        existingSubject.getId(), name, coefficient, credits, finalScore, semester
                );
                dbHelper.updateSubject(updateSubject);
                Toast.makeText(this, "Đã cập nhật điểm môn: " + name, Toast.LENGTH_SHORT).show();
            } else {
                Subject newSubject = new Subject(name, coefficient, credits, finalScore, semester);
                dbHelper.addSubject(newSubject);
                Toast.makeText(this, "Đã thêm môn mới: " + name, Toast.LENGTH_SHORT).show();
            }
            finish();

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void validateInfo() throws Exception {
        if (edtName.getText().toString().trim().isEmpty()) throw new Exception("Nhập tên môn!");
        if (edtCredits.getText().toString().isEmpty()) throw new Exception("Nhập số tín chỉ!");
        if (edtSemester.getText().toString().isEmpty()) throw new Exception("Nhập học kỳ!");
    }
}