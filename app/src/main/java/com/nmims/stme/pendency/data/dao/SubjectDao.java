package com.nmims.stme.pendency.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.nmims.stme.pendency.data.entity.SubjectEntity;

import java.util.List;

@Dao
public interface SubjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SubjectEntity> subjects);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SubjectEntity subject);

    @Update
    void update(SubjectEntity subject);

    @Query("SELECT * FROM subjects WHERE classId = :classId ORDER BY name ASC")
    LiveData<List<SubjectEntity>> getSubjectsForClassLive(String classId);

    @Query("SELECT * FROM subjects WHERE classId = :classId ORDER BY name ASC")
    List<SubjectEntity> getSubjectsForClassSync(String classId);

    @Query("SELECT * FROM subjects WHERE facultyCode = :facultyCode ORDER BY name ASC")
    List<SubjectEntity> getSubjectsForFacultySync(String facultyCode);

    @Query("SELECT * FROM subjects WHERE classId = :classId AND name = :name LIMIT 1")
    SubjectEntity getSubjectByClassAndName(String classId, String name);

    @Query("SELECT * FROM subjects ORDER BY classId ASC, name ASC")
    LiveData<List<SubjectEntity>> getAllSubjectsLive();

    @Query("SELECT * FROM subjects ORDER BY classId ASC, name ASC")
    List<SubjectEntity> getAllSubjectsSync();

    @Query("UPDATE subjects SET scheduledLectures = scheduledLectures + 1, pendingLectures = MAX(0, pendingLectures - 1) WHERE classId = :classId AND name = :subjectName")
    void incrementScheduledCount(String classId, String subjectName);
}
