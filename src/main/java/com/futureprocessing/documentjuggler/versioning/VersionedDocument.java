package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.annotation.DbField;
import com.futureprocessing.documentjuggler.annotation.ObjectId;

import java.util.Date;

public interface VersionedDocument<MODEL extends VersionedDocument> {

    static final String DOC_ID = "_docId";
    static final String VERSION = "_v";
    static final String DATE = "_date";
    static final String PENDING_ARCHIVE = "_pending_archive";
    static final String TRANSACTION = "_transaction";

    @ObjectId
    @DbField(DOC_ID)
    String getId();

    @ObjectId
    @DbField(DOC_ID)
    MODEL withId(String id);

    @DbField(VERSION)
    int getVersion();

    @DbField(VERSION)
    MODEL withVersion(int version);

    @DbField(DATE)
    Date getDate();
}
