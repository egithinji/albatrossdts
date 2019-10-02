package com.example.albatrossdts;

public class Document {
    private String barcode_number;
    private String title;
    private String description;
    private String permanent_location;
    private String currently_checked_out_to;
    private String purpose;
    private String last_transaction_id; //Auto-generated id for the last transaction on this document
    private String photo_url;
    private String added_by;

    public Document(String barcode_number, String title, String description, String permanent_location, String currently_checked_out_to, String purpose, String last_transaction_id, String photo_url, String added_by) {
        this.barcode_number = barcode_number;
        this.title = title;
        this.description = description;
        this.permanent_location = permanent_location;
        this.currently_checked_out_to = currently_checked_out_to;
        this.purpose = purpose;
        this.last_transaction_id = last_transaction_id;
        this.photo_url = photo_url;
        this.added_by = added_by;
    }

    public Document(){}

    public String getBarcode_number() {
        return barcode_number;
    }

    public void setBarcode_number(String barcode_number) {
        this.barcode_number = barcode_number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPermanent_location() {
        return permanent_location;
    }

    public void setPermanent_location(String permanent_location) {
        this.permanent_location = permanent_location;
    }

    public String getCurrently_checked_out_to() {
        return currently_checked_out_to;
    }

    public void setCurrently_checked_out_to(String currently_checked_out_to) {
        this.currently_checked_out_to = currently_checked_out_to;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public String getAdded_by() {
        return added_by;
    }

    public void setAdded_by(String added_by) {
        this.added_by = added_by;
    }

    public String getLast_transaction_id() {
        return last_transaction_id;
    }

    public void setLast_transaction_id(String last_transaction_id) {
        this.last_transaction_id = last_transaction_id;
    }
}
