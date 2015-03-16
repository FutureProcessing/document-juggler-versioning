package com.futureprocessing.documentjuggler.versioning.example;

import com.futureprocessing.documentjuggler.versioning.VersioningRepository;
import com.futureprocessing.documentjuggler.versioning.example.model.Movie;
import com.mongodb.DB;

public class MovieRepository extends VersioningRepository<Movie> {

    public MovieRepository(DB db) {
        super(db.getCollection(Movie.COLLECTION), db.getCollection(Movie.COLLECTION + "_archive"), Movie.class);
    }
}
