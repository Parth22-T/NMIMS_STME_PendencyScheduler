package com.nmims.stme.pendency.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nmims.stme.pendency.data.entity.TimetableSlotEntity;

import java.util.List;

@Dao
public interface TimetableDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TimetableSlotEntity> slots);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TimetableSlotEntity slot);

    @Query("SELECT * FROM timetable_slots ORDER BY dayOfWeek, startTime")
    List<TimetableSlotEntity> getAllSlotsSync();

    @Query("SELECT * FROM timetable_slots WHERE classId = :classId ORDER BY dayOfWeek, startTime")
    LiveData<List<TimetableSlotEntity>> getSlotsForClassLive(String classId);

    @Query("SELECT * FROM timetable_slots WHERE classId = :classId ORDER BY dayOfWeek, startTime")
    List<TimetableSlotEntity> getSlotsForClassSync(String classId);

    @Query("SELECT * FROM timetable_slots WHERE classId = :classId AND dayOfWeek = :day ORDER BY startTime")
    List<TimetableSlotEntity> getSlotsForClassAndDaySync(String classId, String day);

    @Query("SELECT * FROM timetable_slots WHERE facultyCode = :facultyCode ORDER BY dayOfWeek, startTime")
    LiveData<List<TimetableSlotEntity>> getSlotsForFacultyLive(String facultyCode);

    @Query("SELECT * FROM timetable_slots WHERE facultyCode = :facultyCode ORDER BY dayOfWeek, startTime")
    List<TimetableSlotEntity> getSlotsForFacultySync(String facultyCode);

    @Query("SELECT * FROM timetable_slots WHERE roomLab = :room ORDER BY dayOfWeek, startTime")
    List<TimetableSlotEntity> getSlotsForRoomSync(String room);

    @Query("SELECT * FROM timetable_slots WHERE syncId = :syncId LIMIT 1")
    TimetableSlotEntity getSlotBySyncId(String syncId);

    // Conflict Check Queries:
    @Query("SELECT * FROM timetable_slots WHERE classId = :classId AND dayOfWeek = :day AND timeDisplay = :timeSlot LIMIT 1")
    TimetableSlotEntity findClassSlotConflict(String classId, String day, String timeSlot);

    @Query("SELECT * FROM timetable_slots WHERE facultyCode = :facultyCode AND dayOfWeek = :day AND timeDisplay = :timeSlot LIMIT 1")
    TimetableSlotEntity findFacultySlotConflict(String facultyCode, String day, String timeSlot);

    @Query("SELECT * FROM timetable_slots WHERE roomLab = :room AND dayOfWeek = :day AND timeDisplay = :timeSlot LIMIT 1")
    TimetableSlotEntity findRoomSlotConflict(String room, String day, String timeSlot);
}
