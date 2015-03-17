package com.futureprocessing.documentjuggler.versioning.example.model;

import com.futureprocessing.documentjuggler.annotation.DbField;
import com.futureprocessing.documentjuggler.versioning.VersionedDocument;

public interface Movie extends VersionedDocument<Movie> {

    public static final String COLLECTION = "Movies";
    public static final String TITLE = "title";
    public static final String DIRECTOR = "director";

    @DbField(TITLE)
    String getTitle();

    @DbField(TITLE)
    Movie withTitle(String title);

    @DbField(DIRECTOR)
    String getDirector();

    @DbField(DIRECTOR)
    Movie withDirector(String director);
}
