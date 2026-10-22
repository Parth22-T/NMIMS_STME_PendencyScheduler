package com.nmims.stme.pendency.ui.timetable;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.nmims.stme.pendency.R;
import com.nmims.stme.pendency.data.entity.ClassEntity;
import com.nmims.stme.pendency.data.entity.TimetableSlotEntity;
import com.nmims.stme.pendency.ui.MainViewModel;
import com.nmims.stme.pendency.ui.adapter.TimetableSlotAdapter;

import java.util.ArrayList;
import java.util.List;

public class TimetableViewFragment extends Fragment {

    private MainViewModel viewModel;
    private TextView tvHeader, tvSub, tvEmpty;
    private LinearLayout layoutClassSelector;
    private Spinner spinnerClass;
    private ChipGroup chipGroupDays;
    private RecyclerView rvSlots;
    private TimetableSlotAdapter adapter;

    private List<TimetableSlotEntity> allSlots = new ArrayList<>();
    private List<ClassEntity> allClasses = new ArrayList<>();
    private String selectedDayFilter = "ALL";
    private String viewedClassId = "BTECH_CE_S1_A";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timetable_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        tvHeader = view.findViewById(R.id.tv_timetable_header);
        tvSub = view.findViewById(R.id.tv_timetable_sub);
        tvEmpty = view.findViewById(R.id.tv_empty_timetable);
        layoutClassSelector = view.findViewById(R.id.layout_timetable_class_selector);
        spinnerClass = view.findViewById(R.id.spinner_timetable_class);
        chipGroupDays = view.findViewById(R.id.chip_group_days);
        rvSlots = view.findViewById(R.id.rv_timetable_slots);

        rvSlots.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TimetableSlotAdapter();
        rvSlots.setAdapter(adapter);

        setupDayChips();
        observeData();
    }

    private void setupDayChips() {
        chipGroupDays.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_all_days) selectedDayFilter = "ALL";
            else if (id == R.id.chip_mon) selectedDayFilter = "Mon";
            else if (id == R.id.chip_tues) selectedDayFilter = "Tues";
            else if (id == R.id.chip_wed) selectedDayFilter = "Wed";
            else if (id == R.id.chip_thurs) selectedDayFilter = "Thurs";
            else if (id == R.id.chip_fri) selectedDayFilter = "Fri";
            else if (id == R.id.chip_sat) selectedDayFilter = "Sat";

            filterSlots();
        });
    }

    private void observeData() {
        viewModel.getAllClasses().observe(getViewLifecycleOwner(), classes -> {
            if (classes != null && !classes.isEmpty()) {
                allClasses = classes;
                setupClassSpinner();
            }
        });

        viewModel.getCurrentRole().observe(getViewLifecycleOwner(), role -> {
            if (role == 1) {
                // Faculty view
                layoutClassSelector.setVisibility(View.GONE);
                tvHeader.setText("Faculty Teaching Schedule");
                tvSub.setText("All assigned sections and lab commitments across STME");
                observeFacultySlots();
            } else {
                // CR or Admin view
                layoutClassSelector.setVisibility(View.VISIBLE);
                tvHeader.setText("Official Academic Timetable (AY 2026-27)");
                tvSub.setText("Showing regular and approved rescheduled slots");
                observeClassSlots();
            }
        });
    }

    private void setupClassSpinner() {
        List<String> names = new ArrayList<>();
        int selectedIndex = 0;
        String curClassId = viewModel.getSelectedClassId().getValue();

        for (int i = 0; i < allClasses.size(); i++) {
            ClassEntity c = allClasses.get(i);
            names.add(c.getDisplayName() + " (" + c.getDefaultRoom() + ")");
            if (c.getClassId().equals(curClassId)) {
                selectedIndex = i;
            }
        }

        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, names);
        spinnerClass.setAdapter(spinAdapter);
        if (selectedIndex < names.size()) {
            spinnerClass.setSelection(selectedIndex);
        }

        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < allClasses.size()) {
                    viewedClassId = allClasses.get(position).getClassId();
                    loadSlotsForClass(viewedClassId);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void observeClassSlots() {
        String active = viewModel.getSelectedClassId().getValue();
        if (active != null) viewedClassId = active;
        loadSlotsForClass(viewedClassId);
    }

    private void loadSlotsForClass(String cid) {
        viewModel.getRepository().getSlotsForClass(cid).observe(getViewLifecycleOwner(), slots -> {
            if (slots != null) {
                allSlots = slots;
                filterSlots();
            }
        });
    }

    private void observeFacultySlots() {
        viewModel.getSelectedFacultyCode().observe(getViewLifecycleOwner(), fCode -> {
            viewModel.getRepository().getSlotsForFaculty(fCode).observe(getViewLifecycleOwner(), slots -> {
                if (slots != null) {
                    allSlots = slots;
                    filterSlots();
                }
            });
        });
    }

    private void filterSlots() {
        if ("ALL".equalsIgnoreCase(selectedDayFilter)) {
            adapter.setSlots(allSlots);
            tvEmpty.setVisibility(allSlots.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            List<TimetableSlotEntity> filtered = new ArrayList<>();
            for (TimetableSlotEntity s : allSlots) {
                if (selectedDayFilter.equalsIgnoreCase(s.getDayOfWeek())) {
                    filtered.add(s);
                }
            }
            adapter.setSlots(filtered);
            tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}
