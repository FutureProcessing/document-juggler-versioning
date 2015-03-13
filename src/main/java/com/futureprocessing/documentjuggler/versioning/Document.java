package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.annotation.DbField;
import com.futureprocessing.documentjuggler.annotation.Id;

import java.util.Date;

public class Document<MODEL> {

    private final String id;
    private final String docId;
    private final int revision;
    private final Date date;
    private final MODEL content;

    public Document(String id, String docId, int revision, Date date, MODEL content) {
        this.id = id;
        this.docId = docId;
        this.revision = revision;
        this.date = date;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getDocId() {
        return docId;
    }

    public int getRevision() {
        return revision;
    }

    public Date getDate() {
        return date;
    }

    public MODEL getContent() {
        return content;
    }
}
