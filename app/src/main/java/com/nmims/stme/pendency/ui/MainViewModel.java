package com.nmims.stme.pendency.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nmims.stme.pendency.data.entity.ClassEntity;
import com.nmims.stme.pendency.data.entity.FacultyEntity;
import com.nmims.stme.pendency.data.entity.FacultyLeaveEntity;
import com.nmims.stme.pendency.data.entity.HolidayEntity;
import com.nmims.stme.pendency.data.entity.PendencyRequestEntity;
import com.nmims.stme.pendency.data.entity.SubjectEntity;
import com.nmims.stme.pendency.data.entity.TimetableSlotEntity;
import com.nmims.stme.pendency.data.repository.PendencyRepository;
import com.nmims.stme.pendency.sync.NetworkSyncManager;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final PendencyRepository repository;

    private final MutableLiveData<String> selectedClassId = new MutableLiveData<>("BTECH_CE_S1_A");
    private final MutableLiveData<String> selectedFacultyCode = new MutableLiveData<>("NA");
    private final MutableLiveData<Integer> currentRole = new MutableLiveData<>(0); // 0=CR, 1=Faculty, 2=Admin

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new PendencyRepository(application);
    }

    public PendencyRepository getRepository() {
        return repository;
    }

    public LiveData<NetworkSyncManager.SyncState> getSyncState() {
        return repository.getSyncManager().getSyncStateLive();
    }

    public LiveData<String> getLastSyncMessage() {
        return repository.getSyncManager().getLastSyncMessageLive();
    }

    public LiveData<Integer> getCurrentRole() { return currentRole; }
    public void setCurrentRole(int role) { currentRole.setValue(role); }

    public LiveData<String> getSelectedClassId() { return selectedClassId; }
    public void setSelectedClassId(String classId) { selectedClassId.setValue(classId); }

    public LiveData<String> getSelectedFacultyCode() { return selectedFacultyCode; }
    public void setSelectedFacultyCode(String facultyCode) { selectedFacultyCode.setValue(facultyCode); }

    public LiveData<List<ClassEntity>> getAllClasses() {
        return repository.getAllClasses();
    }

    public LiveData<List<FacultyEntity>> getAllFaculties() {
        return repository.getAllFaculties();
    }

    public LiveData<List<SubjectEntity>> getSubjectsForCurrentClass() {
        String cid = selectedClassId.getValue() != null ? selectedClassId.getValue() : "BTECH_CE_S1_A";
        return repository.getSubjectsForClass(cid);
    }

    public LiveData<List<SubjectEntity>> getAllSubjects() {
        return repository.getAllSubjects();
    }

    public LiveData<List<TimetableSlotEntity>> getSlotsForCurrentClass() {
        String cid = selectedClassId.getValue() != null ? selectedClassId.getValue() : "BTECH_CE_S1_A";
        return repository.getSlotsForClass(cid);
    }

    public LiveData<List<TimetableSlotEntity>> getSlotsForCurrentFaculty() {
        String fcode = selectedFacultyCode.getValue() != null ? selectedFacultyCode.getValue() : "NA";
        return repository.getSlotsForFaculty(fcode);
    }

    public LiveData<List<HolidayEntity>> getAllHolidays() {
        return repository.getAllHolidays();
    }

    public LiveData<List<FacultyLeaveEntity>> getAllFacultyLeaves() {
        return repository.getAllFacultyLeaves();
    }

    public LiveData<List<PendencyRequestEntity>> getAllRequests() {
        return repository.getAllRequests();
    }

    public LiveData<List<PendencyRequestEntity>> getRequestsForCurrentClass() {
        String cid = selectedClassId.getValue() != null ? selectedClassId.getValue() : "BTECH_CE_S1_A";
        return repository.getRequestsForClass(cid);
    }

    public LiveData<List<PendencyRequestEntity>> getRequestsForCurrentFaculty() {
        String fcode = selectedFacultyCode.getValue() != null ? selectedFacultyCode.getValue() : "NA";
        return repository.getRequestsForFaculty(fcode);
    }

    public LiveData<List<PendencyRequestEntity>> getRequestsPendingAdminApproval() {
        return repository.getRequestsPendingAdminApproval();
    }

    public void submitRequest(PendencyRequestEntity request, Runnable onComplete) {
        repository.submitRequest(request, onComplete);
    }

    public void facultyApprove(long requestId, String remarks, Runnable onComplete) {
        repository.facultyApprove(requestId, remarks, onComplete);
    }

    public void facultyReject(long requestId, String remarks, Runnable onComplete) {
        repository.facultyReject(requestId, remarks, onComplete);
    }

    public void adminApprove(long requestId, String remarks, Runnable onComplete) {
        repository.adminApprove(requestId, remarks, onComplete);
    }

    public void adminReject(long requestId, String remarks, Runnable onComplete) {
        repository.adminReject(requestId, remarks, onComplete);
    }

    public void intimateFacultyLeave(FacultyLeaveEntity leave, List<PendencyRequestEntity> notices, Runnable onComplete) {
        repository.intimateFacultyLeave(leave, notices, onComplete);
    }
}
