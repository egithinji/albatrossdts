package com.example.albatrossdts;

import com.google.firebase.Timestamp;

public class Transaction {
    private String barcode_number;
    private String document_title;
    private String uid;
    private String employee_name;
    private String transaction_type;
    private String purpose;
    private Timestamp date;

    public Transaction(String barcode_number, String document_title, String uid, String employee_name, String transaction_type, String purpose, Timestamp date) {
        this.barcode_number = barcode_number;
        this.document_title = document_title;
        this.uid = uid;
        this.employee_name = employee_name;
        this.transaction_type = transaction_type;
        this.purpose = purpose;
        this.date = date;
    }

    public Transaction(){}

    public String getBarcode_number() {
        return barcode_number;
    }

    public void setBarcode_number(String barcode_number) {
        this.barcode_number = barcode_number;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTransaction_type() {
        return transaction_type;
    }

    public void setTransaction_type(String transaction_type) {
        this.transaction_type = transaction_type;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getDocument_title() {
        return document_title;
    }

    public void setDocument_title(String document_title) {
        this.document_title = document_title;
    }

    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
    }
}
