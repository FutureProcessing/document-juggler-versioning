package com.futureprocessing.documentjuggler.versioning.example.model;

import com.futureprocessing.documentjuggler.annotation.DbField;
import com.futureprocessing.documentjuggler.annotation.Id;

public interface Movie {

    public static final String COLLECTION = "Movies";
    public static final String TITLE = "title";

    @Id   //todo this should return docId
    String getId();

    @DbField(TITLE)
    String getTitle();

    @DbField(TITLE)
    Movie withTitle(String title);
}
