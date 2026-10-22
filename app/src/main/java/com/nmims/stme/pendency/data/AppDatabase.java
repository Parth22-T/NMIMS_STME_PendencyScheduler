package com.nmims.stme.pendency.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.nmims.stme.pendency.data.dao.ClassDao;
import com.nmims.stme.pendency.data.dao.FacultyDao;
import com.nmims.stme.pendency.data.dao.FacultyLeaveDao;
import com.nmims.stme.pendency.data.dao.HolidayDao;
import com.nmims.stme.pendency.data.dao.RequestDao;
import com.nmims.stme.pendency.data.dao.SubjectDao;
import com.nmims.stme.pendency.data.dao.TimetableDao;
import com.nmims.stme.pendency.data.entity.ClassEntity;
import com.nmims.stme.pendency.data.entity.FacultyEntity;
import com.nmims.stme.pendency.data.entity.FacultyLeaveEntity;
import com.nmims.stme.pendency.data.entity.HolidayEntity;
import com.nmims.stme.pendency.data.entity.PendencyRequestEntity;
import com.nmims.stme.pendency.data.entity.SubjectEntity;
import com.nmims.stme.pendency.data.entity.TimetableSlotEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        ClassEntity.class,
        FacultyEntity.class,
        SubjectEntity.class,
        TimetableSlotEntity.class,
        HolidayEntity.class,
        PendencyRequestEntity.class,
        FacultyLeaveEntity.class
}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ClassDao classDao();
    public abstract FacultyDao facultyDao();
    public abstract SubjectDao subjectDao();
    public abstract TimetableDao timetableDao();
    public abstract HolidayDao holidayDao();
    public abstract RequestDao requestDao();
    public abstract FacultyLeaveDao facultyLeaveDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "stme_pendency_db")
                            .fallbackToDestructiveMigration()
                            .build();

                    databaseWriteExecutor.execute(() -> {
                        DatabaseInitializer.seedDatabase(INSTANCE);
                    });
                }
            }
        }
        return INSTANCE;
    }
}
