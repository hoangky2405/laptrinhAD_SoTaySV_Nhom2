package com.example.sotaysv_nhom2.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sotaysv_nhom2.Adapters.SemesterAdapter;
import com.example.sotaysv_nhom2.Adapters.SubjectAdapter;
import com.example.sotaysv_nhom2.CalculateTargetActivity;
import com.example.sotaysv_nhom2.CalculateFinalActivity;
import com.example.sotaysv_nhom2.Models.GradeUtils;
import com.example.sotaysv_nhom2.Models.Subject;
import com.example.sotaysv_nhom2.R;
import com.example.sotaysv_nhom2.SQLlite.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private List<Subject> listAllSubjects, listDisplaySubjects;
    private SubjectAdapter subjectAdapter;
    private SemesterAdapter semesterAdapter;
    private DatabaseHelper databaseHelper;
    private TextView tvGpa, tvStatus;
    private CardView cardGpa;
    private RecyclerView rvSubjects, rvSemesterFilter;
    private int currentSemesterIndex = 0;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Đã xóa setHasOptionsMenu(true); vì deprecated
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.my_toolbar);
        if (getActivity() != null) { ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar); }

        // --- CẤU HÌNH MENU MỚI (MenuProvider) ---
        setupMenu();

        tvGpa = view.findViewById(R.id.tv_gpa);
        tvStatus = view.findViewById(R.id.tv_status);
        cardGpa = view.findViewById(R.id.card_gpa);
        rvSubjects = view.findViewById(R.id.rv_subjects);
        rvSemesterFilter = view.findViewById(R.id.rv_semester_filter);

        databaseHelper = new DatabaseHelper(getContext());
        listAllSubjects = new ArrayList<>();
        listDisplaySubjects = new ArrayList<>();

        loadDataFromDB();
        setupSubjectList();
        setupSemesterFilter();
        filterData(currentSemesterIndex);
    }

    // --- HÀM XỬ LÝ MENU MỚI ---
    private void setupMenu() {
        // MenuHost thường là Activity chứa Fragment
        MenuHost menuHost = requireActivity();

        // Thêm MenuProvider để quản lý việc tạo và chọn item menu
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Thay thế cho onCreateOptionsMenu cũ
                menuInflater.inflate(R.menu.main_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Thay thế cho onOptionsItemSelected cũ
                int id = menuItem.getItemId();
                if (id == R.id.action_clear_all) {
                    // Xử lý xóa tất cả ở đây nếu cần
                    return true;
                } else if (id == R.id.action_predict) {
                    startActivity(new Intent(getActivity(), CalculateTargetActivity.class));
                    return true;
                } else if (id == R.id.action_calculate_final) {
                    startActivity(new Intent(getActivity(), CalculateFinalActivity.class));
                    return true;
                } else if (id == R.id.action_about) {
                    Toast.makeText(getContext(), "App Sổ Tay SV", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        // Lifecycle.State.RESUMED đảm bảo menu chỉ hiện khi Fragment hiển thị
    }

    @Override public void onResume() { super.onResume(); loadDataFromDB(); filterData(currentSemesterIndex); }

    private void loadDataFromDB() {
        if (databaseHelper != null) {
            listAllSubjects.clear();
            listAllSubjects.addAll(databaseHelper.getAllSubjects());
        }
    }

    private void setupSubjectList() {
        rvSubjects.setLayoutManager(new LinearLayoutManager(getContext()));

        subjectAdapter = new SubjectAdapter(listDisplaySubjects, new SubjectAdapter.SubjectClickListener() {
            @Override
            public void onSubjectClick(View view, Subject subject) {
                // Xử lý click item
            }

            @Override
            public void onSubjectLongClick(Subject subject) { }

            @Override
            public void onSelectionChanged(int count) {}
        });
        rvSubjects.setAdapter(subjectAdapter);
    }

    private void setupSemesterFilter() {
        List<String> listSemesters = new ArrayList<>();
        listSemesters.add("Tất cả");
        for (int i = 1; i <= 8; i++) listSemesters.add("Kỳ " + i);
        rvSemesterFilter.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        semesterAdapter = new SemesterAdapter(listSemesters, position -> {
            currentSemesterIndex = position;
            filterData(position);
        });
        rvSemesterFilter.setAdapter(semesterAdapter);
    }

    private void filterData(int semesterId) {
        listDisplaySubjects.clear();
        for (Subject sub : listAllSubjects) {
            boolean matchSemester = (semesterId == 0 || sub.getSemester() == semesterId);
            boolean isGraded = !sub.isStudying();

            if (matchSemester && isGraded) {
                listDisplaySubjects.add(sub);
            }
        }
        if (subjectAdapter != null) subjectAdapter.notifyDataSetChanged();
        updateSummary();
    }

    private void updateSummary() {
        double currentResult = GradeUtils.calculateGPA(listDisplaySubjects);
        tvGpa.setText(String.format("%.2f", currentResult));
        if (currentResult >= 3.6) { tvStatus.setText("Xuất sắc"); cardGpa.setCardBackgroundColor(Color.parseColor("#4CAF50")); }
        else if (currentResult >= 3.2) { tvStatus.setText("Giỏi"); cardGpa.setCardBackgroundColor(Color.parseColor("#4CAF50")); }
        else if (currentResult >= 2.5) { tvStatus.setText("Khá"); cardGpa.setCardBackgroundColor(Color.parseColor("#2196F3")); }
        else { tvStatus.setText("Trung bình/Yếu"); cardGpa.setCardBackgroundColor(Color.parseColor("#FF9800")); }
    }
}