package com.futureprocessing.documentjuggler.versioning.example.model;

import com.futureprocessing.documentjuggler.annotation.DbField;
import com.futureprocessing.documentjuggler.annotation.Id;
import com.futureprocessing.documentjuggler.versioning.VersionedDocument;

public interface Movie extends VersionedDocument{

    public static final String COLLECTION = "Movies";
    public static final String TITLE = "title";

    @DbField(TITLE)
    String getTitle();

    @DbField(TITLE)
    Movie withTitle(String title);
}
