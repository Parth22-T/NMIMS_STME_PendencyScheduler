package com.nmims.stme.pendency.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nmims.stme.pendency.data.entity.FacultyLeaveEntity;

import java.util.List;

@Dao
public interface FacultyLeaveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FacultyLeaveEntity leave);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<FacultyLeaveEntity> leaves);

    @Query("SELECT * FROM faculty_leaves ORDER BY leaveDate ASC")
    LiveData<List<FacultyLeaveEntity>> getAllLeavesLive();

    @Query("SELECT * FROM faculty_leaves ORDER BY leaveDate ASC")
    List<FacultyLeaveEntity> getAllLeavesSync();

    @Query("SELECT * FROM faculty_leaves WHERE facultyCode = :facultyCode ORDER BY leaveDate ASC")
    LiveData<List<FacultyLeaveEntity>> getLeavesForFacultyLive(String facultyCode);

    @Query("SELECT * FROM faculty_leaves WHERE facultyCode = :facultyCode AND leaveDate = :dateStr LIMIT 1")
    FacultyLeaveEntity getLeaveByFacultyAndDateSync(String facultyCode, String dateStr);

    @Query("SELECT COUNT(*) FROM faculty_leaves WHERE facultyCode = :facultyCode AND leaveDate = :dateStr")
    int countFacultyLeavesOnDateSync(String facultyCode, String dateStr);
}
