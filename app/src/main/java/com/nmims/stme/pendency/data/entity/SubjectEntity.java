package com.nmims.stme.pendency.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "subjects")
public class SubjectEntity {
    @PrimaryKey
    @NonNull
    private String subjectId; // e.g. "CE_CALCULUS"
    private String name;      // "Calculus Theory"
    private String classId;   // "BTECH_CE_S1_A"
    private String facultyCode; // "NA"
    private int credits;      // 1, 2, 3, 4, 5
    private int targetLectures; // 15, 30, 45, 60, 75 (credits * 15)
    private int conductedLectures;
    private int scheduledLectures;
    private int pendingLectures;

    public SubjectEntity(@NonNull String subjectId, String name, String classId, String facultyCode,
                         int credits, int targetLectures, int conductedLectures, int scheduledLectures, int pendingLectures) {
        this.subjectId = subjectId;
        this.name = name;
        this.classId = classId;
        this.facultyCode = facultyCode;
        this.credits = credits;
        this.targetLectures = targetLectures;
        this.conductedLectures = conductedLectures;
        this.scheduledLectures = scheduledLectures;
        this.pendingLectures = pendingLectures;
    }

    @NonNull
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(@NonNull String subjectId) { this.subjectId = subjectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getFacultyCode() { return facultyCode; }
    public void setFacultyCode(String facultyCode) { this.facultyCode = facultyCode; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public int getTargetLectures() { return targetLectures; }
    public void setTargetLectures(int targetLectures) { this.targetLectures = targetLectures; }

    public int getConductedLectures() { return conductedLectures; }
    public void setConductedLectures(int conductedLectures) { this.conductedLectures = conductedLectures; }

    public int getScheduledLectures() { return scheduledLectures; }
    public void setScheduledLectures(int scheduledLectures) { this.scheduledLectures = scheduledLectures; }

    public int getPendingLectures() { return pendingLectures; }
    public void setPendingLectures(int pendingLectures) { this.pendingLectures = pendingLectures; }

    public int getRemainingLectures() {
        int rem = targetLectures - (conductedLectures + scheduledLectures);
        return Math.max(0, rem);
    }
}
