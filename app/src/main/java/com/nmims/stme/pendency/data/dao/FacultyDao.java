package com.nmims.stme.pendency.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nmims.stme.pendency.data.entity.FacultyEntity;

import java.util.List;

@Dao
public interface FacultyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<FacultyEntity> faculties);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FacultyEntity faculty);

    @Query("SELECT * FROM faculties ORDER BY name ASC")
    LiveData<List<FacultyEntity>> getAllFacultiesLive();

    @Query("SELECT * FROM faculties ORDER BY name ASC")
    List<FacultyEntity> getAllFacultiesSync();

    @Query("SELECT * FROM faculties WHERE facultyCode = :code LIMIT 1")
    FacultyEntity getFacultyByCode(String code);
}
