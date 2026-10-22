package com.nmims.stme.pendency.util;

import com.nmims.stme.pendency.data.AppDatabase;
import com.nmims.stme.pendency.data.entity.FacultyLeaveEntity;
import com.nmims.stme.pendency.data.entity.HolidayEntity;
import com.nmims.stme.pendency.data.entity.TimetableSlotEntity;

import java.util.ArrayList;
import java.util.List;

public class ConflictChecker {

    public static class ConflictResult {
        public final boolean hasConflict;
        public final String conflictReason;

        public ConflictResult(boolean hasConflict, String conflictReason) {
            this.hasConflict = hasConflict;
            this.conflictReason = conflictReason;
        }

        public static ConflictResult clean() {
            return new ConflictResult(false, "Slot is free and available! No conflicts detected.");
        }

        public static ConflictResult conflict(String reason) {
            return new ConflictResult(true, reason);
        }
    }

    public static class SlotRecommendation {
        public final String timeSlot;
        public final String room;
        public final String description;

        public SlotRecommendation(String timeSlot, String room, String description) {
            this.timeSlot = timeSlot;
            this.room = room;
            this.description = description;
        }
    }

    private static final String[] STANDARD_PERIODS = {
            "9:00 am to 10:00 am",
            "10:00 am to 11:00 am",
            "11:50 am to 12:50 pm",
            "12:50 pm to 1:50 pm",
            "2:10 pm to 3:10 pm",
            "3:10 pm to 4:10 pm",
            "4:10 pm to 5:10 pm"
    };

    private static final String[] CANDIDATE_ROOMS = {
            "CR 302", "CR 303", "CR 304", "CR 306", "CR 307", "CR 308",
            "CR 309", "CR 310", "CR 311", "CR 312", "CR 313", "CR 314",
            "CR 315", "CR 316", "CR 317", "CR 413", "CR 301", "CC 201", "CC 202"
    };

    /**
     * Comprehensive multi-factor conflict validation:
     * 1. Official National & Academic Holiday Check
     * 2. Weekend (Sunday) Restriction
     * 3. Faculty Leave / Absence Intimation Check
     * 4. Class Timetable Clash
     * 5. Faculty Teaching Commitment Clash
     * 6. Room / Laboratory Clash
     */
    public static ConflictResult validateSlot(AppDatabase db, String classId, String facultyCode,
                                              String room, String dayOfWeek, String timeSlot, String proposedDateStr) {

        // 1. Sunday check
        if (dayOfWeek != null && (dayOfWeek.equalsIgnoreCase("Sun") || dayOfWeek.equalsIgnoreCase("Sunday"))) {
            return ConflictResult.conflict("Weekend Restriction: Rescheduling on Sunday is not permitted.");
        }

        // 2. Official Holiday check
        if (proposedDateStr != null && !proposedDateStr.isEmpty()) {
            List<HolidayEntity> holidays = db.holidayDao().getAllHolidaysSync();
            for (HolidayEntity h : holidays) {
                if (proposedDateStr.equalsIgnoreCase(h.getDateStr())) {
                    return ConflictResult.conflict("Official Holiday Clash: Proposed date " + proposedDateStr
                            + " is '" + h.getHolidayName() + "' (" + h.getType() + "). Institute is closed.");
                }
            }
        }

        // 3. Faculty Leave / Absence Check
        if (facultyCode != null && !facultyCode.isEmpty() && proposedDateStr != null && !proposedDateStr.isEmpty()) {
            FacultyLeaveEntity leave = db.facultyLeaveDao().getLeaveByFacultyAndDateSync(facultyCode, proposedDateStr);
            if (leave != null) {
                return ConflictResult.conflict("Faculty Unavailable: Faculty (" + facultyCode
                        + ") has registered leave on " + proposedDateStr + " (" + leave.getReason() + ").");
            }
        }

        // 4. Class conflict check
        TimetableSlotEntity classConflict = db.timetableDao().findClassSlotConflict(classId, dayOfWeek, timeSlot);
        if (classConflict != null) {
            return ConflictResult.conflict("Class Schedule Conflict: Class has '" + classConflict.getSubjectName()
                    + "' already scheduled in slot " + timeSlot + " on " + dayOfWeek);
        }

        // 5. Faculty conflict check
        if (facultyCode != null && !facultyCode.isEmpty()) {
            TimetableSlotEntity facultyConflict = db.timetableDao().findFacultySlotConflict(facultyCode, dayOfWeek, timeSlot);
            if (facultyConflict != null) {
                return ConflictResult.conflict("Faculty Conflict: Faculty (" + facultyCode + ") is already teaching '"
                        + facultyConflict.getSubjectName() + "' in " + facultyConflict.getRoomLab()
                        + " for " + facultyConflict.getClassId() + " in slot " + timeSlot);
            }
        }

        // 6. Room conflict check
        if (room != null && !room.isEmpty()) {
            TimetableSlotEntity roomConflict = db.timetableDao().findRoomSlotConflict(room, dayOfWeek, timeSlot);
            if (roomConflict != null) {
                return ConflictResult.conflict("Room Conflict: Room/Lab '" + room + "' is occupied by "
                        + roomConflict.getClassId() + " for '" + roomConflict.getSubjectName() + "'");
            }
        }

        return ConflictResult.clean();
    }

    // Overload for backwards compatibility
    public static ConflictResult validateSlot(AppDatabase db, String classId, String facultyCode,
                                              String room, String dayOfWeek, String timeSlot) {
        return validateSlot(db, classId, facultyCode, room, dayOfWeek, timeSlot, null);
    }

    /**
     * Smart Slot Finder: Finds up to 3 non-conflicting slots on the requested day
     * where Class is free, Faculty is free, room is available, and date is free of holiday/leave.
     */
    public static List<SlotRecommendation> findFreeSlots(AppDatabase db, String classId, String facultyCode,
                                                         String dayOfWeek, String defaultRoom, String proposedDateStr) {
        List<SlotRecommendation> recommendations = new ArrayList<>();

        // Check if date itself is blocked by holiday
        if (proposedDateStr != null && !proposedDateStr.isEmpty()) {
            List<HolidayEntity> holidays = db.holidayDao().getAllHolidaysSync();
            for (HolidayEntity h : holidays) {
                if (proposedDateStr.equalsIgnoreCase(h.getDateStr())) {
                    return recommendations; // Zero slots on official holiday
                }
            }
            // Check if faculty on leave on that date
            if (facultyCode != null && !facultyCode.isEmpty()) {
                if (db.facultyLeaveDao().countFacultyLeavesOnDateSync(facultyCode, proposedDateStr) > 0) {
                    return recommendations; // Zero slots on faculty leave date
                }
            }
        }

        for (String period : STANDARD_PERIODS) {
            // Check if class is free
            if (db.timetableDao().findClassSlotConflict(classId, dayOfWeek, period) != null) {
                continue;
            }
            // Check if faculty is free
            if (facultyCode != null && db.timetableDao().findFacultySlotConflict(facultyCode, dayOfWeek, period) != null) {
                continue;
            }

            // Check if default room is free
            String assignedRoom = defaultRoom;
            if (assignedRoom == null || db.timetableDao().findRoomSlotConflict(assignedRoom, dayOfWeek, period) != null) {
                // Find alternate free room
                assignedRoom = null;
                for (String candidate : CANDIDATE_ROOMS) {
                    if (db.timetableDao().findRoomSlotConflict(candidate, dayOfWeek, period) == null) {
                        assignedRoom = candidate;
                        break;
                    }
                }
            }

            if (assignedRoom != null) {
                recommendations.add(new SlotRecommendation(
                        period,
                        assignedRoom,
                        "✓ " + period + " in " + assignedRoom + " (Class, Faculty & Room Free)"
                ));
            }

            if (recommendations.size() >= 3) break;
        }

        return recommendations;
    }

    public static List<SlotRecommendation> findFreeSlots(AppDatabase db, String classId, String facultyCode,
                                                         String dayOfWeek, String defaultRoom) {
        return findFreeSlots(db, classId, facultyCode, dayOfWeek, defaultRoom, null);
    }
}
