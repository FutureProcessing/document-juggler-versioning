package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.annotation.DbField;
import com.futureprocessing.documentjuggler.annotation.Id;

import java.util.Date;

public interface VersionedDocument {

    public static final String DOC_ID = "_docId";
    public static final String VERSION = "_v";
    public static final String DATE = "_date";

    @Id
    String getId();

    @DbField(DOC_ID)
    String getDocId();

    @DbField(VERSION)
    int getVersion();


    @DbField(DATE)
    Date getDate();

}
