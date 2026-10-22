package com.nmims.stme.pendency.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nmims.stme.pendency.R;
import com.nmims.stme.pendency.data.AppDatabase;
import com.nmims.stme.pendency.data.entity.PendencyRequestEntity;
import com.nmims.stme.pendency.ui.MainViewModel;
import com.nmims.stme.pendency.ui.adapter.PendencyRequestAdapter;
import com.nmims.stme.pendency.util.ConflictChecker;

import java.util.List;

public class AdminDashboardFragment extends Fragment {

    private MainViewModel viewModel;
    private TextView tvAdminStatPending, tvAdminStatApproved, tvAdminStatTotal;
    private TextView tvAdminQueueBadge, tvEmptyAdminQueue;
    private RecyclerView rvAdminQueue, rvAdminAllRequests;

    private PendencyRequestAdapter queueAdapter;
    private PendencyRequestAdapter allRequestsAdapter;

    private static final String[] ROOMS = {
            "CR 302", "CR 303", "CR 304", "CR 306", "CR 307", "CR 308",
            "CR 309", "CR 310", "CR 311", "CR 312", "CR 313", "CR 314",
            "CR 315", "CR 316", "CR 317", "CR 413", "CR 301",
            "CC 201", "CC 202", "CC 401", "CC 402", "CL 201", "CL 202",
            "CL 203", "CL 204", "CL 205", "CL 206", "EI lab", "BEE lab"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        tvAdminStatPending = view.findViewById(R.id.tv_admin_stat_pending);
        tvAdminStatApproved = view.findViewById(R.id.tv_admin_stat_approved);
        tvAdminStatTotal = view.findViewById(R.id.tv_admin_stat_total);
        tvAdminQueueBadge = view.findViewById(R.id.tv_admin_queue_badge);
        tvEmptyAdminQueue = view.findViewById(R.id.tv_empty_admin_queue);
        rvAdminQueue = view.findViewById(R.id.rv_admin_queue);
        rvAdminAllRequests = view.findViewById(R.id.rv_admin_all_requests);

        rvAdminQueue.setLayoutManager(new LinearLayoutManager(getContext()));
        queueAdapter = new PendencyRequestAdapter();
        queueAdapter.setRole(2); // Admin role
        queueAdapter.setActionListener(new PendencyRequestAdapter.OnRequestActionListener() {
            @Override
            public void onApprove(PendencyRequestEntity request) {
                verifyAndPromptAdminApprove(request);
            }

            @Override
            public void onReject(PendencyRequestEntity request) {
                promptAdminReject(request);
            }
        });
        rvAdminQueue.setAdapter(queueAdapter);

        rvAdminAllRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        allRequestsAdapter = new PendencyRequestAdapter();
        allRequestsAdapter.setRole(0); // View only for log
        rvAdminAllRequests.setAdapter(allRequestsAdapter);

        observeData();
    }

    private void observeData() {
        viewModel.getRequestsPendingAdminApproval().observe(getViewLifecycleOwner(), pendingList -> {
            if (pendingList != null) {
                queueAdapter.setRequests(pendingList);
                tvAdminStatPending.setText(String.valueOf(pendingList.size()));
                tvAdminQueueBadge.setText(pendingList.size() + " Awaiting Clearance");
                tvEmptyAdminQueue.setVisibility(pendingList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getAllRequests().observe(getViewLifecycleOwner(), allList -> {
            if (allList != null) {
                allRequestsAdapter.setRequests(allList);
                int approved = 0;
                for (PendencyRequestEntity r : allList) {
                    if ("ADMIN_APPROVED".equals(r.getStatus())) approved++;
                }
                tvAdminStatApproved.setText(String.valueOf(approved));
            }
        });
    }

    private void verifyAndPromptAdminApprove(PendencyRequestEntity req) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            String dayOfWeek = "Mon";
            try {
                java.time.LocalDate localDate = java.time.LocalDate.parse(req.getProposedDate());
                String day = localDate.getDayOfWeek().toString().substring(0, 3);
                dayOfWeek = day.substring(0, 1).toUpperCase() + day.substring(1).toLowerCase();
                if (dayOfWeek.equals("Tue")) dayOfWeek = "Tues";
                if (dayOfWeek.equals("Thu")) dayOfWeek = "Thurs";
            } catch (Exception ignored) {}

            ConflictChecker.ConflictResult conflict = ConflictChecker.validateSlot(
                    viewModel.getRepository().getDatabase(),
                    req.getClassId(),
                    req.getFacultyCode(),
                    req.getProposedRoom(),
                    dayOfWeek,
                    req.getProposedTimeSlot(),
                    req.getProposedDate()
            );

            final String finalDay = dayOfWeek;
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (conflict.hasConflict) {
                        // Present conflict override / room change options
                        showAdminConflictResolutionDialog(req, conflict.conflictReason, finalDay);
                    } else {
                        showStandardAdminApproveDialog(req);
                    }
                });
            }
        });
    }

    private void showStandardAdminApproveDialog(PendencyRequestEntity req) {
        final EditText input = new EditText(getContext());
        input.setHint("Admin clearance remark (e.g. Room locked & roster updated)");
        input.setPadding(30, 20, 30, 20);

        new AlertDialog.Builder(requireContext())
                .setTitle("Grant Final Admin Approval")
                .setMessage("Authorize rescheduling of '" + req.getSubjectName() + "' on "
                        + req.getProposedDate() + " (" + req.getProposedTimeSlot() + ") in "
                        + req.getProposedRoom() + "?\n\n✓ Slot and Room verified: No active conflicts.\n\nThis will officially lock the timetable across all devices and advance lecture count.")
                .setView(input)
                .setPositiveButton("Authorize & Schedule", (dialog, which) -> {
                    String remarks = input.getText().toString().trim();
                    if (remarks.isEmpty()) remarks = "Authorized by STME Academic Office. Timetable locked.";
                    executeAdminApproval(req, remarks);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAdminConflictResolutionDialog(PendencyRequestEntity req, String conflictReason, String dayOfWeek) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        TextView tvReason = new TextView(getContext());
        tvReason.setText("⚠️ " + conflictReason + "\n\nAs Administrator, you can assign an alternate vacant room or reject the request:");
        tvReason.setTextColor(getResources().getColor(R.color.status_rejected, null));
        tvReason.setTextSize(13);
        layout.addView(tvReason);

        TextView tvLabel = new TextView(getContext());
        tvLabel.setText("\nReassign Room / Lab:");
        tvLabel.setTextSize(12);
        layout.addView(tvLabel);

        Spinner spinnerRooms = new Spinner(getContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, ROOMS);
        spinnerRooms.setAdapter(adapter);
        layout.addView(spinnerRooms);

        EditText etRemark = new EditText(getContext());
        etRemark.setHint("Administrative remarks");
        layout.addView(etRemark);

        new AlertDialog.Builder(requireContext())
                .setTitle("Scheduling Conflict - Admin Override")
                .setView(layout)
                .setPositiveButton("Reassign Room & Authorize", (dialog, which) -> {
                    String selectedRoom = spinnerRooms.getSelectedItem().toString();
                    req.setProposedRoom(selectedRoom);
                    String remark = etRemark.getText().toString().trim();
                    if (remark.isEmpty()) remark = "Room reassigned to " + selectedRoom + " by STME Admin to resolve conflict.";
                    executeAdminApproval(req, remark);
                })
                .setNegativeButton("Reject Request", (dialog, which) -> promptAdminReject(req))
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void executeAdminApproval(PendencyRequestEntity req, String remarks) {
        viewModel.adminApprove(req.getRequestId(), remarks, () -> {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "✓ Rescheduled lecture officially booked and synced to all devices!", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void promptAdminReject(PendencyRequestEntity req) {
        final EditText input = new EditText(getContext());
        input.setHint("Administrative rejection rationale");
        input.setPadding(30, 20, 30, 20);

        new AlertDialog.Builder(requireContext())
                .setTitle("Reject Request")
                .setMessage("State reasons for administrative rejection:")
                .setView(input)
                .setPositiveButton("Reject", (dialog, which) -> {
                    String remarks = input.getText().toString().trim();
                    if (remarks.isEmpty()) remarks = "Rejected due to institutional exam/room conflict.";
                    viewModel.adminReject(req.getRequestId(), remarks, () -> {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Request rejected by Admin.", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
