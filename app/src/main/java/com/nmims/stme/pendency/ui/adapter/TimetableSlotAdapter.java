package com.nmims.stme.pendency.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nmims.stme.pendency.R;
import com.nmims.stme.pendency.data.entity.TimetableSlotEntity;

import java.util.ArrayList;
import java.util.List;

public class TimetableSlotAdapter extends RecyclerView.Adapter<TimetableSlotAdapter.ViewHolder> {

    private List<TimetableSlotEntity> slots = new ArrayList<>();

    public void setSlots(List<TimetableSlotEntity> slots) {
        this.slots = slots != null ? slots : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timetable_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimetableSlotEntity slot = slots.get(position);
        holder.tvDay.setText(slot.getDayOfWeek());
        holder.tvTime.setText(slot.getTimeDisplay() != null ? slot.getTimeDisplay() : slot.getStartTime() + " - " + slot.getEndTime());
        holder.tvSubject.setText(slot.getSubjectName());
        holder.tvFaculty.setText("Faculty: " + slot.getFacultyCode() + " • " + (slot.getBatch() != null ? slot.getBatch() : "All"));
        holder.tvRoom.setText(slot.getRoomLab());

        if ("RESCHEDULED".equalsIgnoreCase(slot.getSlotType())) {
            holder.tvTypeBadge.setText("Rescheduled");
            holder.tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_admin_approved);
            holder.tvTypeBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_admin_approved));
        } else {
            holder.tvTypeBadge.setText("Regular");
            holder.tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_faculty_approved);
            holder.tvTypeBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_navy));
        }
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvTime, tvSubject, tvFaculty, tvRoom, tvTypeBadge;

        ViewHolder(View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tv_slot_day);
            tvTime = itemView.findViewById(R.id.tv_slot_time);
            tvSubject = itemView.findViewById(R.id.tv_slot_subject);
            tvFaculty = itemView.findViewById(R.id.tv_slot_faculty);
            tvRoom = itemView.findViewById(R.id.tv_slot_room);
            tvTypeBadge = itemView.findViewById(R.id.tv_slot_type_badge);
        }
    }
}
