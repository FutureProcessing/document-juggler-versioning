package com.futureprocessing.documentjuggler.versioning.integration;

import com.futureprocessing.documentjuggler.versioning.example.MovieRepository;
import com.futureprocessing.documentjuggler.versioning.example.model.Movie;
import com.mongodb.DBCollection;
import org.junit.BeforeClass;

public class UpdateIntegrationTest extends BaseIntegrationTest {

    private static DBCollection collection;
    private static MovieRepository movieRepository;

    @BeforeClass
    public static void init() throws Exception {
        movieRepository = new MovieRepository(db());
        collection = db().getCollection(Movie.COLLECTION);
    }

}
