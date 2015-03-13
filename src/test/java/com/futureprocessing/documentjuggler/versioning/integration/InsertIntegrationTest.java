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
        movieRepository = new MovieRepository(db());
        collection = db().getCollection(Movie.COLLECTION);
    }

    @Test
    public void shouldInsertOneDocument() {
        //given

        //when
        String movieId = movieRepository.insert(movie -> movie.withTitle("Star Wars"));

        //then
        BasicDBObject found = (BasicDBObject) collection.findOne();

        assertThat(found.getObjectId(DOC_ID).toHexString()).isEqualTo(movieId);
        assertThat(found.get(VERSION)).isEqualTo(1);
        assertThat(found.getDate(DATE)).isNotNull();

        assertThat(found.getString(Movie.TITLE)).isEqualTo("Star Wars");
    }

    @Test
    public void shouldFindDocumentWithDocId() {
        //given
        String title = "Pocahontas";
        String starWarsId = movieRepository.insert(movie -> movie.withTitle("Star Wars"));
        String pocahontasId = movieRepository.insert(movie -> movie.withTitle(title));

        //when
        Movie pocahontas = movieRepository.find(movie -> movie.withId(pocahontasId)).first();

        //then
        assertThat(pocahontas.getId()).isEqualTo(pocahontasId);
        assertThat(pocahontas.getTitle()).isEqualTo(title);
    }


}
