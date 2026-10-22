package com.nmims.stme.pendency.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "faculties")
public class FacultyEntity {
    @PrimaryKey
    @NonNull
    private String facultyCode; // e.g. "NA", "JG", "SS", "JP"
    private String name;        // e.g. "Dr. Nidhhi Asthana"
    private String designation; // e.g. "Associate Professor & TT Coordinator"
    private String department;  // e.g. "Computer Engineering"
    private String email;

    public FacultyEntity(@NonNull String facultyCode, String name, String designation, String department, String email) {
        this.facultyCode = facultyCode;
        this.name = name;
        this.designation = designation;
        this.department = department;
        this.email = email;
    }

    @NonNull
    public String getFacultyCode() { return facultyCode; }
    public void setFacultyCode(@NonNull String facultyCode) { this.facultyCode = facultyCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayLabel() {
        return name + " (" + facultyCode + ")";
    }
}
