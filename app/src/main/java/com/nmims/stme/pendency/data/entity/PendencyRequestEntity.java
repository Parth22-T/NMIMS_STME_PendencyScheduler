package com.nmims.stme.pendency.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "pendency_requests", indices = {@Index(value = {"syncId"}, unique = true)})
public class PendencyRequestEntity {
    @PrimaryKey(autoGenerate = true)
    private long requestId;

    private String syncId;           // Globally unique ID across devices

    private String classId;          // e.g. "BTECH_CE_S1_A"
    private String className;        // "B.Tech. CE SEC A SEM I"
    private String subjectName;      // "Calculus Theory"
    private String facultyCode;      // "NA"
    private String facultyName;      // "Dr. Nidhhi Asthana"
    private String reason;           // "National Holiday: Gandhi Jayanti", "Faculty Sick Leave", etc.
    private String schedulingType;   // "PRE_SCHEDULED" or "COMPENSATORY_POST"
    
    private String originalDate;     // "2026-10-02"
    private String originalTimeSlot; // "9:00 am to 10:00 am"
    
    private String proposedDate;     // "2026-10-03"
    private String proposedTimeSlot; // "10:00 am to 11:00 am"
    private String proposedRoom;     // "CR 302"
    
    // Status flow: SUBMITTED_TO_FACULTY -> FACULTY_APPROVED -> ADMIN_APPROVED (or REJECTED)
    private String status;           // "SUBMITTED_TO_FACULTY", "FACULTY_APPROVED", "FACULTY_REJECTED", "ADMIN_APPROVED", "ADMIN_REJECTED"
    
    private String crNotes;
    private String facultyRemarks;
    private String adminRemarks;
    
    private long timestampCreated;
    private long timestampFacultyAction;
    private long timestampAdminAction;

    public PendencyRequestEntity(String classId, String className, String subjectName, String facultyCode,
                                 String facultyName, String reason, String schedulingType,
                                 String originalDate, String originalTimeSlot,
                                 String proposedDate, String proposedTimeSlot, String proposedRoom,
                                 String status, String crNotes, String facultyRemarks, String adminRemarks,
                                 long timestampCreated, long timestampFacultyAction, long timestampAdminAction) {
        this.syncId = "REQ-" + UUID.randomUUID().toString();
        this.classId = classId;
        this.className = className;
        this.subjectName = subjectName;
        this.facultyCode = facultyCode;
        this.facultyName = facultyName;
        this.reason = reason;
        this.schedulingType = schedulingType;
        this.originalDate = originalDate;
        this.originalTimeSlot = originalTimeSlot;
        this.proposedDate = proposedDate;
        this.proposedTimeSlot = proposedTimeSlot;
        this.proposedRoom = proposedRoom;
        this.status = status;
        this.crNotes = crNotes;
        this.facultyRemarks = facultyRemarks;
        this.adminRemarks = adminRemarks;
        this.timestampCreated = timestampCreated;
        this.timestampFacultyAction = timestampFacultyAction;
        this.timestampAdminAction = timestampAdminAction;
    }

    public long getRequestId() { return requestId; }
    public void setRequestId(long requestId) { this.requestId = requestId; }

    public String getSyncId() {
        if (syncId == null || syncId.isEmpty()) {
            syncId = "REQ-" + UUID.randomUUID().toString();
        }
        return syncId;
    }
    public void setSyncId(String syncId) { this.syncId = syncId; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getFacultyCode() { return facultyCode; }
    public void setFacultyCode(String facultyCode) { this.facultyCode = facultyCode; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getSchedulingType() { return schedulingType; }
    public void setSchedulingType(String schedulingType) { this.schedulingType = schedulingType; }

    public String getOriginalDate() { return originalDate; }
    public void setOriginalDate(String originalDate) { this.originalDate = originalDate; }

    public String getOriginalTimeSlot() { return originalTimeSlot; }
    public void setOriginalTimeSlot(String originalTimeSlot) { this.originalTimeSlot = originalTimeSlot; }

    public String getProposedDate() { return proposedDate; }
    public void setProposedDate(String proposedDate) { this.proposedDate = proposedDate; }

    public String getProposedTimeSlot() { return proposedTimeSlot; }
    public void setProposedTimeSlot(String proposedTimeSlot) { this.proposedTimeSlot = proposedTimeSlot; }

    public String getProposedRoom() { return proposedRoom; }
    public void setProposedRoom(String proposedRoom) { this.proposedRoom = proposedRoom; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCrNotes() { return crNotes; }
    public void setCrNotes(String crNotes) { this.crNotes = crNotes; }

    public String getFacultyRemarks() { return facultyRemarks; }
    public void setFacultyRemarks(String facultyRemarks) { this.facultyRemarks = facultyRemarks; }

    public String getAdminRemarks() { return adminRemarks; }
    public void setAdminRemarks(String adminRemarks) { this.adminRemarks = adminRemarks; }

    public long getTimestampCreated() { return timestampCreated; }
    public void setTimestampCreated(long timestampCreated) { this.timestampCreated = timestampCreated; }

    public long getTimestampFacultyAction() { return timestampFacultyAction; }
    public void setTimestampFacultyAction(long timestampFacultyAction) { this.timestampFacultyAction = timestampFacultyAction; }

    public long getTimestampAdminAction() { return timestampAdminAction; }
    public void setTimestampAdminAction(long timestampAdminAction) { this.timestampAdminAction = timestampAdminAction; }
}
