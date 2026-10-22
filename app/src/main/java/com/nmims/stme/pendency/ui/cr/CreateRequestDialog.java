package com.nmims.stme.pendency.ui.cr;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.nmims.stme.pendency.R;
import com.nmims.stme.pendency.data.AppDatabase;
import com.nmims.stme.pendency.data.entity.ClassEntity;
import com.nmims.stme.pendency.data.entity.HolidayEntity;
import com.nmims.stme.pendency.data.entity.PendencyRequestEntity;
import com.nmims.stme.pendency.data.entity.SubjectEntity;
import com.nmims.stme.pendency.ui.MainViewModel;
import com.nmims.stme.pendency.util.ConflictChecker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateRequestDialog extends Dialog {

    private final MainViewModel viewModel;
    private final String classId;
    private final HolidayEntity preselectedHoliday;

    private TextView tvClassIndicator, tvAssignedFaculty, tvConflictResult;
    private TextView tvOriginalDateDisplay, tvProposedDateDisplay;
    private View layoutPickOriginalDate, layoutPickProposedDate;
    private RadioGroup rgScheduleType;
    private RadioButton rbPreSchedule, rbPostSchedule;
    private Spinner spinnerSubject, spinnerReason, spinnerOriginalSlot, spinnerProposedSlot, spinnerProposedRoom, spinnerSubmittingCr;
    private EditText etCrNotes;
    private MaterialButton btnValidateConflict, btnAutoSuggestSlot, btnSubmit, btnCancel;

    private List<SubjectEntity> classSubjects = new ArrayList<>();
    private SubjectEntity selectedSubject;
    private ClassEntity currentClassEntity;

    private final Calendar originalCalendar = Calendar.getInstance();
    private final Calendar proposedCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static final String[] TIME_SLOTS = {
            "9:00 am to 10:00 am",
            "10:00 am to 11:00 am",
            "11:50 am to 12:50 pm",
            "12:50 pm to 1:50 pm",
            "2:10 pm to 3:10 pm",
            "3:10 pm to 4:10 pm",
            "4:10 pm to 5:10 pm"
    };

    private static final String[] ROOMS = {
            "CR 302", "CR 303", "CR 304", "CR 306", "CR 307", "CR 308",
            "CR 309", "CR 310", "CR 311", "CR 312", "CR 313", "CR 314",
            "CR 315", "CR 316", "CR 317", "CR 413", "CR 301",
            "CC 201", "CC 202", "CC 401", "CC 402", "CL 201", "CL 202",
            "CL 203", "CL 204", "CL 205", "CL 206", "EI lab", "BEE lab"
    };

    private static final String[] REASONS = {
            "National Holiday / Gazetted Break",
            "Faculty Academic Leave / Duty",
            "Faculty Medical / Personal Absence",
            "NMIMS Institutional Fest / Event",
            "Mid-Term Exam / Quiz Preparation",
            "Compensatory Syllabus Coverage"
    };

    public CreateRequestDialog(@NonNull Context context, MainViewModel viewModel, String classId, HolidayEntity holiday) {
        super(context);
        this.viewModel = viewModel;
        this.classId = classId;
        this.preselectedHoliday = holiday;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_create_request);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        initViews();
        setupSpinners();
        setupDatePickers();
        loadClassData();
    }

    private void initViews() {
        tvClassIndicator = findViewById(R.id.tv_dialog_class_indicator);
        tvAssignedFaculty = findViewById(R.id.tv_dialog_assigned_faculty);
        tvConflictResult = findViewById(R.id.tv_conflict_result);
        tvOriginalDateDisplay = findViewById(R.id.tv_original_date_display);
        tvProposedDateDisplay = findViewById(R.id.tv_proposed_date_display);
        layoutPickOriginalDate = findViewById(R.id.layout_pick_original_date);
        layoutPickProposedDate = findViewById(R.id.layout_pick_proposed_date);

        rgScheduleType = findViewById(R.id.rg_schedule_type);
        rbPreSchedule = findViewById(R.id.rb_preschedule);
        rbPostSchedule = findViewById(R.id.rb_postschedule);
        spinnerSubject = findViewById(R.id.spinner_dialog_subject);
        spinnerReason = findViewById(R.id.spinner_dialog_reason);
        spinnerOriginalSlot = findViewById(R.id.spinner_original_slot);
        spinnerProposedSlot = findViewById(R.id.spinner_proposed_slot);
        spinnerProposedRoom = findViewById(R.id.spinner_proposed_room);
        spinnerSubmittingCr = findViewById(R.id.spinner_submitting_cr);
        etCrNotes = findViewById(R.id.et_cr_notes);
        btnValidateConflict = findViewById(R.id.btn_validate_conflict);
        btnAutoSuggestSlot = findViewById(R.id.btn_auto_suggest_slot);
        btnSubmit = findViewById(R.id.btn_dialog_submit);
        btnCancel = findViewById(R.id.btn_dialog_cancel);

        tvClassIndicator.setText("Class ID: " + classId);

        // Proposed date defaults to tomorrow
        proposedCalendar.add(Calendar.DAY_OF_YEAR, 1);
        tvProposedDateDisplay.setText(dateFormat.format(proposedCalendar.getTime()));

        if (preselectedHoliday != null) {
            rbPreSchedule.setChecked(true);
            tvOriginalDateDisplay.setText(preselectedHoliday.getDateStr());
            etCrNotes.setText("Pre-scheduling ahead of " + preselectedHoliday.getHolidayName() + " (" + preselectedHoliday.getDateStr() + ")");
        } else {
            tvOriginalDateDisplay.setText(dateFormat.format(originalCalendar.getTime()));
        }

        btnCancel.setOnClickListener(v -> dismiss());
        btnValidateConflict.setOnClickListener(v -> checkConflicts());
        btnAutoSuggestSlot.setOnClickListener(v -> autoSuggestSlot());
        btnSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    private void setupDatePickers() {
        layoutPickOriginalDate.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                originalCalendar.set(year, month, dayOfMonth);
                tvOriginalDateDisplay.setText(dateFormat.format(originalCalendar.getTime()));
            }, originalCalendar.get(Calendar.YEAR), originalCalendar.get(Calendar.MONTH), originalCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        layoutPickProposedDate.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                proposedCalendar.set(year, month, dayOfMonth);
                tvProposedDateDisplay.setText(dateFormat.format(proposedCalendar.getTime()));
                // Auto validate after date pick
                checkConflicts();
            }, proposedCalendar.get(Calendar.YEAR), proposedCalendar.get(Calendar.MONTH), proposedCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupSpinners() {
        ArrayAdapter<String> reasonAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, REASONS);
        spinnerReason.setAdapter(reasonAdapter);

        ArrayAdapter<String> slotAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, TIME_SLOTS);
        spinnerOriginalSlot.setAdapter(slotAdapter);
        spinnerProposedSlot.setAdapter(slotAdapter);

        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, ROOMS);
        spinnerProposedRoom.setAdapter(roomAdapter);
    }

    private void loadClassData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            classSubjects = viewModel.getRepository().getDatabase().subjectDao().getSubjectsForClassSync(classId);
            currentClassEntity = viewModel.getRepository().getDatabase().classDao().getClassById(classId);

            List<String> subjectNames = new ArrayList<>();
            for (SubjectEntity sub : classSubjects) {
                subjectNames.add(sub.getName() + " (" + sub.getFacultyCode() + " • " + sub.getCredits() + " Credits)");
            }

            List<String> crList = new ArrayList<>();
            if (currentClassEntity != null && currentClassEntity.getCrNames() != null) {
                String[] crs = currentClassEntity.getCrNames().split("&");
                for (String c : crs) {
                    crList.add("CR: " + c.trim());
                }
            } else {
                crList.add("Class Representative 1");
                crList.add("Class Representative 2");
            }

            findViewById(R.id.tv_dialog_class_indicator).post(() -> {
                if (currentClassEntity != null) {
                    tvClassIndicator.setText("For " + currentClassEntity.getDisplayName() + " (Room " + currentClassEntity.getDefaultRoom() + ")");
                }

                ArrayAdapter<String> crAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, crList);
                spinnerSubmittingCr.setAdapter(crAdapter);

                ArrayAdapter<String> subAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, subjectNames);
                spinnerSubject.setAdapter(subAdapter);
                spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position < classSubjects.size()) {
                            selectedSubject = classSubjects.get(position);
                            tvAssignedFaculty.setText("Assigned Faculty: " + selectedSubject.getFacultyCode()
                                    + " | Target Lectures: " + selectedSubject.getTargetLectures()
                                    + " | Held: " + selectedSubject.getConductedLectures()
                                    + " | Pending: " + selectedSubject.getPendingLectures());
                            // Pre-select default room
                            if (currentClassEntity != null) {
                                for (int i = 0; i < ROOMS.length; i++) {
                                    if (ROOMS[i].equalsIgnoreCase(currentClassEntity.getDefaultRoom())) {
                                        spinnerProposedRoom.setSelection(i);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            });
        });
    }

    private void autoSuggestSlot() {
        if (selectedSubject == null) {
            Toast.makeText(getContext(), "Please select a subject first", Toast.LENGTH_SHORT).show();
            return;
        }

        String proposedDateStr = tvProposedDateDisplay.getText().toString().trim();
        String defaultRoom = currentClassEntity != null ? currentClassEntity.getDefaultRoom() : "CR 302";

        AppDatabase.databaseWriteExecutor.execute(() -> {
            String dayOfWeek = getDayOfWeekFromDate(proposedDateStr);

            List<ConflictChecker.SlotRecommendation> recommendations = ConflictChecker.findFreeSlots(
                    viewModel.getRepository().getDatabase(),
                    classId,
                    selectedSubject.getFacultyCode(),
                    dayOfWeek,
                    defaultRoom,
                    proposedDateStr
            );

            tvConflictResult.post(() -> {
                if (!recommendations.isEmpty()) {
                    ConflictChecker.SlotRecommendation best = recommendations.get(0);

                    // Select the recommended slot in spinner
                    for (int i = 0; i < TIME_SLOTS.length; i++) {
                        if (TIME_SLOTS[i].equalsIgnoreCase(best.timeSlot)) {
                            spinnerProposedSlot.setSelection(i);
                            break;
                        }
                    }

                    // Select the recommended room in spinner
                    for (int i = 0; i < ROOMS.length; i++) {
                        if (ROOMS[i].equalsIgnoreCase(best.room)) {
                            spinnerProposedRoom.setSelection(i);
                            break;
                        }
                    }

                    tvConflictResult.setBackgroundResource(R.drawable.bg_badge_admin_approved);
                    tvConflictResult.setTextColor(ContextCompat.getColor(getContext(), R.color.status_admin_approved));
                    tvConflictResult.setText("✓ Auto-Selected Best Free Slot:\n" + best.timeSlot + " in " + best.room + " (No Class/Faculty/Room/Holiday Clash)");
                    Toast.makeText(getContext(), "Free slot and room found and set!", Toast.LENGTH_SHORT).show();
                } else {
                    tvConflictResult.setBackgroundResource(R.drawable.bg_badge_pending);
                    tvConflictResult.setTextColor(ContextCompat.getColor(getContext(), R.color.status_pending));
                    tvConflictResult.setText("No fully vacant periods found on " + dayOfWeek + " (" + proposedDateStr + "). Try another date or Saturday.");
                }
            });
        });
    }

    private void checkConflicts() {
        if (selectedSubject == null) {
            Toast.makeText(getContext(), "Please select a subject first", Toast.LENGTH_SHORT).show();
            return;
        }

        String proposedDateStr = tvProposedDateDisplay.getText().toString().trim();
        String proposedSlot = spinnerProposedSlot.getSelectedItem().toString();
        String proposedRoom = spinnerProposedRoom.getSelectedItem().toString();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            String dayOfWeek = getDayOfWeekFromDate(proposedDateStr);

            ConflictChecker.ConflictResult result = ConflictChecker.validateSlot(
                    viewModel.getRepository().getDatabase(),
                    classId,
                    selectedSubject.getFacultyCode(),
                    proposedRoom,
                    dayOfWeek,
                    proposedSlot,
                    proposedDateStr
            );

            tvConflictResult.post(() -> {
                if (result.hasConflict) {
                    tvConflictResult.setBackgroundResource(R.drawable.bg_badge_rejected);
                    tvConflictResult.setTextColor(ContextCompat.getColor(getContext(), R.color.status_rejected));
                    tvConflictResult.setText("⚠️ " + result.conflictReason);
                } else {
                    tvConflictResult.setBackgroundResource(R.drawable.bg_badge_admin_approved);
                    tvConflictResult.setTextColor(ContextCompat.getColor(getContext(), R.color.status_admin_approved));
                    tvConflictResult.setText("✓ " + result.conflictReason);
                }
            });
        });
    }

    private String getDayOfWeekFromDate(String dateStr) {
        String dayOfWeek = "Mon";
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            String day = date.getDayOfWeek().toString().substring(0, 3);
            dayOfWeek = day.substring(0, 1).toUpperCase() + day.substring(1).toLowerCase();
            if (dayOfWeek.equals("Tue")) dayOfWeek = "Tues";
            if (dayOfWeek.equals("Thu")) dayOfWeek = "Thurs";
        } catch (Exception ignored) {}
        return dayOfWeek;
    }

    private void validateAndSubmit() {
        if (selectedSubject == null) {
            Toast.makeText(getContext(), "Error: Please select a subject from the list", Toast.LENGTH_SHORT).show();
            return;
        }

        String propDate = tvProposedDateDisplay.getText().toString().trim();
        if (propDate.isEmpty()) {
            Toast.makeText(getContext(), "Error: Please specify the proposed reschedule date", Toast.LENGTH_SHORT).show();
            return;
        }

        String proposedSlot = spinnerProposedSlot.getSelectedItem().toString();
        String proposedRoom = spinnerProposedRoom.getSelectedItem().toString();

        // Check conflicts asynchronously before submitting
        AppDatabase.databaseWriteExecutor.execute(() -> {
            String dayOfWeek = getDayOfWeekFromDate(propDate);
            ConflictChecker.ConflictResult conflict = ConflictChecker.validateSlot(
                    viewModel.getRepository().getDatabase(),
                    classId,
                    selectedSubject.getFacultyCode(),
                    proposedRoom,
                    dayOfWeek,
                    proposedSlot,
                    propDate
            );

            findViewById(R.id.tv_dialog_class_indicator).post(() -> {
                if (conflict.hasConflict) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Scheduling Conflict Detected")
                            .setMessage(conflict.conflictReason + "\n\nDo you want to auto-suggest a free slot instead?")
                            .setPositiveButton("Auto-Suggest Free Slot", (dialog, which) -> autoSuggestSlot())
                            .setNegativeButton("Modify Details", null)
                            .show();
                    return;
                }

                // Check remaining quota warning
                if (selectedSubject.getRemainingLectures() == 0) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Curriculum Quota Notice")
                            .setMessage("Notice: The prescribed quota of " + selectedSubject.getTargetLectures()
                                    + " lectures for '" + selectedSubject.getName()
                                    + "' has already been satisfied (Conducted: " + selectedSubject.getConductedLectures()
                                    + ", Scheduled: " + selectedSubject.getScheduledLectures() + ").\n\n"
                                    + "Do you still wish to submit an additional compensatory lecture request?")
                            .setPositiveButton("Yes, Request Extra Slot", (dialog, which) -> executeSubmission())
                            .setNegativeButton("Cancel", null)
                            .show();
                } else {
                    executeSubmission();
                }
            });
        });
    }

    private void executeSubmission() {
        String schedType = rbPreSchedule.isChecked() ? "PRE_SCHEDULED" : "COMPENSATORY_POST";
        String reason = spinnerReason.getSelectedItem().toString();
        String origDate = tvOriginalDateDisplay.getText().toString().trim();
        String origSlot = spinnerOriginalSlot.getSelectedItem().toString();
        String propDate = tvProposedDateDisplay.getText().toString().trim();
        String propSlot = spinnerProposedSlot.getSelectedItem().toString();
        String propRoom = spinnerProposedRoom.getSelectedItem().toString();
        String submittingCr = spinnerSubmittingCr.getSelectedItem() != null ? spinnerSubmittingCr.getSelectedItem().toString() : "CR";
        String notes = etCrNotes.getText().toString().trim();
        if (!submittingCr.isEmpty()) {
            notes = "[" + submittingCr + "] " + notes;
        }

        PendencyRequestEntity request = new PendencyRequestEntity(
                classId,
                tvClassIndicator.getText().toString(),
                selectedSubject.getName(),
                selectedSubject.getFacultyCode(),
                "Faculty " + selectedSubject.getFacultyCode(),
                reason,
                schedType,
                origDate,
                origSlot,
                propDate,
                propSlot,
                propRoom,
                "SUBMITTED_TO_FACULTY",
                notes,
                "",
                "",
                System.currentTimeMillis(),
                0,
                0
        );

        viewModel.submitRequest(request, () -> {
            findViewById(R.id.tv_dialog_class_indicator).post(() -> {
                Toast.makeText(getContext(), "✓ Reschedule Request Sent to Faculty (" + selectedSubject.getFacultyCode() + ") & Synced across devices!", Toast.LENGTH_LONG).show();
                dismiss();
            });
        });
    }
}
