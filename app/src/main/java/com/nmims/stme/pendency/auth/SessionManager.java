package com.nmims.stme.pendency.auth;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.nmims.stme.pendency.data.entity.UserEntity;

public class SessionManager {

    private static final String PREF_NAME = "nmims_stme_auth_pref";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_JSON = "user_json";

    private final SharedPreferences prefs;
    private final Gson gson;
    private static SessionManager instance;

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void loginUser(UserEntity user) {
        String json = gson.toJson(user);
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_JSON, json)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    @Nullable
    public UserEntity getCurrentUser() {
        if (!isLoggedIn()) return null;
        String json = prefs.getString(KEY_USER_JSON, null);
        if (json == null) return null;
        try {
            return gson.fromJson(json, UserEntity.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
