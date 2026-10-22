package com.nmims.stme.pendency.ui.faculty;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.nmims.stme.pendency.R;
import com.nmims.stme.pendency.data.AppDatabase;
import com.nmims.stme.pendency.data.entity.FacultyEntity;
import com.nmims.stme.pendency.data.entity.PendencyRequestEntity;
import com.nmims.stme.pendency.ui.MainViewModel;
import com.nmims.stme.pendency.ui.adapter.PendencyRequestAdapter;
import com.nmims.stme.pendency.ui.adapter.TimetableSlotAdapter;

public class FacultyDashboardFragment extends Fragment {

    private MainViewModel viewModel;
    private TextView tvFacultyName, tvFacultyCodeBadge, tvFacultyDesignation, tvFacultyEmail;
    private TextView tvFacultyPendingCount, tvEmptyRequests;
    private RecyclerView rvFacultyRequests, rvFacultySchedule;
    private MaterialButton btnIntimateLeave;

    private PendencyRequestAdapter requestAdapter;
    private TimetableSlotAdapter scheduleAdapter;
    private String currentFacultyCode = "NA";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_faculty_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        tvFacultyName = view.findViewById(R.id.tv_faculty_name);
        tvFacultyCodeBadge = view.findViewById(R.id.tv_faculty_code_badge);
        tvFacultyDesignation = view.findViewById(R.id.tv_faculty_designation);
        tvFacultyEmail = view.findViewById(R.id.tv_faculty_email);
        tvFacultyPendingCount = view.findViewById(R.id.tv_faculty_pending_count);
        tvEmptyRequests = view.findViewById(R.id.tv_empty_faculty_requests);
        rvFacultyRequests = view.findViewById(R.id.rv_faculty_requests);
        rvFacultySchedule = view.findViewById(R.id.rv_faculty_schedule);
        btnIntimateLeave = view.findViewById(R.id.btn_intimate_leave);

        rvFacultyRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        requestAdapter = new PendencyRequestAdapter();
        requestAdapter.setRole(1); // Faculty role
        requestAdapter.setActionListener(new PendencyRequestAdapter.OnRequestActionListener() {
            @Override
            public void onApprove(PendencyRequestEntity request) {
                promptApprove(request);
            }

            @Override
            public void onReject(PendencyRequestEntity request) {
                promptReject(request);
            }
        });
        rvFacultyRequests.setAdapter(requestAdapter);

        rvFacultySchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleAdapter = new TimetableSlotAdapter();
        rvFacultySchedule.setAdapter(scheduleAdapter);

        btnIntimateLeave.setOnClickListener(v -> {
            IntimateLeaveDialog dialog = new IntimateLeaveDialog(requireContext(), viewModel, currentFacultyCode);
            dialog.show();
        });

        observeData();
    }

    private void observeData() {
        viewModel.getSelectedFacultyCode().observe(getViewLifecycleOwner(), facultyCode -> {
            if (facultyCode != null) {
                currentFacultyCode = facultyCode;
                loadFacultyDetails(facultyCode);
                loadRequestsAndSchedule(facultyCode);
            }
        });
    }

    private void loadFacultyDetails(String facultyCode) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            FacultyEntity faculty = viewModel.getRepository().getDatabase().facultyDao().getFacultyByCode(facultyCode);
            if (faculty != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    tvFacultyName.setText(faculty.getName());
                    tvFacultyCodeBadge.setText("CODE: " + faculty.getFacultyCode());
                    tvFacultyDesignation.setText(faculty.getDesignation() + " • " + faculty.getDepartment());
                    tvFacultyEmail.setText(faculty.getEmail());
                });
            }
        });
    }

    private void loadRequestsAndSchedule(String facultyCode) {
        viewModel.getRepository().getRequestsForFaculty(facultyCode).observe(getViewLifecycleOwner(), requests -> {
            if (requests != null) {
                requestAdapter.setRequests(requests);
                int pending = 0;
                for (PendencyRequestEntity r : requests) {
                    if ("SUBMITTED_TO_FACULTY".equals(r.getStatus())) pending++;
                }
                tvFacultyPendingCount.setText(pending + " Awaiting You");
                tvEmptyRequests.setVisibility(requests.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getRepository().getSlotsForFaculty(facultyCode).observe(getViewLifecycleOwner(), slots -> {
            if (slots != null) {
                scheduleAdapter.setSlots(slots);
            }
        });
    }

    private void promptApprove(PendencyRequestEntity req) {
        final EditText input = new EditText(getContext());
        input.setHint("Enter approval remarks (e.g. Verified slot, ready for admin clearance)");
        input.setPadding(30, 20, 30, 20);

        new AlertDialog.Builder(requireContext())
                .setTitle("Approve Reschedule Request")
                .setMessage("Approve " + req.getSubjectName() + " on " + req.getProposedDate() + " (" + req.getProposedTimeSlot() + ") and forward to Admin?")
                .setView(input)
                .setPositiveButton("Approve & Forward", (dialog, which) -> {
                    String remarks = input.getText().toString().trim();
                    if (remarks.isEmpty()) remarks = "Approved by Faculty " + req.getFacultyCode() + ". Slot verified.";
                    viewModel.facultyApprove(req.getRequestId(), remarks, () -> {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Approved and forwarded to Administration Department!", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void promptReject(PendencyRequestEntity req) {
        final EditText input = new EditText(getContext());
        input.setHint("Enter rejection reason (e.g. Schedule clash with departmental meeting)");
        input.setPadding(30, 20, 30, 20);

        new AlertDialog.Builder(requireContext())
                .setTitle("Reject Reschedule Request")
                .setMessage("State the reason for rejecting this proposed slot:")
                .setView(input)
                .setPositiveButton("Reject Request", (dialog, which) -> {
                    String remarks = input.getText().toString().trim();
                    if (remarks.isEmpty()) remarks = "Slot rejected due to conflict.";
                    viewModel.facultyReject(req.getRequestId(), remarks, () -> {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Request rejected.", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
