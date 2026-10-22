package com.nmims.stme.pendency.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.tabs.TabLayout;
import com.nmims.stme.pendency.R;
import com.nmims.stme.pendency.data.AppDatabase;
import com.nmims.stme.pendency.data.entity.ClassEntity;
import android.app.AlertDialog;
import android.content.Intent;
import com.nmims.stme.pendency.auth.SessionManager;
import com.nmims.stme.pendency.data.entity.FacultyEntity;
import com.nmims.stme.pendency.data.entity.UserEntity;
import com.nmims.stme.pendency.sync.NetworkSyncManager;
import com.nmims.stme.pendency.ui.admin.AdminDashboardFragment;
import com.nmims.stme.pendency.ui.cr.CrDashboardFragment;
import com.nmims.stme.pendency.ui.faculty.FacultyDashboardFragment;
import com.nmims.stme.pendency.ui.timetable.TimetableViewFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private SessionManager sessionManager;
    private UserEntity currentUser;

    private TextView tvActiveRoleBadge, tvSelectorLabel, tvUserSessionInfo;
    private View btnSyncStatus, viewSyncDot, btnLogout;
    private TextView tvSyncStatusLabel;
    private MaterialButtonToggleGroup roleToggleGroup;
    private LinearLayout layoutContextSelector;
    private Spinner spinnerContext;
    private TabLayout tabLayout;

    private List<ClassEntity> classList = new ArrayList<>();
    private List<FacultyEntity> facultyList = new ArrayList<>();
    private int activeRoleIndex = 0; // 0=CR, 1=Faculty, 2=Admin
    private int activeTabIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = SessionManager.getInstance(this);
        if (!sessionManager.isLoggedIn() || sessionManager.getCurrentUser() == null) {
            launchLogin();
            return;
        }
        currentUser = sessionManager.getCurrentUser();

        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Pre-warm database initialization
        AppDatabase.getDatabase(this);

        initViews();
        setupTabs();
        setupRoleToggle();
        observeMetadata();
        observeSyncState();

        // Apply initial user role session
        applyUserSessionRole();

        // Ensure default fragment is rendered immediately
        displayFragmentForCurrentTab();
    }

    private void launchLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        tvActiveRoleBadge = findViewById(R.id.tv_active_role_badge);
        tvSelectorLabel = findViewById(R.id.tv_selector_label);
        tvUserSessionInfo = findViewById(R.id.tv_user_session_info);
        btnLogout = findViewById(R.id.btn_logout);
        btnSyncStatus = findViewById(R.id.btn_sync_status);
        viewSyncDot = findViewById(R.id.view_sync_indicator_dot);
        tvSyncStatusLabel = findViewById(R.id.tv_sync_status_label);
        roleToggleGroup = findViewById(R.id.role_toggle_group);
        layoutContextSelector = findViewById(R.id.layout_context_selector);
        spinnerContext = findViewById(R.id.spinner_context);
        tabLayout = findViewById(R.id.tab_layout);

        if (currentUser != null && tvUserSessionInfo != null) {
            tvUserSessionInfo.setText("👤 " + currentUser.getDisplayName() + " (" + currentUser.getRoleName() + ")");
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> confirmLogout());
        }

        btnSyncStatus.setOnClickListener(v -> {
            new ServerSettingsDialog(MainActivity.this, viewModel.getRepository().getSyncManager()).show();
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out of NMIMS Academic Scheduler?")
                .setPositiveButton("Sign Out", (dialog, which) -> {
                    sessionManager.logout();
                    launchLogin();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void applyUserSessionRole() {
        if (currentUser == null) return;

        if (currentUser.getRole() == 2) {
            // Admin role
            roleToggleGroup.check(R.id.btn_role_admin);
        } else if (currentUser.getRole() == 1) {
            // Faculty role
            roleToggleGroup.check(R.id.btn_role_faculty);
            if (currentUser.getAssociatedFacultyCode() != null) {
                viewModel.setSelectedFacultyCode(currentUser.getAssociatedFacultyCode());
            }
        } else {
            // CR role
            roleToggleGroup.check(R.id.btn_role_cr);
            if (currentUser.getAssociatedClassId() != null) {
                viewModel.setSelectedClassId(currentUser.getAssociatedClassId());
            }
        }
    }

    private void observeSyncState() {
        viewModel.getSyncState().observe(this, syncState -> {
            if (syncState == NetworkSyncManager.SyncState.CONNECTED) {
                viewSyncDot.setBackgroundResource(R.drawable.bg_dot_online);
                tvSyncStatusLabel.setText("LINKED");
                btnSyncStatus.setBackgroundResource(R.drawable.bg_badge_admin_approved);
            } else if (syncState == NetworkSyncManager.SyncState.CONNECTING) {
                viewSyncDot.setBackgroundResource(R.drawable.bg_dot_offline);
                tvSyncStatusLabel.setText("SYNCING");
                btnSyncStatus.setBackgroundResource(R.drawable.bg_badge_pending);
            } else {
                viewSyncDot.setBackgroundResource(R.drawable.bg_dot_offline);
                tvSyncStatusLabel.setText("OFFLINE");
                btnSyncStatus.setBackgroundResource(R.drawable.bg_badge_pending);
            }
        });
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Dashboard"));
        tabLayout.addTab(tabLayout.newTab().setText("Timetable"));
        tabLayout.addTab(tabLayout.newTab().setText("Holiday Calendar"));
        tabLayout.addTab(tabLayout.newTab().setText("Analytics"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                activeTabIndex = tab.getPosition();
                displayFragmentForCurrentTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRoleToggle() {
        roleToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.btn_role_cr) {
                activeRoleIndex = 0;
                tvActiveRoleBadge.setText("ROLE: CLASS REP (CR)");
                tvActiveRoleBadge.setBackgroundResource(R.drawable.bg_badge_faculty_approved);
                updateSelectorForCr();
            } else if (checkedId == R.id.btn_role_faculty) {
                if (currentUser != null && currentUser.getRole() == 0) {
                    android.widget.Toast.makeText(this, "Access restricted: Faculty authorization required.", android.widget.Toast.LENGTH_SHORT).show();
                    roleToggleGroup.check(R.id.btn_role_cr);
                    return;
                }
                activeRoleIndex = 1;
                tvActiveRoleBadge.setText("ROLE: FACULTY");
                tvActiveRoleBadge.setBackgroundResource(R.drawable.bg_badge_holiday);
                updateSelectorForFaculty();
            } else if (checkedId == R.id.btn_role_admin) {
                if (currentUser != null && currentUser.getRole() < 2) {
                    android.widget.Toast.makeText(this, "Access restricted: STME Admin clearance required.", android.widget.Toast.LENGTH_SHORT).show();
                    if (currentUser.getRole() == 1) roleToggleGroup.check(R.id.btn_role_faculty);
                    else roleToggleGroup.check(R.id.btn_role_cr);
                    return;
                }
                activeRoleIndex = 2;
                tvActiveRoleBadge.setText("ROLE: ADMIN DEPT");
                tvActiveRoleBadge.setBackgroundResource(R.drawable.bg_badge_admin_approved);
                updateSelectorForAdmin();
            }
            viewModel.setCurrentRole(activeRoleIndex);
            displayFragmentForCurrentTab();
        });
    }

    private void observeMetadata() {
        viewModel.getAllClasses().observe(this, classes -> {
            if (classes != null && !classes.isEmpty()) {
                classList = classes;
                if (activeRoleIndex == 0) updateSelectorForCr();
            }
        });

        viewModel.getAllFaculties().observe(this, faculties -> {
            if (faculties != null && !faculties.isEmpty()) {
                facultyList = faculties;
                if (activeRoleIndex == 1) updateSelectorForFaculty();
            }
        });
    }

    private void updateSelectorForCr() {
        layoutContextSelector.setVisibility(View.VISIBLE);
        tvSelectorLabel.setText("Active Class:");

        List<String> displayNames = new ArrayList<>();
        int selectedIndex = 0;
        String curClassId = viewModel.getSelectedClassId().getValue();

        for (int i = 0; i < classList.size(); i++) {
            ClassEntity c = classList.get(i);
            displayNames.add(c.getDisplayName() + " (" + c.getDefaultRoom() + ")");
            if (c.getClassId().equals(curClassId)) {
                selectedIndex = i;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, displayNames);
        spinnerContext.setAdapter(adapter);
        if (selectedIndex < displayNames.size()) {
            spinnerContext.setSelection(selectedIndex);
        }

        spinnerContext.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < classList.size()) {
                    viewModel.setSelectedClassId(classList.get(position).getClassId());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateSelectorForFaculty() {
        layoutContextSelector.setVisibility(View.VISIBLE);
        tvSelectorLabel.setText("Active Faculty:");

        List<String> facultyNames = new ArrayList<>();
        int selectedIndex = 0;
        String curCode = viewModel.getSelectedFacultyCode().getValue();

        for (int i = 0; i < facultyList.size(); i++) {
            FacultyEntity f = facultyList.get(i);
            facultyNames.add(f.getName() + " (" + f.getFacultyCode() + ")");
            if (f.getFacultyCode().equals(curCode)) {
                selectedIndex = i;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, facultyNames);
        spinnerContext.setAdapter(adapter);
        if (selectedIndex < facultyNames.size()) {
            spinnerContext.setSelection(selectedIndex);
        }

        spinnerContext.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < facultyList.size()) {
                    viewModel.setSelectedFacultyCode(facultyList.get(position).getFacultyCode());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateSelectorForAdmin() {
        layoutContextSelector.setVisibility(View.GONE);
    }

    private void displayFragmentForCurrentTab() {
        Fragment fragment;
        if (activeTabIndex == 0) {
            // Dashboard depends on active role
            if (activeRoleIndex == 0) {
                fragment = new CrDashboardFragment();
            } else if (activeRoleIndex == 1) {
                fragment = new FacultyDashboardFragment();
            } else {
                fragment = new AdminDashboardFragment();
            }
        } else if (activeTabIndex == 1) {
            fragment = new TimetableViewFragment();
        } else if (activeTabIndex == 2) {
            fragment = new HolidaysFragment();
        } else {
            fragment = new AnalyticsFragment();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }
}
