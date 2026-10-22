package com.nmims.stme.pendency.ui.cr;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.nmims.stme.pendency.R;
import com.nmims.stme.pendency.data.AppDatabase;
import com.nmims.stme.pendency.data.entity.ClassEntity;
import com.nmims.stme.pendency.data.entity.HolidayEntity;
import com.nmims.stme.pendency.data.entity.SubjectEntity;
import com.nmims.stme.pendency.ui.MainViewModel;
import com.nmims.stme.pendency.ui.adapter.PendencyRequestAdapter;
import com.nmims.stme.pendency.ui.adapter.SubjectProgressAdapter;

public class CrDashboardFragment extends Fragment {

    private MainViewModel viewModel;
    private TextView tvClassTitle, tvRoomTag, tvCrNames;
    private TextView tvStatTotalSubjects, tvStatPendingLectures, tvStatScheduledRescheduled;
    private RecyclerView rvSubjectProgress, rvCrRequests;
    private ExtendedFloatingActionButton fabNewRequest;
    private MaterialButton btnPreScheduleHoliday;

    private SubjectProgressAdapter subjectAdapter;
    private PendencyRequestAdapter requestAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cr_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        tvClassTitle = view.findViewById(R.id.tv_cr_class_title);
        tvRoomTag = view.findViewById(R.id.tv_cr_room_tag);
        tvCrNames = view.findViewById(R.id.tv_cr_names);
        tvStatTotalSubjects = view.findViewById(R.id.tv_stat_total_subjects);
        tvStatPendingLectures = view.findViewById(R.id.tv_stat_pending_lectures);
        tvStatScheduledRescheduled = view.findViewById(R.id.tv_stat_scheduled_rescheduled);
        rvSubjectProgress = view.findViewById(R.id.rv_subject_progress);
        rvCrRequests = view.findViewById(R.id.rv_cr_requests);
        fabNewRequest = view.findViewById(R.id.fab_new_request);
        btnPreScheduleHoliday = view.findViewById(R.id.btn_preschedule_holiday);

        rvSubjectProgress.setLayoutManager(new LinearLayoutManager(getContext()));
        subjectAdapter = new SubjectProgressAdapter();
        rvSubjectProgress.setAdapter(subjectAdapter);

        rvCrRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        requestAdapter = new PendencyRequestAdapter();
        requestAdapter.setRole(0); // CR role
        rvCrRequests.setAdapter(requestAdapter);

        fabNewRequest.setOnClickListener(v -> openRequestDialog(null));
        btnPreScheduleHoliday.setOnClickListener(v -> {
            HolidayEntity gandhiJayanti = new HolidayEntity("2026-10-02", "Mahatma Gandhi Jayanti", "Fri", "National Holiday", false);
            openRequestDialog(gandhiJayanti);
        });

        observeData();
    }

    private void observeData() {
        viewModel.getSelectedClassId().observe(getViewLifecycleOwner(), classId -> {
            loadClassDetails(classId);
            loadSubjectsAndRequests(classId);
        });
    }

    private void loadClassDetails(String classId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            ClassEntity cls = viewModel.getRepository().getDatabase().classDao().getClassById(classId);
            if (cls != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    tvClassTitle.setText(cls.getDisplayName());
                    tvRoomTag.setText(cls.getDefaultRoom());
                    tvCrNames.setText("Appointed CRs: " + cls.getCrNames());
                });
            }
        });
    }

    private void loadSubjectsAndRequests(String classId) {
        viewModel.getRepository().getSubjectsForClass(classId).observe(getViewLifecycleOwner(), subjects -> {
            if (subjects != null) {
                subjectAdapter.setSubjects(subjects);
                tvStatTotalSubjects.setText(String.valueOf(subjects.size()));

                int totalPending = 0;
                int totalScheduled = 0;
                for (SubjectEntity s : subjects) {
                    totalPending += s.getPendingLectures();
                    totalScheduled += s.getScheduledLectures();
                }
                tvStatPendingLectures.setText(String.valueOf(totalPending));
                tvStatScheduledRescheduled.setText(String.valueOf(totalScheduled));
            }
        });

        viewModel.getRepository().getRequestsForClass(classId).observe(getViewLifecycleOwner(), requests -> {
            if (requests != null) {
                requestAdapter.setRequests(requests);
            }
        });
    }

    private void openRequestDialog(HolidayEntity holiday) {
        String currentClass = viewModel.getSelectedClassId().getValue();
        if (currentClass == null) currentClass = "BTECH_CE_S1_A";
        CreateRequestDialog dialog = new CreateRequestDialog(requireContext(), viewModel, currentClass, holiday);
        dialog.show();
    }
}
