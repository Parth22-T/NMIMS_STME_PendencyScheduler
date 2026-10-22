package com.nmims.stme.pendency.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.nmims.stme.pendency.R;
import com.nmims.stme.pendency.auth.SessionManager;
import com.nmims.stme.pendency.data.AppDatabase;
import com.nmims.stme.pendency.data.entity.UserEntity;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextView tvError;
    private ChipGroup chipGroupRole;
    private Chip chipRoleAdmin, chipRoleFaculty, chipRoleCr;
    private MaterialButton btnLogin;

    private SessionManager sessionManager;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = SessionManager.getInstance(this);
        db = AppDatabase.getDatabase(this);

        // If already logged in, proceed straight to Main Activity
        if (sessionManager.isLoggedIn() && sessionManager.getCurrentUser() != null) {
            launchMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        tvError = findViewById(R.id.tv_login_error);
        chipGroupRole = findViewById(R.id.chip_group_login_role);
        chipRoleAdmin = findViewById(R.id.chip_role_admin);
        chipRoleFaculty = findViewById(R.id.chip_role_faculty);
        chipRoleCr = findViewById(R.id.chip_role_cr);
        btnLogin = findViewById(R.id.btn_login);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());

        // Quick presets
        findViewById(R.id.btn_quick_admin).setOnClickListener(v -> {
            chipRoleAdmin.setChecked(true);
            etEmail.setText("admin.stme@nmims.edu");
            etPassword.setText("Admin@STME2026");
            tvError.setVisibility(View.GONE);
        });

        findViewById(R.id.btn_quick_faculty_vk).setOnClickListener(v -> {
            chipRoleFaculty.setChecked(true);
            etEmail.setText("vikas.khare@nmims.edu");
            etPassword.setText("Faculty@VK2026");
            tvError.setVisibility(View.GONE);
        });

        findViewById(R.id.btn_quick_cr_ce).setOnClickListener(v -> {
            chipRoleCr.setChecked(true);
            etEmail.setText("aarav.sharma@nmims.edu");
            etPassword.setText("CR@CES1A");
            tvError.setVisibility(View.GONE);
        });

        findViewById(R.id.btn_quick_cr_aids).setOnClickListener(v -> {
            chipRoleCr.setChecked(true);
            etEmail.setText("kabir.singh@nmims.edu");
            etPassword.setText("CR@AIDSS1A");
            tvError.setVisibility(View.GONE);
        });

        findViewById(R.id.btn_show_credentials_sheet).setOnClickListener(v -> showCredentialsReferenceSheet());

        chipGroupRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_role_admin) {
                etEmail.setText("admin.stme@nmims.edu");
                etPassword.setText("Admin@STME2026");
            } else if (checkedId == R.id.chip_role_faculty) {
                etEmail.setText("vikas.khare@nmims.edu");
                etPassword.setText("Faculty@VK2026");
            } else if (checkedId == R.id.chip_role_cr) {
                etEmail.setText("aarav.sharma@nmims.edu");
                etPassword.setText("CR@CES1A");
            }
            tvError.setVisibility(View.GONE);
        });
    }

    private void performLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            tvError.setText("Please enter your official NMIMS email.");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tvError.setText("Please enter your password.");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        tvError.setVisibility(View.GONE);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserEntity user = db.userDao().authenticate(email, password);
            runOnUiThread(() -> {
                if (user != null) {
                    sessionManager.loginUser(user);
                    Toast.makeText(this, "✓ Welcome, " + user.getDisplayName() + "!", Toast.LENGTH_SHORT).show();
                    launchMainActivity();
                } else {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        UserEntity emailCheck = db.userDao().getUserByEmail(email);
                        runOnUiThread(() -> {
                            if (emailCheck != null) {
                                tvError.setText("⚠️ Incorrect password for " + emailCheck.getDisplayName() + " (" + emailCheck.getRoleName() + ").");
                            } else {
                                tvError.setText("⚠️ Account not found. Use one of the 1-Tap Demo Logins or view the reference sheet below.");
                            }
                            tvError.setVisibility(View.VISIBLE);
                        });
                    });
                }
            });
        });
    }

    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showCredentialsReferenceSheet() {
        String message = "🏛️ ADMINISTRATION DEPARTMENT:\n"
                + "• Email: admin.stme@nmims.edu | Pass: Admin@STME2026\n"
                + "• Email: attendance.stme@nmims.edu | Pass: Attendance@2026\n"
                + "• Email: director.stme@nmims.edu | Pass: Director@2026\n\n"
                + "👨‍🏫 FACULTY MEMBERS:\n"
                + "• Dr. Vikas Khare (VK): vikas.khare@nmims.edu | Pass: Faculty@VK2026\n"
                + "• Dr. Nidhhi Asthana (NA): nidhhi.asthana@nmims.edu | Pass: Faculty@NA2026\n"
                + "• Mr. Lokesh Gupta (MJ): lokesh.gupta@nmims.edu | Pass: Faculty@MJ2026\n"
                + "• Dr. Vishal Jain (VJ): vikas.jain@nmims.edu | Pass: Faculty@VJ2026\n"
                + "• Dr. Ankur Ratmele (AR): ankur.ratmele@nmims.edu | Pass: Faculty@AR2026\n"
                + "*(All 33 faculty codes supported with Faculty@<CODE>2026)*\n\n"
                + "🎓 CLASS REPRESENTATIVES (2 Per Class):\n"
                + "• B.Tech CE S1 A: aarav.sharma@nmims.edu | Pass: CR@CES1A\n"
                + "• B.Tech CE S1 A: diya.patel@nmims.edu | Pass: CR@CES1A\n"
                + "• B.Tech AIDS S1 A: kabir.singh@nmims.edu | Pass: CR@AIDSS1A\n"
                + "• B.Tech CE S5 A: yash.malhotra@nmims.edu | Pass: CR@CES5A\n"
                + "• MBA.Tech CE S1: siddharth.jain@nmims.edu | Pass: CR@CES12026\n"
                + "*(All 18 class sections supported with CR@<CLASS>2026)*";

        new AlertDialog.Builder(this)
                .setTitle("Official Account Credentials Reference")
                .setMessage(message)
                .setPositiveButton("Close", null)
                .show();
    }
}
