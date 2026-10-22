package com.nmims.stme.pendency.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nmims.stme.pendency.R;
import com.nmims.stme.pendency.sync.NetworkSyncManager;

public class ServerSettingsDialog extends Dialog {

    private final NetworkSyncManager syncManager;
    private TextInputEditText etServerUrl;
    private TextView tvStatusSummary, tvPingResult;
    private MaterialButton btnPresetEmulator, btnPresetLocalhost;
    private MaterialButton btnTestPing, btnSyncNow, btnSaveUrl, btnClose;

    public ServerSettingsDialog(@NonNull Context context, NetworkSyncManager syncManager) {
        super(context);
        this.syncManager = syncManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_server_settings);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        etServerUrl = findViewById(R.id.et_server_url);
        tvStatusSummary = findViewById(R.id.tv_dialog_sync_status_summary);
        tvPingResult = findViewById(R.id.tv_ping_result);
        btnPresetEmulator = findViewById(R.id.btn_preset_emulator);
        btnPresetLocalhost = findViewById(R.id.btn_preset_localhost);
        btnTestPing = findViewById(R.id.btn_test_ping);
        btnSyncNow = findViewById(R.id.btn_sync_now);
        btnSaveUrl = findViewById(R.id.btn_save_url);
        btnClose = findViewById(R.id.btn_dialog_close);

        String currentUrl = syncManager.getServerUrl();
        etServerUrl.setText(currentUrl);
        tvStatusSummary.setText("Current Target: " + currentUrl);

        btnPresetEmulator.setOnClickListener(v -> etServerUrl.setText("http://10.0.2.2:8080"));
        btnPresetLocalhost.setOnClickListener(v -> etServerUrl.setText("http://127.0.0.1:8080"));

        btnTestPing.setOnClickListener(v -> {
            String testUrl = etServerUrl.getText() != null ? etServerUrl.getText().toString().trim() : "";
            if (testUrl.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a server URL", Toast.LENGTH_SHORT).show();
                return;
            }
            tvPingResult.setText("Pinging " + testUrl + "...");
            tvPingResult.setBackgroundResource(R.drawable.bg_badge_pending);
            tvPingResult.setTextColor(ContextCompat.getColor(getContext(), R.color.status_pending));

            syncManager.testConnection(testUrl, (success, latencyMs, message) -> {
                if (success) {
                    tvPingResult.setBackgroundResource(R.drawable.bg_badge_admin_approved);
                    tvPingResult.setTextColor(ContextCompat.getColor(getContext(), R.color.status_admin_approved));
                    tvPingResult.setText("✓ " + message + " - Server is active & ready for multi-device sync!");
                } else {
                    tvPingResult.setBackgroundResource(R.drawable.bg_badge_rejected);
                    tvPingResult.setTextColor(ContextCompat.getColor(getContext(), R.color.status_rejected));
                    tvPingResult.setText("⚠️ " + message);
                }
            });
        });

        btnSyncNow.setOnClickListener(v -> {
            syncManager.triggerImmediateSync();
            Toast.makeText(getContext(), "Sync cycle triggered!", Toast.LENGTH_SHORT).show();
        });

        btnSaveUrl.setOnClickListener(v -> {
            String newUrl = etServerUrl.getText() != null ? etServerUrl.getText().toString().trim() : "";
            if (!newUrl.isEmpty()) {
                syncManager.setServerUrl(newUrl);
                Toast.makeText(getContext(), "✓ Saved! Linked to: " + newUrl, Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        btnClose.setOnClickListener(v -> dismiss());
    }
}
