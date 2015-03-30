package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.annotation.AsObjectId;
import com.futureprocessing.documentjuggler.annotation.DbField;

import java.util.Date;

public interface VersionedDocument<MODEL extends VersionedDocument> {

    String DOC_ID = "_docId";
    String VERSION = "_v";
    String DATE = "_date";
    String PENDING_ARCHIVE = "_pending_archive";

    @AsObjectId
    @DbField(DOC_ID)
    String getId();

    @AsObjectId
    @DbField(DOC_ID)
    MODEL withId(String id);

    @DbField(VERSION)
    int getVersion();

    @DbField(VERSION)
    MODEL withVersion(int version);

    @DbField(DATE)
    Date getDate();
}
