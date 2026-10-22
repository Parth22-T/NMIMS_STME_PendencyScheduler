package com.nmims.stme.pendency.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "timetable_slots")
public class TimetableSlotEntity {
    @PrimaryKey(autoGenerate = true)
    private long slotId;

    private String syncId;

    private String classId;       // e.g. "BTECH_CE_S1_A"
    private String dayOfWeek;     // "Mon", "Tues", "Wed", "Thurs", "Fri", "Sat"
    private String startTime;     // "09:00"
    private String endTime;       // "10:00"
    private String timeDisplay;   // "9:00 am to 10:00 am"
    private String subjectName;   // "Calculus Theory"
    private String facultyCode;   // "NA"
    private String roomLab;       // "CR 302" or "CC 201"
    private String batch;         // "All", "Batch 01", "Batch 02"
    private String slotType;      // "REGULAR", "RESCHEDULED", "FREE"

    public TimetableSlotEntity(String classId, String dayOfWeek, String startTime, String endTime,
                               String timeDisplay, String subjectName, String facultyCode,
                               String roomLab, String batch, String slotType) {
        this.syncId = "SLOT-" + UUID.randomUUID().toString();
        this.classId = classId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeDisplay = timeDisplay;
        this.subjectName = subjectName;
        this.facultyCode = facultyCode;
        this.roomLab = roomLab;
        this.batch = batch;
        this.slotType = slotType;
    }

    public long getSlotId() { return slotId; }
    public void setSlotId(long slotId) { this.slotId = slotId; }

    public String getSyncId() {
        if (syncId == null || syncId.isEmpty()) {
            syncId = "SLOT-" + UUID.randomUUID().toString();
        }
        return syncId;
    }
    public void setSyncId(String syncId) { this.syncId = syncId; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getTimeDisplay() { return timeDisplay; }
    public void setTimeDisplay(String timeDisplay) { this.timeDisplay = timeDisplay; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getFacultyCode() { return facultyCode; }
    public void setFacultyCode(String facultyCode) { this.facultyCode = facultyCode; }

    public String getRoomLab() { return roomLab; }
    public void setRoomLab(String roomLab) { this.roomLab = roomLab; }

    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }

    public String getSlotType() { return slotType; }
    public void setSlotType(String slotType) { this.slotType = slotType; }
}
