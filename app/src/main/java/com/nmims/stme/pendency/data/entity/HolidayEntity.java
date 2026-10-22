package com.nmims.stme.pendency.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "holidays")
public class HolidayEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String dateStr;      // "2026-10-02"
    private String holidayName;  // "Mahatma Gandhi Jayanti"
    private String dayOfWeek;    // "Fri"
    private String type;         // "National Holiday", "Gazetted Holiday", "Academic Break"
    private boolean isPreScheduled;

    public HolidayEntity(@NonNull String dateStr, String holidayName, String dayOfWeek, String type, boolean isPreScheduled) {
        this.dateStr = dateStr;
        this.holidayName = holidayName;
        this.dayOfWeek = dayOfWeek;
        this.type = type;
        this.isPreScheduled = isPreScheduled;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull
    public String getDateStr() { return dateStr; }
    public void setDateStr(@NonNull String dateStr) { this.dateStr = dateStr; }

    public String getHolidayName() { return holidayName; }
    public void setHolidayName(String holidayName) { this.holidayName = holidayName; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isPreScheduled() { return isPreScheduled; }
    public void setPreScheduled(boolean preScheduled) { isPreScheduled = preScheduled; }
}
