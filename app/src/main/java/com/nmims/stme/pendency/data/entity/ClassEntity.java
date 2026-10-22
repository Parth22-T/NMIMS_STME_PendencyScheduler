package com.nmims.stme.pendency.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "classes")
public class ClassEntity {
    @PrimaryKey
    @NonNull
    private String classId; // e.g. "BTECH_CE_S1_A"
    private String displayName; // e.g. "B.Tech. CE SEC A SEM I"
    private String department; // "CE", "AIDS", "MBA.Tech"
    private int semester;
    private String section; // "A", "B"
    private String defaultRoom; // "CR 302"
    private String crNames; // "CR 1 & CR 2"

    public ClassEntity(@NonNull String classId, String displayName, String department, int semester, String section, String defaultRoom, String crNames) {
        this.classId = classId;
        this.displayName = displayName;
        this.department = department;
        this.semester = semester;
        this.section = section;
        this.defaultRoom = defaultRoom;
        this.crNames = crNames;
    }

    @NonNull
    public String getClassId() { return classId; }
    public void setClassId(@NonNull String classId) { this.classId = classId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getDefaultRoom() { return defaultRoom; }
    public void setDefaultRoom(String defaultRoom) { this.defaultRoom = defaultRoom; }

    public String getCrNames() { return crNames; }
    public void setCrNames(String crNames) { this.crNames = crNames; }
}
