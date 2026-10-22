package com.nmims.stme.pendency.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nmims.stme.pendency.data.entity.HolidayEntity;

import java.util.List;

@Dao
public interface HolidayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<HolidayEntity> holidays);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HolidayEntity holiday);

    @Query("SELECT * FROM holidays ORDER BY dateStr ASC")
    LiveData<List<HolidayEntity>> getAllHolidaysLive();

    @Query("SELECT * FROM holidays ORDER BY dateStr ASC")
    List<HolidayEntity> getAllHolidaysSync();

    @Query("SELECT * FROM holidays WHERE dateStr = :dateStr LIMIT 1")
    HolidayEntity getHolidayByDate(String dateStr);

    @Query("UPDATE holidays SET isPreScheduled = :isPreScheduled WHERE id = :id")
    void setPreScheduled(long id, boolean isPreScheduled);
}
