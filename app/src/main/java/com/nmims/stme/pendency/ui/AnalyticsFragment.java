package com.nmims.stme.pendency.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.nmims.stme.pendency.R;
import com.nmims.stme.pendency.data.entity.SubjectEntity;
import com.nmims.stme.pendency.ui.adapter.SubjectProgressAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnalyticsFragment extends Fragment {

    private MainViewModel viewModel;

    private TextView tvRate, tvHeld, tvRescheduled, tvPending, tvTarget;
    private LinearProgressIndicator progressHealth;
    private ChipGroup chipGroupDept;
    private RecyclerView rvSubjects;
    private MaterialButton btnExportReport;

    private SubjectProgressAdapter adapter;
    private List<SubjectEntity> allSubjectsList = new ArrayList<>();
    private String selectedDeptFilter = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        tvRate = view.findViewById(R.id.tv_analytics_rate);
        tvHeld = view.findViewById(R.id.tv_analytics_held);
        tvRescheduled = view.findViewById(R.id.tv_analytics_rescheduled);
        tvPending = view.findViewById(R.id.tv_analytics_pending);
        tvTarget = view.findViewById(R.id.tv_analytics_target);
        progressHealth = view.findViewById(R.id.progress_overall_health);
        chipGroupDept = view.findViewById(R.id.chip_group_dept);
        rvSubjects = view.findViewById(R.id.rv_analytics_subjects);
        btnExportReport = view.findViewById(R.id.btn_export_report);

        rvSubjects.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SubjectProgressAdapter();
        rvSubjects.setAdapter(adapter);

        setupDeptFilter();
        btnExportReport.setOnClickListener(v -> exportAuditReport());

        viewModel.getAllSubjects().observe(getViewLifecycleOwner(), subjects -> {
            if (subjects != null) {
                allSubjectsList = subjects;
                updateMetricsAndFilter();
            }
        });
    }

    private void setupDeptFilter() {
        chipGroupDept.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_dept_all) selectedDeptFilter = "ALL";
            else if (id == R.id.chip_dept_ce) selectedDeptFilter = "CE";
            else if (id == R.id.chip_dept_aids) selectedDeptFilter = "AIDS";
            else if (id == R.id.chip_dept_mbatech) selectedDeptFilter = "MBATECH";

            updateMetricsAndFilter();
        });
    }

    private void updateMetricsAndFilter() {
        int totalHeld = 0;
        int totalRescheduled = 0;
        int totalPending = 0;
        int totalTarget = 0;

        List<SubjectEntity> filtered = new ArrayList<>();
        for (SubjectEntity sub : allSubjectsList) {
            boolean matches = true;
            if ("CE".equals(selectedDeptFilter) && !sub.getClassId().contains("CE")) matches = false;
            else if ("AIDS".equals(selectedDeptFilter) && !sub.getClassId().contains("AIDS")) matches = false;
            else if ("MBATECH".equals(selectedDeptFilter) && !sub.getClassId().contains("MBATECH")) matches = false;

            if (matches) {
                filtered.add(sub);
                totalHeld += sub.getConductedLectures();
                totalRescheduled += sub.getScheduledLectures();
                totalPending += sub.getPendingLectures();
                totalTarget += sub.getTargetLectures();
            }
        }

        tvHeld.setText(String.valueOf(totalHeld));
        tvRescheduled.setText(String.valueOf(totalRescheduled));
        tvPending.setText(String.valueOf(totalPending));
        tvTarget.setText(String.valueOf(totalTarget));

        int completed = totalHeld + totalRescheduled;
        int pct = totalTarget > 0 ? (int) ((completed / (float) totalTarget) * 100) : 0;
        progressHealth.setProgress(Math.min(100, pct));
        tvRate.setText(pct + "% Quota Fulfilled");

        adapter.setSubjects(filtered);
    }

    private void exportAuditReport() {
        StringBuilder sb = new StringBuilder();
        String dateStr = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(new Date());

        sb.append("=========================================\n");
        sb.append("NMIMS INDORE - STME ACADEMIC AUDIT REPORT\n");
        sb.append("Academic Year 2026-27 | wef 31.08.2026\n");
        sb.append("Generated: ").append(dateStr).append("\n");
        sb.append("=========================================\n\n");

        sb.append("OVERALL CURRICULUM HEALTH:\n");
        sb.append("• Target Lectures Quota: ").append(tvTarget.getText()).append("\n");
        sb.append("• Conducted Lectures: ").append(tvHeld.getText()).append("\n");
        sb.append("• Rescheduled & Locked: ").append(tvRescheduled.getText()).append("\n");
        sb.append("• Accumulated Pendencies: ").append(tvPending.getText()).append("\n");
        sb.append("• Quota Pace: ").append(tvRate.getText()).append("\n\n");

        sb.append("ATTENTION REQUIRED (HIGH PENDENCY COURSES):\n");
        int count = 0;
        for (SubjectEntity sub : allSubjectsList) {
            if (sub.getPendingLectures() >= 2) {
                count++;
                sb.append(count).append(". [").append(sub.getClassId()).append("] ")
                        .append(sub.getName()).append(" (Faculty: ").append(sub.getFacultyCode())
                        .append(" | Credits: ").append(sub.getCredits()).append(") -> ")
                        .append(sub.getPendingLectures()).append(" Pending / ").append(sub.getTargetLectures()).append(" Target\n");
            }
        }

        if (count == 0) {
            sb.append("All courses are currently on pace!\n");
        }

        sb.append("\nSubmitted to: Time Table Coordinators (Dr. Nidhhi Asthana, Dr. Ankur Ratmele, Dr. Vikas Jain)\n");
        sb.append("Associate Dean: Dr. Vikas Khare | Director: Dr. Anshuman Jaswal\n");

        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("STME Academic Audit", sb.toString());
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "✓ Academic Audit Report copied to clipboard!", Toast.LENGTH_LONG).show();
        }
    }
}
