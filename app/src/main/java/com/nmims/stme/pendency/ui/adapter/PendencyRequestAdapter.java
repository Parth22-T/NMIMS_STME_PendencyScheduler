package com.nmims.stme.pendency.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.nmims.stme.pendency.R;
import com.nmims.stme.pendency.data.entity.PendencyRequestEntity;

import java.util.ArrayList;
import java.util.List;

public class PendencyRequestAdapter extends RecyclerView.Adapter<PendencyRequestAdapter.ViewHolder> {

    public interface OnRequestActionListener {
        void onApprove(PendencyRequestEntity request);
        void onReject(PendencyRequestEntity request);
    }

    private List<PendencyRequestEntity> requests = new ArrayList<>();
    private int currentRole = 0; // 0=CR, 1=Faculty, 2=Admin
    private OnRequestActionListener actionListener;

    public void setRole(int role) {
        this.currentRole = role;
        notifyDataSetChanged();
    }

    public void setActionListener(OnRequestActionListener listener) {
        this.actionListener = listener;
    }

    public void setRequests(List<PendencyRequestEntity> requests) {
        this.requests = requests != null ? requests : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pendency_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PendencyRequestEntity req = requests.get(position);

        holder.tvClassName.setText(req.getClassName() != null ? req.getClassName() : req.getClassId());
        holder.tvSubject.setText(req.getSubjectName());
        holder.tvFaculty.setText("Faculty: " + req.getFacultyName() + " (" + req.getFacultyCode() + ")");
        holder.tvTypeTag.setText(req.getSchedulingType() != null && req.getSchedulingType().contains("PRE") ? "PRE-SCHEDULED" : "COMPENSATORY POST");
        holder.tvReason.setText("Reason: " + req.getReason());

        String orig = (req.getOriginalDate() != null ? req.getOriginalDate() : "") + " • " + (req.getOriginalTimeSlot() != null ? req.getOriginalTimeSlot() : "Slot");
        holder.tvOriginalSlot.setText(orig);

        String prop = (req.getProposedDate() != null ? req.getProposedDate() : "") + " • " + (req.getProposedTimeSlot() != null ? req.getProposedTimeSlot() : "Slot") + " in " + (req.getProposedRoom() != null ? req.getProposedRoom() : "TBD");
        holder.tvProposedSlot.setText(prop);

        // Status badge configuration
        String status = req.getStatus() != null ? req.getStatus() : "SUBMITTED_TO_FACULTY";
        switch (status) {
            case "SUBMITTED_TO_FACULTY":
                holder.tvStatusBadge.setText("Pending Faculty");
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending);
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_pending));
                break;
            case "FACULTY_APPROVED":
                holder.tvStatusBadge.setText("Faculty Approved • Pending Admin");
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_faculty_approved);
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_faculty_approved));
                break;
            case "ADMIN_APPROVED":
                holder.tvStatusBadge.setText("Admin Approved • Scheduled");
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_admin_approved);
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_admin_approved));
                break;
            case "FACULTY_REJECTED":
                holder.tvStatusBadge.setText("Rejected by Faculty");
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_rejected);
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_rejected));
                break;
            case "ADMIN_REJECTED":
                holder.tvStatusBadge.setText("Rejected by Admin");
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_rejected);
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_rejected));
                break;
            default:
                holder.tvStatusBadge.setText(status);
                break;
        }

        // Remarks
        StringBuilder remarks = new StringBuilder();
        if (req.getFacultyRemarks() != null && !req.getFacultyRemarks().isEmpty()) {
            remarks.append("Faculty Remarks: ").append(req.getFacultyRemarks()).append("\n");
        }
        if (req.getAdminRemarks() != null && !req.getAdminRemarks().isEmpty()) {
            remarks.append("Admin Remarks: ").append(req.getAdminRemarks());
        }
        if (remarks.length() > 0) {
            holder.tvRemarks.setVisibility(View.VISIBLE);
            holder.tvRemarks.setText(remarks.toString().trim());
        } else {
            holder.tvRemarks.setVisibility(View.GONE);
        }

        // Action Buttons logic based on Role and Status
        if (currentRole == 1 && "SUBMITTED_TO_FACULTY".equals(status)) {
            // Faculty can approve or reject
            holder.layoutActions.setVisibility(View.VISIBLE);
            holder.btnApprove.setText("Approve (Forward to Admin)");
            holder.btnApprove.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onApprove(req);
            });
            holder.btnReject.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onReject(req);
            });
        } else if (currentRole == 2 && "FACULTY_APPROVED".equals(status)) {
            // Admin can give final approval or reject
            holder.layoutActions.setVisibility(View.VISIBLE);
            holder.btnApprove.setText("Grant Final Admin Approval");
            holder.btnApprove.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onApprove(req);
            });
            holder.btnReject.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onReject(req);
            });
        } else {
            holder.layoutActions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassName, tvStatusBadge, tvSubject, tvFaculty, tvTypeTag, tvReason;
        TextView tvOriginalSlot, tvProposedSlot, tvRemarks;
        LinearLayout layoutActions;
        MaterialButton btnApprove, btnReject;

        ViewHolder(View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tv_req_class_name);
            tvStatusBadge = itemView.findViewById(R.id.tv_req_status_badge);
            tvSubject = itemView.findViewById(R.id.tv_req_subject);
            tvFaculty = itemView.findViewById(R.id.tv_req_faculty);
            tvTypeTag = itemView.findViewById(R.id.tv_req_type_tag);
            tvReason = itemView.findViewById(R.id.tv_req_reason);
            tvOriginalSlot = itemView.findViewById(R.id.tv_req_original_slot);
            tvProposedSlot = itemView.findViewById(R.id.tv_req_proposed_slot);
            tvRemarks = itemView.findViewById(R.id.tv_req_remarks);
            layoutActions = itemView.findViewById(R.id.layout_req_actions);
            btnApprove = itemView.findViewById(R.id.btn_req_approve);
            btnReject = itemView.findViewById(R.id.btn_req_reject);
        }
    }
}
