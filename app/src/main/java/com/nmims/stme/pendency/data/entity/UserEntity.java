package com.nmims.stme.pendency.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey
    @NonNull
    private String email;

    @NonNull
    private String password;

    private int role; // 0 = CR, 1 = FACULTY, 2 = ADMIN

    @NonNull
    private String displayName;

    private String associatedClassId; // For CR (e.g. BTECH_CE_S1_A)

    private String associatedFacultyCode; // For Faculty (e.g. VK, NA, MJ)

    private String department;

    private String rollOrEmployeeId;

    public UserEntity(@NonNull String email, @NonNull String password, int role,
                      @NonNull String displayName, String associatedClassId,
                      String associatedFacultyCode, String department, String rollOrEmployeeId) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.displayName = displayName;
        this.associatedClassId = associatedClassId;
        this.associatedFacultyCode = associatedFacultyCode;
        this.department = department;
        this.rollOrEmployeeId = rollOrEmployeeId;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@NonNull String displayName) {
        this.displayName = displayName;
    }

    public String getAssociatedClassId() {
        return associatedClassId;
    }

    public void setAssociatedClassId(String associatedClassId) {
        this.associatedClassId = associatedClassId;
    }

    public String getAssociatedFacultyCode() {
        return associatedFacultyCode;
    }

    public void setAssociatedFacultyCode(String associatedFacultyCode) {
        this.associatedFacultyCode = associatedFacultyCode;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRollOrEmployeeId() {
        return rollOrEmployeeId;
    }

    public void setRollOrEmployeeId(String rollOrEmployeeId) {
        this.rollOrEmployeeId = rollOrEmployeeId;
    }

    public String getRoleName() {
        switch (role) {
            case 2:
                return "Admin Dept";
            case 1:
                return "Faculty Member";
            case 0:
            default:
                return "Class Representative (CR)";
        }
    }
}
