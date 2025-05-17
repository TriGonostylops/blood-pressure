package com.example.blood_pressure.model;

import com.google.firebase.Timestamp;

public class Measurement {

    private int systolic;
    private int diastolic;
    private int pulse;
    private String note;
    private Timestamp timestamp;
    private String userId;
    private transient String documentId;

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    public Measurement() {}

    public Measurement(int systolic, int diastolic, int pulse, String note, Timestamp timestamp, String userId) {
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.pulse = pulse;
        this.note = note;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    // Getters and Setters
    public int getSystolic() {
        return systolic;
    }

    public void setSystolic(int systolic) {
        this.systolic = systolic;
    }

    public int getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(int diastolic) {
        this.diastolic = diastolic;
    }

    public int getPulse() {
        return pulse;
    }

    public void setPulse(int pulse) {
        this.pulse = pulse;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
