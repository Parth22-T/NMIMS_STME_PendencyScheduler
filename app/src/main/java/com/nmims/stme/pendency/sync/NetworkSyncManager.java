package com.nmims.stme.pendency.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nmims.stme.pendency.data.AppDatabase;
import com.nmims.stme.pendency.data.entity.FacultyLeaveEntity;
import com.nmims.stme.pendency.data.entity.PendencyRequestEntity;
import com.nmims.stme.pendency.data.entity.SubjectEntity;
import com.nmims.stme.pendency.data.entity.TimetableSlotEntity;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * High-reliability Network Synchronization Engine for multi-device coordination.
 * Links Class Representatives, Faculty Members, and Academic Administration in real time.
 */
public class NetworkSyncManager {

    private static final String TAG = "NetworkSync";
    private static final String PREF_NAME = "nmims_sync_prefs";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_HOST_MODE = "is_host_mode";
    public static final String DEFAULT_EMULATOR_URL = "http://10.0.2.2:8080";

    public enum SyncState {
        CONNECTED,
        CONNECTING,
        OFFLINE
    }

    private static volatile NetworkSyncManager INSTANCE;
    private final Context context;
    private final AppDatabase db;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final SharedPreferences prefs;
    private final ExecutorService syncExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<SyncState> syncStateLive = new MutableLiveData<>(SyncState.OFFLINE);
    private final MutableLiveData<String> lastSyncMessageLive = new MutableLiveData<>("Local database initialized");
    private final MutableLiveData<Long> lastSyncTimeLive = new MutableLiveData<>(0L);

    private String serverUrl;
    private long lastSyncedTimestamp = 0;
    private boolean isPollingActive = false;

    public static NetworkSyncManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (NetworkSyncManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NetworkSyncManager(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    private NetworkSyncManager(Context context) {
        this.context = context;
        this.db = AppDatabase.getDatabase(context);
        this.gson = new Gson();
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.serverUrl = prefs.getString(KEY_SERVER_URL, DEFAULT_EMULATOR_URL);

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        startAutoSyncLoop();
    }

    public LiveData<SyncState> getSyncStateLive() {
        return syncStateLive;
    }

    public LiveData<String> getLastSyncMessageLive() {
        return lastSyncMessageLive;
    }

    public LiveData<Long> getLastSyncTimeLive() {
        return lastSyncTimeLive;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String newUrl) {
        if (newUrl == null || newUrl.trim().isEmpty()) return;
        newUrl = newUrl.trim();
        if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
            newUrl = "http://" + newUrl;
        }
        if (newUrl.endsWith("/")) {
            newUrl = newUrl.substring(0, newUrl.length() - 1);
        }
        this.serverUrl = newUrl;
        prefs.edit().putString(KEY_SERVER_URL, this.serverUrl).apply();
        triggerImmediateSync();
    }

    public void triggerImmediateSync() {
        syncExecutor.execute(this::performSyncPull);
    }

    private void startAutoSyncLoop() {
        if (isPollingActive) return;
        isPollingActive = true;

        Runnable pollRunnable = new Runnable() {
            @Override
            public void run() {
                syncExecutor.execute(() -> {
                    performSyncPull();
                    // Schedule next poll in 2.5 seconds
                    mainHandler.postDelayed(this, 2500);
                });
            }
        };
        mainHandler.postDelayed(pollRunnable, 1000);
    }

    /**
     * Pull delta changes from the server and apply to local database.
     */
    private void performSyncPull() {
        String endpoint = serverUrl + "/api/sync/pull?since=" + lastSyncedTimestamp;
        Request request = new Request.Builder()
                .url(endpoint)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();

                long serverTime = root.has("serverTime") ? root.get("serverTime").getAsLong() : System.currentTimeMillis();
                
                // Process Requests
                if (root.has("requests") && root.get("requests").isJsonArray()) {
                    JsonArray reqsArray = root.getAsJsonArray("requests");
                    for (JsonElement el : reqsArray) {
                        try {
                            PendencyRequestEntity incoming = gson.fromJson(el, PendencyRequestEntity.class);
                            if (incoming != null && incoming.getSyncId() != null) {
                                PendencyRequestEntity existing = db.requestDao().getRequestBySyncId(incoming.getSyncId());
                                if (existing == null) {
                                    db.requestDao().insert(incoming);
                                } else {
                                    // Preserve local primary key while syncing fields
                                    incoming.setRequestId(existing.getRequestId());
                                    db.requestDao().update(incoming);
                                }
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error applying synced request: " + e.getMessage());
                        }
                    }
                }

                // Process Timetable Rescheduled Slots
                if (root.has("timetableSlots") && root.get("timetableSlots").isJsonArray()) {
                    JsonArray slotsArray = root.getAsJsonArray("timetableSlots");
                    for (JsonElement el : slotsArray) {
                        try {
                            TimetableSlotEntity incomingSlot = gson.fromJson(el, TimetableSlotEntity.class);
                            if (incomingSlot != null && incomingSlot.getSyncId() != null) {
                                TimetableSlotEntity existing = db.timetableDao().getSlotBySyncId(incomingSlot.getSyncId());
                                if (existing == null) {
                                    db.timetableDao().insert(incomingSlot);
                                }
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error applying synced slot: " + e.getMessage());
                        }
                    }
                }

                // Process Faculty Leaves
                if (root.has("facultyLeaves") && root.get("facultyLeaves").isJsonArray()) {
                    JsonArray leavesArray = root.getAsJsonArray("facultyLeaves");
                    for (JsonElement el : leavesArray) {
                        try {
                            FacultyLeaveEntity incomingLeave = gson.fromJson(el, FacultyLeaveEntity.class);
                            if (incomingLeave != null) {
                                if (incomingLeave.getLeaveId() == null || incomingLeave.getLeaveId().isEmpty()) {
                                    JsonObject obj = el.getAsJsonObject();
                                    String id = obj.has("syncId") ? obj.get("syncId").getAsString()
                                            : (obj.has("leaveId") ? obj.get("leaveId").getAsString() : UUID.randomUUID().toString());
                                    incomingLeave.setLeaveId(id);
                                }
                                db.facultyLeaveDao().insert(incomingLeave);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error applying synced leave: " + e.getMessage());
                        }
                    }
                }

                // Process Subject Quota Updates
                if (root.has("subjectUpdates") && root.get("subjectUpdates").isJsonArray()) {
                    JsonArray subjArray = root.getAsJsonArray("subjectUpdates");
                    for (JsonElement el : subjArray) {
                        try {
                            JsonObject obj = el.getAsJsonObject();
                            String classId = obj.get("classId").getAsString();
                            String subjectName = obj.get("subjectName").getAsString();
                            int scheduled = obj.get("scheduledLectures").getAsInt();
                            int pending = obj.get("pendingLectures").getAsInt();

                            SubjectEntity s = db.subjectDao().getSubjectByClassAndName(classId, subjectName);
                            if (s != null) {
                                s.setScheduledLectures(scheduled);
                                s.setPendingLectures(pending);
                                db.subjectDao().update(s);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error applying synced subject: " + e.getMessage());
                        }
                    }
                }

                lastSyncedTimestamp = serverTime;
                mainHandler.post(() -> {
                    syncStateLive.setValue(SyncState.CONNECTED);
                    lastSyncMessageLive.setValue("Linked & Synced with " + serverUrl);
                    lastSyncTimeLive.setValue(System.currentTimeMillis());
                });
            } else {
                mainHandler.post(() -> {
                    syncStateLive.setValue(SyncState.OFFLINE);
                    lastSyncMessageLive.setValue("Server responded with code " + response.code());
                });
            }
        } catch (IOException e) {
            mainHandler.post(() -> {
                syncStateLive.setValue(SyncState.OFFLINE);
                lastSyncMessageLive.setValue("Offline: " + e.getClass().getSimpleName());
            });
        }
    }

    /**
     * Push new CR Reschedule Request to Server immediately.
     */
    public void pushRequest(PendencyRequestEntity request, Runnable onDone) {
        syncExecutor.execute(() -> {
            try {
                String json = gson.toJson(request);
                RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
                Request req = new Request.Builder()
                        .url(serverUrl + "/api/requests/submit")
                        .post(body)
                        .build();

                try (Response res = httpClient.newCall(req).execute()) {
                    if (res.isSuccessful()) {
                        Log.i(TAG, "Request pushed successfully to server: " + request.getSyncId());
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Push request failed (will sync later): " + e.getMessage());
            } finally {
                if (onDone != null) mainHandler.post(onDone);
            }
        });
    }

    /**
     * Push Faculty Approval / Rejection to Server immediately.
     */
    public void pushFacultyAction(String syncId, String status, String remarks, Runnable onDone) {
        syncExecutor.execute(() -> {
            try {
                JsonObject obj = new JsonObject();
                obj.addProperty("syncId", syncId);
                obj.addProperty("status", status);
                obj.addProperty("remarks", remarks);
                obj.addProperty("timestamp", System.currentTimeMillis());

                RequestBody body = RequestBody.create(obj.toString(), MediaType.parse("application/json"));
                Request req = new Request.Builder()
                        .url(serverUrl + "/api/requests/faculty-action")
                        .post(body)
                        .build();

                try (Response res = httpClient.newCall(req).execute()) {
                    if (res.isSuccessful()) {
                        Log.i(TAG, "Faculty action pushed to server for " + syncId);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Push faculty action failed: " + e.getMessage());
            } finally {
                if (onDone != null) mainHandler.post(onDone);
            }
        });
    }

    /**
     * Push Admin Clearance & Official Rescheduled Slot Booking to Server immediately.
     */
    public void pushAdminAction(String syncId, String status, String remarks,
                                TimetableSlotEntity newSlot, String classId, String subjectName,
                                Runnable onDone) {
        syncExecutor.execute(() -> {
            try {
                JsonObject obj = new JsonObject();
                obj.addProperty("syncId", syncId);
                obj.addProperty("status", status);
                obj.addProperty("remarks", remarks);
                obj.addProperty("timestamp", System.currentTimeMillis());

                if (newSlot != null) {
                    obj.add("newSlot", gson.toJsonTree(newSlot));
                }
                if (classId != null && subjectName != null) {
                    obj.addProperty("classId", classId);
                    obj.addProperty("subjectName", subjectName);
                }

                RequestBody body = RequestBody.create(obj.toString(), MediaType.parse("application/json"));
                Request req = new Request.Builder()
                        .url(serverUrl + "/api/requests/admin-action")
                        .post(body)
                        .build();

                try (Response res = httpClient.newCall(req).execute()) {
                    if (res.isSuccessful()) {
                        Log.i(TAG, "Admin action pushed to server for " + syncId);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Push admin action failed: " + e.getMessage());
            } finally {
                if (onDone != null) mainHandler.post(onDone);
            }
        });
    }

    /**
     * Push Faculty Leave Intimation to Server immediately.
     */
    public void pushFacultyLeave(FacultyLeaveEntity leave, Runnable onDone) {
        syncExecutor.execute(() -> {
            try {
                String json = gson.toJson(leave);
                RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
                Request req = new Request.Builder()
                        .url(serverUrl + "/api/leaves/intimate")
                        .post(body)
                        .build();

                try (Response res = httpClient.newCall(req).execute()) {
                    if (res.isSuccessful()) {
                        Log.i(TAG, "Faculty leave pushed to server for " + leave.getFacultyCode());
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Push leave failed: " + e.getMessage());
            } finally {
                if (onDone != null) mainHandler.post(onDone);
            }
        });
    }

    /**
     * Ping test to check server connectivity and latency.
     */
    public void testConnection(String testUrl, ConnectionTestCallback callback) {
        syncExecutor.execute(() -> {
            long start = System.currentTimeMillis();
            String endpoint = testUrl + "/api/status";
            Request request = new Request.Builder().url(endpoint).get().build();

            try (Response response = httpClient.newCall(request).execute()) {
                long latency = System.currentTimeMillis() - start;
                boolean ok = response.isSuccessful();
                mainHandler.post(() -> callback.onResult(ok, latency, ok ? "Connected (" + latency + "ms)" : "HTTP " + response.code()));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onResult(false, -1, "Connection failed: " + e.getLocalizedMessage()));
            }
        });
    }

    public interface ConnectionTestCallback {
        void onResult(boolean success, long latencyMs, String message);
    }
}
