package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.annotation.DbField;
import com.futureprocessing.documentjuggler.annotation.Id;

import java.util.Date;

public interface VersionedDocument<MODEL> {

    public static final String DOC_ID = "docId";
    public static final String VERSION = "v";
    public static final String DATE = "date";
    public static final String CONTENT = "content";

    @Id
    String getId();

    @DbField(DOC_ID)
    String getDocId();

    @DbField(DOC_ID)
    VersionedDocument<MODEL> withDocId(String docId);

    @DbField(VERSION)
    int getVersion();

    @DbField(VERSION)
    VersionedDocument<MODEL> withVersion(int version);

    @DbField(DATE)
    Date getDate();

    @DbField(DATE)
    VersionedDocument<MODEL> withDate(Date date);

    @DbField(CONTENT)
    MODEL getContent();

    @DbField(CONTENT)
    VersionedDocument<MODEL> withContent(MODEL content);
}
