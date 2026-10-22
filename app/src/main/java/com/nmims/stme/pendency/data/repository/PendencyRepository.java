package com.nmims.stme.pendency.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.nmims.stme.pendency.data.AppDatabase;
import com.nmims.stme.pendency.data.entity.ClassEntity;
import com.nmims.stme.pendency.data.entity.FacultyEntity;
import com.nmims.stme.pendency.data.entity.FacultyLeaveEntity;
import com.nmims.stme.pendency.data.entity.HolidayEntity;
import com.nmims.stme.pendency.data.entity.PendencyRequestEntity;
import com.nmims.stme.pendency.data.entity.SubjectEntity;
import com.nmims.stme.pendency.data.entity.TimetableSlotEntity;
import com.nmims.stme.pendency.sync.NetworkSyncManager;

import java.util.List;

public class PendencyRepository {

    private final AppDatabase db;
    private final NetworkSyncManager syncManager;

    public PendencyRepository(Application application) {
        db = AppDatabase.getDatabase(application);
        syncManager = NetworkSyncManager.getInstance(application);
    }

    public AppDatabase getDatabase() {
        return db;
    }

    public NetworkSyncManager getSyncManager() {
        return syncManager;
    }

    // Classes
    public LiveData<List<ClassEntity>> getAllClasses() {
        return db.classDao().getAllClassesLive();
    }

    // Faculties
    public LiveData<List<FacultyEntity>> getAllFaculties() {
        return db.facultyDao().getAllFacultiesLive();
    }

    // Subjects
    public LiveData<List<SubjectEntity>> getSubjectsForClass(String classId) {
        return db.subjectDao().getSubjectsForClassLive(classId);
    }

    public LiveData<List<SubjectEntity>> getAllSubjects() {
        return db.subjectDao().getAllSubjectsLive();
    }

    // Timetable Slots
    public LiveData<List<TimetableSlotEntity>> getSlotsForClass(String classId) {
        return db.timetableDao().getSlotsForClassLive(classId);
    }

    public LiveData<List<TimetableSlotEntity>> getSlotsForFaculty(String facultyCode) {
        return db.timetableDao().getSlotsForFacultyLive(facultyCode);
    }

    // Holidays
    public LiveData<List<HolidayEntity>> getAllHolidays() {
        return db.holidayDao().getAllHolidaysLive();
    }

    // Faculty Leaves
    public LiveData<List<FacultyLeaveEntity>> getAllFacultyLeaves() {
        return db.facultyLeaveDao().getAllLeavesLive();
    }

    public LiveData<List<FacultyLeaveEntity>> getLeavesForFaculty(String facultyCode) {
        return db.facultyLeaveDao().getLeavesForFacultyLive(facultyCode);
    }

    // Requests
    public LiveData<List<PendencyRequestEntity>> getAllRequests() {
        return db.requestDao().getAllRequestsLive();
    }

    public LiveData<List<PendencyRequestEntity>> getRequestsForClass(String classId) {
        return db.requestDao().getRequestsForClassLive(classId);
    }

    public LiveData<List<PendencyRequestEntity>> getRequestsForFaculty(String facultyCode) {
        return db.requestDao().getRequestsForFacultyLive(facultyCode);
    }

    public LiveData<List<PendencyRequestEntity>> getRequestsPendingAdminApproval() {
        return db.requestDao().getRequestsPendingAdminApprovalLive();
    }

    // CR Actions
    public void submitRequest(PendencyRequestEntity request, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.requestDao().insert(request);
            // Immediately push to network sync
            syncManager.pushRequest(request, onComplete);
        });
    }

    // Faculty Actions
    public void facultyApprove(long requestId, String remarks, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            db.requestDao().updateFacultyStatus(requestId, "FACULTY_APPROVED", remarks, now);
            PendencyRequestEntity req = db.requestDao().getRequestById(requestId);
            if (req != null) {
                syncManager.pushFacultyAction(req.getSyncId(), "FACULTY_APPROVED", remarks, onComplete);
            } else {
                if (onComplete != null) onComplete.run();
            }
        });
    }

    public void facultyReject(long requestId, String remarks, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            db.requestDao().updateFacultyStatus(requestId, "FACULTY_REJECTED", remarks, now);
            PendencyRequestEntity req = db.requestDao().getRequestById(requestId);
            if (req != null) {
                syncManager.pushFacultyAction(req.getSyncId(), "FACULTY_REJECTED", remarks, onComplete);
            } else {
                if (onComplete != null) onComplete.run();
            }
        });
    }

    // Admin Actions
    public void adminApprove(long requestId, String remarks, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            db.requestDao().updateAdminStatus(requestId, "ADMIN_APPROVED", remarks, now);
            PendencyRequestEntity req = db.requestDao().getRequestById(requestId);
            TimetableSlotEntity newSlot = null;

            if (req != null) {
                // 1. Add rescheduled slot to timetable
                String dayOfWeek = "Mon";
                try {
                    java.time.LocalDate localDate = java.time.LocalDate.parse(req.getProposedDate());
                    String day = localDate.getDayOfWeek().toString().substring(0, 3);
                    dayOfWeek = day.substring(0, 1).toUpperCase() + day.substring(1).toLowerCase();
                    if (dayOfWeek.equals("Tue")) dayOfWeek = "Tues";
                    if (dayOfWeek.equals("Thu")) dayOfWeek = "Thurs";
                } catch (Exception ignored) {}

                String startTime = "09:00";
                String endTime = "10:00";
                if (req.getProposedTimeSlot() != null && req.getProposedTimeSlot().contains("to")) {
                    String[] parts = req.getProposedTimeSlot().split("to");
                    if (parts.length == 2) {
                        startTime = parts[0].trim();
                        endTime = parts[1].trim();
                    }
                }

                newSlot = new TimetableSlotEntity(
                        req.getClassId(),
                        dayOfWeek,
                        startTime,
                        endTime,
                        req.getProposedTimeSlot(),
                        req.getSubjectName() + " (Rescheduled)",
                        req.getFacultyCode(),
                        req.getProposedRoom(),
                        "All",
                        "RESCHEDULED"
                );
                db.timetableDao().insert(newSlot);

                // 2. Increment scheduled/conducted count in subjects
                db.subjectDao().incrementScheduledCount(req.getClassId(), req.getSubjectName());

                // 3. Push to network sync for other devices
                syncManager.pushAdminAction(req.getSyncId(), "ADMIN_APPROVED", remarks, newSlot, req.getClassId(), req.getSubjectName(), onComplete);
            } else {
                if (onComplete != null) onComplete.run();
            }
        });
    }

    public void adminReject(long requestId, String remarks, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            db.requestDao().updateAdminStatus(requestId, "ADMIN_REJECTED", remarks, now);
            PendencyRequestEntity req = db.requestDao().getRequestById(requestId);
            if (req != null) {
                syncManager.pushAdminAction(req.getSyncId(), "ADMIN_REJECTED", remarks, null, null, null, onComplete);
            } else {
                if (onComplete != null) onComplete.run();
            }
        });
    }

    // Faculty Leave Intimation
    public void intimateFacultyLeave(FacultyLeaveEntity leave, List<PendencyRequestEntity> notices, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.facultyLeaveDao().insert(leave);
            for (PendencyRequestEntity notice : notices) {
                db.requestDao().insert(notice);
                SubjectEntity sub = db.subjectDao().getSubjectByClassAndName(notice.getClassId(), notice.getSubjectName());
                if (sub != null) {
                    sub.setPendingLectures(sub.getPendingLectures() + 1);
                    db.subjectDao().update(sub);
                }
                syncManager.pushRequest(notice, null);
            }
            syncManager.pushFacultyLeave(leave, onComplete);
        });
    }
}
