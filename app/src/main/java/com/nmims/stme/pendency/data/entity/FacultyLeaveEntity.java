package com.nmims.stme.pendency.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "faculty_leaves")
public class FacultyLeaveEntity {
    @PrimaryKey
    @NonNull
    private String leaveId;
    private String facultyCode;
    private String facultyName;
    private String leaveDate; // "YYYY-MM-DD"
    private String reason;
    private int affectedSlotsCount;
    private long timestamp;

    public FacultyLeaveEntity(@NonNull String leaveId, String facultyCode, String facultyName,
                              String leaveDate, String reason, int affectedSlotsCount, long timestamp) {
        this.leaveId = leaveId != null && !leaveId.isEmpty() ? leaveId : UUID.randomUUID().toString();
        this.facultyCode = facultyCode;
        this.facultyName = facultyName;
        this.leaveDate = leaveDate;
        this.reason = reason;
        this.affectedSlotsCount = affectedSlotsCount;
        this.timestamp = timestamp;
    }

    @NonNull
    public String getLeaveId() { return leaveId; }
    public void setLeaveId(@NonNull String leaveId) { this.leaveId = leaveId; }

    public String getFacultyCode() { return facultyCode; }
    public void setFacultyCode(String facultyCode) { this.facultyCode = facultyCode; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public String getLeaveDate() { return leaveDate; }
    public void setLeaveDate(String leaveDate) { this.leaveDate = leaveDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public int getAffectedSlotsCount() { return affectedSlotsCount; }
    public void setAffectedSlotsCount(int affectedSlotsCount) { this.affectedSlotsCount = affectedSlotsCount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
