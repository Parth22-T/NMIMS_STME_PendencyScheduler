package com.nmims.stme.pendency.ui.faculty;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.nmims.stme.pendency.R;
import com.nmims.stme.pendency.data.AppDatabase;
import com.nmims.stme.pendency.data.entity.FacultyEntity;
import com.nmims.stme.pendency.data.entity.FacultyLeaveEntity;
import com.nmims.stme.pendency.data.entity.PendencyRequestEntity;
import com.nmims.stme.pendency.data.entity.TimetableSlotEntity;
import com.nmims.stme.pendency.ui.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class IntimateLeaveDialog extends Dialog {

    private final MainViewModel viewModel;
    private final String facultyCode;

    private TextView tvFacultyInfo, tvLeaveDateDisplay, tvAffectedSummary;
    private View layoutPickLeaveDate;
    private Spinner spinnerLeaveReason;
    private MaterialButton btnSubmit, btnCancel;

    private final Calendar leaveCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private List<TimetableSlotEntity> affectedSlots = new ArrayList<>();
    private FacultyEntity facultyEntity;

    private static final String[] LEAVE_REASONS = {
            "Faculty Academic Duty / Conference",
            "Medical / Health Leave",
            "Institutional Examination Duty",
            "Personal / Urgent Leave",
            "Official STME University Work"
    };

    public IntimateLeaveDialog(@NonNull Context context, MainViewModel viewModel, String facultyCode) {
        super(context);
        this.viewModel = viewModel;
        this.facultyCode = facultyCode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_intimate_leave);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        initViews();
        setupSpinner();
        setupDatePicker();
        loadFacultyInfo();
    }

    private void initViews() {
        tvFacultyInfo = findViewById(R.id.tv_leave_faculty_info);
        tvLeaveDateDisplay = findViewById(R.id.tv_leave_date_display);
        tvAffectedSummary = findViewById(R.id.tv_affected_summary);
        layoutPickLeaveDate = findViewById(R.id.layout_pick_leave_date);
        spinnerLeaveReason = findViewById(R.id.spinner_leave_reason);
        btnSubmit = findViewById(R.id.btn_leave_submit);
        btnCancel = findViewById(R.id.btn_leave_cancel);

        leaveCalendar.add(Calendar.DAY_OF_YEAR, 2); // default 2 days ahead
        tvLeaveDateDisplay.setText(dateFormat.format(leaveCalendar.getTime()));

        btnCancel.setOnClickListener(v -> dismiss());
        btnSubmit.setOnClickListener(v -> submitLeave());
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, LEAVE_REASONS);
        spinnerLeaveReason.setAdapter(adapter);
    }

    private void setupDatePicker() {
        layoutPickLeaveDate.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                leaveCalendar.set(year, month, dayOfMonth);
                tvLeaveDateDisplay.setText(dateFormat.format(leaveCalendar.getTime()));
                calculateAffectedSlots();
            }, leaveCalendar.get(Calendar.YEAR), leaveCalendar.get(Calendar.MONTH), leaveCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void loadFacultyInfo() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            facultyEntity = viewModel.getRepository().getDatabase().facultyDao().getFacultyByCode(facultyCode);
            tvFacultyInfo.post(() -> {
                if (facultyEntity != null) {
                    tvFacultyInfo.setText("For " + facultyEntity.getName() + " (" + facultyCode + ")");
                }
                calculateAffectedSlots();
            });
        });
    }

    private void calculateAffectedSlots() {
        String dateStr = tvLeaveDateDisplay.getText().toString().trim();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            String dayOfWeek = "Mon";
            try {
                java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
                String day = date.getDayOfWeek().toString().substring(0, 3);
                dayOfWeek = day.substring(0, 1).toUpperCase() + day.substring(1).toLowerCase();
                if (dayOfWeek.equals("Tue")) dayOfWeek = "Tues";
                if (dayOfWeek.equals("Thu")) dayOfWeek = "Thurs";
            } catch (Exception ignored) {}

            List<TimetableSlotEntity> allFacultySlots = viewModel.getRepository().getDatabase().timetableDao().getSlotsForFacultySync(facultyCode);
            affectedSlots.clear();
            StringBuilder sb = new StringBuilder();
            int count = 0;

            for (TimetableSlotEntity s : allFacultySlots) {
                if (dayOfWeek.equalsIgnoreCase(s.getDayOfWeek())) {
                    affectedSlots.add(s);
                    count++;
                    sb.append("• ").append(s.getClassId()).append(": ").append(s.getSubjectName())
                            .append(" (").append(s.getTimeDisplay()).append(" in ").append(s.getRoomLab()).append(")\n");
                }
            }

            final int finalCount = count;
            final String finalDay = dayOfWeek;
            tvAffectedSummary.post(() -> {
                if (finalCount > 0) {
                    tvAffectedSummary.setText("⚠️ " + finalCount + " lectures affected on " + finalDay + " (" + dateStr + "):\n\n" + sb.toString().trim()
                            + "\n\nCRs of these classes will be alerted to schedule compensatory lectures.");
                } else {
                    tvAffectedSummary.setText("✓ No regular lectures scheduled for you on " + finalDay + " (" + dateStr + ").");
                }
            });
        });
    }

    private void submitLeave() {
        String dateStr = tvLeaveDateDisplay.getText().toString().trim();
        if (dateStr.isEmpty()) {
            Toast.makeText(getContext(), "Error: Please specify the absence date", Toast.LENGTH_SHORT).show();
            return;
        }

        String reason = spinnerLeaveReason.getSelectedItem().toString();
        String facName = facultyEntity != null ? facultyEntity.getName() : facultyCode;

        FacultyLeaveEntity leave = new FacultyLeaveEntity(
                UUID.randomUUID().toString(),
                facultyCode,
                facName,
                dateStr,
                reason,
                affectedSlots.size(),
                System.currentTimeMillis()
        );

        List<PendencyRequestEntity> notices = new ArrayList<>();
        for (TimetableSlotEntity slot : affectedSlots) {
            PendencyRequestEntity notice = new PendencyRequestEntity(
                    slot.getClassId(),
                    slot.getClassId(),
                    slot.getSubjectName(),
                    facultyCode,
                    facName,
                    "Faculty Absence: " + reason,
                    "COMPENSATORY_POST",
                    dateStr,
                    slot.getTimeDisplay(),
                    "",
                    "",
                    slot.getRoomLab(),
                    "SUBMITTED_TO_FACULTY",
                    "Faculty (" + facultyCode + ") intimated planned leave. CR please propose reschedule slot.",
                    "Faculty pre-approved cancellation due to " + reason,
                    "",
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    0
            );
            notices.add(notice);
        }

        viewModel.intimateFacultyLeave(leave, notices, () -> {
            tvFacultyInfo.post(() -> {
                Toast.makeText(getContext(), "✓ Absence intimated! " + notices.size() + " class pendency notices synced to CRs across devices.", Toast.LENGTH_LONG).show();
                dismiss();
            });
        });
    }
}
