package com.futureprocessing.documentjuggler.versioning.integration;

import com.futureprocessing.documentjuggler.versioning.example.MovieRepository;
import com.futureprocessing.documentjuggler.versioning.example.model.Movie;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.futureprocessing.documentjuggler.versioning.VersionedDocument.*;
import static org.assertj.core.api.Assertions.assertThat;

public class InsertIntegrationTest extends BaseIntegrationTest {

    private static DBCollection collection;
    private static MovieRepository movieRepository;

    @BeforeClass
    public static void init() throws Exception {
        movieRepository = new MovieRepository();
        collection = db().getCollection(Movie.COLLECTION);
    }

    @Test
    public void shouldInsertOneDocument() {
        //given

        //when
        String movieId = movieRepository.insert(movie -> movie.withTitle("Star Wars"));


        //then
        BasicDBObject found = (BasicDBObject) collection.findOne();

        assertThat(found.getString(DOC_ID)).isEqualTo(movieId);
        assertThat(found.get(VERSION)).isEqualTo(1);
        assertThat(found.getDate(DATE)).isNotNull();
        assertThat(found.get(CONTENT)).isNotNull();

        BasicDBObject movie = (BasicDBObject) found.get(CONTENT);
        BasicDBObject expectedContent = new BasicDBObject(Movie.TITLE, "Star Wars");
        assertThat(movie).isEqualTo(expectedContent);
    }

}
