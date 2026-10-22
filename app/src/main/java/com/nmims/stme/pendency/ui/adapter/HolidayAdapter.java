package com.nmims.stme.pendency.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.nmims.stme.pendency.R;
import com.nmims.stme.pendency.data.entity.HolidayEntity;

import java.util.ArrayList;
import java.util.List;

public class HolidayAdapter extends RecyclerView.Adapter<HolidayAdapter.ViewHolder> {

    public interface OnHolidayActionListener {
        void onPreSchedule(HolidayEntity holiday);
    }

    private List<HolidayEntity> holidays = new ArrayList<>();
    private OnHolidayActionListener listener;

    public void setOnHolidayActionListener(OnHolidayActionListener listener) {
        this.listener = listener;
    }

    public void setHolidays(List<HolidayEntity> holidays) {
        this.holidays = holidays != null ? holidays : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_holiday, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HolidayEntity hol = holidays.get(position);
        holder.tvDay.setText(hol.getDayOfWeek());
        holder.tvDate.setText(hol.getDateStr());
        holder.tvTitle.setText(hol.getHolidayName());
        holder.tvCategory.setText(hol.getType());

        holder.btnPreSchedule.setOnClickListener(v -> {
            if (listener != null) listener.onPreSchedule(hol);
        });
    }

    @Override
    public int getItemCount() {
        return holidays.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvDate, tvTitle, tvCategory;
        MaterialButton btnPreSchedule;

        ViewHolder(View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tv_holiday_day);
            tvDate = itemView.findViewById(R.id.tv_holiday_date);
            tvTitle = itemView.findViewById(R.id.tv_holiday_title);
            tvCategory = itemView.findViewById(R.id.tv_holiday_category);
            btnPreSchedule = itemView.findViewById(R.id.btn_preschedule_this);
        }
    }
}
