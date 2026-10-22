package com.nmims.stme.pendency.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.nmims.stme.pendency.data.entity.PendencyRequestEntity;

import java.util.List;

@Dao
public interface RequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PendencyRequestEntity request);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PendencyRequestEntity> requests);

    @Update
    void update(PendencyRequestEntity request);

    @Query("SELECT * FROM pendency_requests ORDER BY timestampCreated DESC")
    LiveData<List<PendencyRequestEntity>> getAllRequestsLive();

    @Query("SELECT * FROM pendency_requests ORDER BY timestampCreated DESC")
    List<PendencyRequestEntity> getAllRequestsSync();

    @Query("SELECT * FROM pendency_requests WHERE classId = :classId ORDER BY timestampCreated DESC")
    LiveData<List<PendencyRequestEntity>> getRequestsForClassLive(String classId);

    @Query("SELECT * FROM pendency_requests WHERE facultyCode = :facultyCode ORDER BY timestampCreated DESC")
    LiveData<List<PendencyRequestEntity>> getRequestsForFacultyLive(String facultyCode);

    @Query("SELECT * FROM pendency_requests WHERE status = 'FACULTY_APPROVED' ORDER BY timestampFacultyAction DESC")
    LiveData<List<PendencyRequestEntity>> getRequestsPendingAdminApprovalLive();

    @Query("SELECT * FROM pendency_requests WHERE requestId = :requestId LIMIT 1")
    PendencyRequestEntity getRequestById(long requestId);

    @Query("SELECT * FROM pendency_requests WHERE syncId = :syncId LIMIT 1")
    PendencyRequestEntity getRequestBySyncId(String syncId);

    @Query("UPDATE pendency_requests SET status = :status, facultyRemarks = :remarks, timestampFacultyAction = :time WHERE requestId = :id")
    void updateFacultyStatus(long id, String status, String remarks, long time);

    @Query("UPDATE pendency_requests SET status = :status, facultyRemarks = :remarks, timestampFacultyAction = :time WHERE syncId = :syncId")
    void updateFacultyStatusBySyncId(String syncId, String status, String remarks, long time);

    @Query("UPDATE pendency_requests SET status = :status, adminRemarks = :remarks, timestampAdminAction = :time WHERE requestId = :id")
    void updateAdminStatus(long id, String status, String remarks, long time);

    @Query("UPDATE pendency_requests SET status = :status, adminRemarks = :remarks, timestampAdminAction = :time WHERE syncId = :syncId")
    void updateAdminStatusBySyncId(String syncId, String status, String remarks, long time);
}
