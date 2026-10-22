package com.nmims.stme.pendency.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.nmims.stme.pendency.R;
import com.nmims.stme.pendency.data.entity.SubjectEntity;

import java.util.ArrayList;
import java.util.List;

public class SubjectProgressAdapter extends RecyclerView.Adapter<SubjectProgressAdapter.ViewHolder> {

    private List<SubjectEntity> subjects = new ArrayList<>();

    public void setSubjects(List<SubjectEntity> subjects) {
        this.subjects = subjects != null ? subjects : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubjectEntity sub = subjects.get(position);
        holder.tvName.setText(sub.getName());
        holder.tvCredits.setText(sub.getCredits() + " Credits (" + sub.getTargetLectures() + " Target Lecs)");
        holder.tvFaculty.setText("Assigned Faculty: " + sub.getFacultyCode());

        int conducted = sub.getConductedLectures();
        int scheduled = sub.getScheduledLectures();
        int pending = sub.getPendingLectures();
        int target = sub.getTargetLectures();

        int progressPercent = target > 0 ? (int) (((conducted + scheduled) / (float) target) * 100) : 0;
        holder.progressBar.setProgress(Math.min(100, progressPercent));

        holder.tvConducted.setText("Conducted: " + conducted);
        holder.tvScheduled.setText("Rescheduled: " + scheduled);
        holder.tvPending.setText("Pending: " + pending);
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCredits, tvFaculty, tvConducted, tvScheduled, tvPending;
        LinearProgressIndicator progressBar;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_subject_name);
            tvCredits = itemView.findViewById(R.id.tv_subject_credits);
            tvFaculty = itemView.findViewById(R.id.tv_subject_faculty);
            tvConducted = itemView.findViewById(R.id.tv_lectures_conducted);
            tvScheduled = itemView.findViewById(R.id.tv_lectures_scheduled);
            tvPending = itemView.findViewById(R.id.tv_lectures_pending);
            progressBar = itemView.findViewById(R.id.progress_lectures);
        }
    }
}
