package com.nmims.stme.pendency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.nmims.stme.pendency.data.entity.FacultyLeaveEntity;
import com.nmims.stme.pendency.data.entity.PendencyRequestEntity;
import com.nmims.stme.pendency.data.entity.SubjectEntity;
import com.nmims.stme.pendency.data.entity.TimetableSlotEntity;
import com.nmims.stme.pendency.util.ConflictChecker;

import org.junit.Test;

public class ConflictAndCreditsTest {

    @Test
    public void testCreditsToLecturesMapping() {
        // Validate credit to lecture quotas: 1 credit = 15, 2 = 30, 3 = 45, 4 = 60, 5 = 75
        int[] credits = {1, 2, 3, 4, 5};
        int[] expectedTargets = {15, 30, 45, 60, 75};

        for (int i = 0; i < credits.length; i++) {
            int credit = credits[i];
            int target = credit * 15;
            assertEquals(expectedTargets[i], target);

            SubjectEntity subject = new SubjectEntity(
                    "SUB_TEST_" + credit,
                    "Course " + credit,
                    "BTECH_CE_S1_A",
                    "NA",
                    credit,
                    target,
                    10,
                    2,
                    3
            );

            assertEquals(expectedTargets[i], subject.getTargetLectures());
            assertEquals(target - (10 + 2), subject.getRemainingLectures());
        }
    }

    @Test
    public void testApprovalLifecycleStatusAndSyncId() {
        PendencyRequestEntity req = new PendencyRequestEntity(
                "BTECH_CE_S1_A",
                "B.Tech. CE SEC A SEM I",
                "Calculus Theory",
                "NA",
                "Dr. Nidhhi Asthana",
                "Gandhi Jayanti Holiday",
                "PRE_SCHEDULED",
                "2026-10-02",
                "9:00 am to 10:00 am",
                "2026-10-03",
                "10:00 am to 11:00 am",
                "CR 302",
                "SUBMITTED_TO_FACULTY",
                "Pre-scheduling before holiday",
                "",
                "",
                System.currentTimeMillis(),
                0,
                0
        );

        // Verify unique syncId is populated
        assertNotNull(req.getSyncId());
        assertTrue(req.getSyncId().startsWith("REQ-"));

        // Stage 1: Submitted by CR
        assertEquals("SUBMITTED_TO_FACULTY", req.getStatus());

        // Stage 2: Approved by Faculty
        req.setStatus("FACULTY_APPROVED");
        req.setFacultyRemarks("Slot verified by NA. No department clash.");
        req.setTimestampFacultyAction(System.currentTimeMillis());
        assertEquals("FACULTY_APPROVED", req.getStatus());
        assertTrue(req.getFacultyRemarks().contains("Slot verified"));

        // Stage 3: Final Approval by Admin
        req.setStatus("ADMIN_APPROVED");
        req.setAdminRemarks("Room CR 302 allocated and locked. Attendance quota updated.");
        req.setTimestampAdminAction(System.currentTimeMillis());
        assertEquals("ADMIN_APPROVED", req.getStatus());
        assertTrue(req.getAdminRemarks().contains("Attendance quota updated"));
    }

    @Test
    public void testConflictCheckerLogic() {
        ConflictChecker.ConflictResult clean = ConflictChecker.ConflictResult.clean();
        assertFalse(clean.hasConflict);
        assertTrue(clean.conflictReason.contains("free and available"));

        ConflictChecker.ConflictResult conflict = ConflictChecker.ConflictResult.conflict("Room CR 302 is occupied");
        assertTrue(conflict.hasConflict);
        assertTrue(conflict.conflictReason.contains("occupied"));
    }

    @Test
    public void testSundayRestriction() {
        ConflictChecker.ConflictResult sunResult = ConflictChecker.validateSlot(
                null, "BTECH_CE_S1_A", "NA", "CR 302", "Sun", "9:00 am to 10:00 am", "2026-10-04"
        );
        assertTrue(sunResult.hasConflict);
        assertTrue(sunResult.conflictReason.contains("Sunday is not permitted"));
    }

    @Test
    public void testFacultyLeaveEntity() {
        FacultyLeaveEntity leave = new FacultyLeaveEntity(
                "LEAVE-12345",
                "NA",
                "Dr. Nidhhi Asthana",
                "2026-11-05",
                "Academic Duty / Conference",
                3,
                System.currentTimeMillis()
        );

        assertEquals("LEAVE-12345", leave.getLeaveId());
        assertEquals("NA", leave.getFacultyCode());
        assertEquals("2026-11-05", leave.getLeaveDate());
        assertEquals(3, leave.getAffectedSlotsCount());
    }

    @Test
    public void testTimetableSlotSyncId() {
        TimetableSlotEntity slot = new TimetableSlotEntity(
                "BTECH_CE_S1_A",
                "Mon",
                "09:00",
                "10:00",
                "9:00 am to 10:00 am",
                "Calculus Theory",
                "NA",
                "CR 302",
                "All",
                "REGULAR"
        );
        assertNotNull(slot.getSyncId());
        assertTrue(slot.getSyncId().startsWith("SLOT-"));
    }
}
