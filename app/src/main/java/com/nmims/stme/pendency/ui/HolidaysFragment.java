package com.nmims.stme.pendency.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nmims.stme.pendency.R;
import com.nmims.stme.pendency.data.entity.HolidayEntity;
import com.nmims.stme.pendency.ui.adapter.HolidayAdapter;
import com.nmims.stme.pendency.ui.cr.CreateRequestDialog;

public class HolidaysFragment extends Fragment {

    private MainViewModel viewModel;
    private RecyclerView rvHolidays;
    private HolidayAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_holidays_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        rvHolidays = view.findViewById(R.id.rv_holidays);
        rvHolidays.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HolidayAdapter();
        adapter.setOnHolidayActionListener(holiday -> {
            String currentClass = viewModel.getSelectedClassId().getValue();
            if (currentClass == null) currentClass = "BTECH_CE_S1_A";
            CreateRequestDialog dialog = new CreateRequestDialog(requireContext(), viewModel, currentClass, holiday);
            dialog.show();
        });
        rvHolidays.setAdapter(adapter);

        viewModel.getAllHolidays().observe(getViewLifecycleOwner(), holidays -> {
            if (holidays != null) {
                adapter.setHolidays(holidays);
            }
        });
    }
}
