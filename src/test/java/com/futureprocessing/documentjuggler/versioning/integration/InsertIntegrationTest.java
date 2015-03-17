package com.futureprocessing.documentjuggler.versioning.integration;

import com.futureprocessing.documentjuggler.versioning.example.MovieRepository;
import com.futureprocessing.documentjuggler.versioning.example.model.Movie;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.futureprocessing.documentjuggler.versioning.VersionedDocument.*;
import static com.futureprocessing.documentjuggler.versioning.assertions.CustomAssertions.assertThat;


public class InsertIntegrationTest extends BaseIntegrationTest {

    private static DBCollection collection;
    private static DBCollection collection_archive;
    private static MovieRepository movieRepository;

    @BeforeClass
    public static void init() throws Exception {
        movieRepository = new MovieRepository(db());
        collection = db().getCollection(Movie.COLLECTION);
        collection_archive = db().getCollection(Movie.COLLECTION + "_archive");
    }

    @Test
    public void shouldInsertOneDocument() {
        //given

        //when
        String movieId = movieRepository.insert(movie -> movie.withTitle("Star Wars"));

        //then
        BasicDBObject found = (BasicDBObject) collection.findOne();

        assertThat(found.getObjectId(DOC_ID).toHexString()).isEqualTo(movieId);
        assertThat(found).hasVersion(1);
        assertThat(found.getDate(DATE)).isNotNull();

        assertThat(found.getString(Movie.TITLE)).isEqualTo("Star Wars");
    }

    @Test
    public void shouldInsertOneDocumentWithDocId() {
        //given

        //when
        String movieId = movieRepository.insert(movie -> movie.withTitle("Star Wars"));

        //then
        BasicDBObject found = (BasicDBObject) collection.findOne(new BasicDBObject(DOC_ID, new ObjectId(movieId)));

        assertThat(found.getObjectId(DOC_ID).toHexString()).isEqualTo(movieId);
        assertThat(found).hasVersion(1);
        assertThat(found.getDate(DATE)).isNotNull();

        assertThat(found.getString(Movie.TITLE)).isEqualTo("Star Wars");
    }

    @Test
    public void shouldInsertOneDocumentIntoArchive() {
        //given

        //when
        String movieId = movieRepository.insert(movie -> movie.withTitle("Star Wars"));

        //then
        BasicDBObject found = (BasicDBObject) collection_archive.findOne();

        assertThat(found.getObjectId(DOC_ID).toHexString()).isEqualTo(movieId);
        assertThat(found).hasVersion(1);
        assertThat(found.getDate(DATE)).isNotNull();

        assertThat(found.getString(Movie.TITLE)).isEqualTo("Star Wars");
    }

    @Test
    public void shouldInsertOneDocumentIntoArchiveWithDocId() {
        //given

        //when
        String movieId = movieRepository.insert(movie -> movie.withTitle("Star Wars"));

        //then
        BasicDBObject found = (BasicDBObject) collection_archive.findOne(new BasicDBObject(DOC_ID, new ObjectId(movieId)));

        assertThat(found.getObjectId(DOC_ID).toHexString()).isEqualTo(movieId);
        assertThat(found).hasVersion(1);
        assertThat(found.getDate(DATE)).isNotNull();

        assertThat(found.getString(Movie.TITLE)).isEqualTo("Star Wars");
    }


}
