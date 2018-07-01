package com.example.david.journalapp.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by David on 29/06/2018.
 */

public class Entry implements Parcelable {

    private String entryId;
    private String subject;
    private String content;
    private String userEmail;
    private String date;
    private int mData;

    public Entry(String entryId, String subject, String content, String userEmail, String date ) {

        this.entryId = entryId;
        this.content = content;
        this.subject = subject;
        this.date = date;
        this.userEmail = userEmail;

    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setUserId(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getEntryId() {
        return entryId;
    }

    public String getUserId() {
        return userEmail;
    }

    public String getContent() {
        return content;
    }

    public String getSubject() {
        return subject;
    }

    public String getDate() {
        return date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public static final Parcelable.Creator<Entry> CREATOR
            = new Parcelable.Creator<Entry>() {
        public Entry createFromParcel(Parcel in) {
            return new Entry(in);
        }

        public Entry[] newArray(int size) {
            return new Entry[size];
        }
    };

    /** recreate object from parcel */
    private Entry(Parcel in) {
        mData = in.readInt();
    }
}
