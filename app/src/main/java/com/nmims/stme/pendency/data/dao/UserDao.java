package com.nmims.stme.pendency.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nmims.stme.pendency.data.entity.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UserEntity> users);

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) AND password = :password LIMIT 1")
    UserEntity authenticate(String email, String password);

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Query("SELECT * FROM users ORDER BY role DESC, displayName ASC")
    List<UserEntity> getAllUsersSync();

    @Query("SELECT * FROM users ORDER BY role DESC, displayName ASC")
    LiveData<List<UserEntity>> getAllUsersLive();

    @Query("SELECT * FROM users WHERE role = :role ORDER BY displayName ASC")
    List<UserEntity> getUsersByRoleSync(int role);

    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();

    @Query("DELETE FROM users WHERE associatedFacultyCode IS NOT NULL")
    void deleteAllFacultyUsers();
}
