package com.nmims.stme.pendency.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nmims.stme.pendency.data.entity.ClassEntity;

import java.util.List;

@Dao
public interface ClassDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ClassEntity> classes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ClassEntity classEntity);

    @Query("SELECT * FROM classes ORDER BY displayName ASC")
    LiveData<List<ClassEntity>> getAllClassesLive();

    @Query("SELECT * FROM classes ORDER BY displayName ASC")
    List<ClassEntity> getAllClassesSync();

    @Query("SELECT * FROM classes WHERE classId = :classId LIMIT 1")
    ClassEntity getClassById(String classId);
}
