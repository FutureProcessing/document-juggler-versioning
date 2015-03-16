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
    public void shouldFindDocumentWithId() {
        //given
        String title = "Pocahontas";
        String starWarsId = movieRepository.insert(movie -> movie.withTitle("Star Wars"));
        String pocahontasId = movieRepository.insert(movie -> movie.withTitle(title));

        //when
        Movie pocahontas = movieRepository.find(movie -> movie.withId(pocahontasId)).first();

        //then
        assertThat(pocahontas.getId()).isEqualTo(pocahontasId);
        assertThat(pocahontas.getTitle()).isEqualTo(title);
        assertThat(pocahontas.getVersion()).isEqualTo(1);
    }

    @Test
    public void shouldUpdateDocumentFoudWithId() {
        //given
        final String originalTitle = "Star Wars";
        final String newTitle = "Armageddon";
        String movieId = movieRepository.insert(movie -> movie.withTitle(originalTitle));

        //when
        movieRepository.find(movie -> movie.withId(movieId))
                .update(movie -> movie.withTitle(newTitle))
                .ensureOneUpdated();

        //then
        Movie first = movieRepository.find(movie -> movie.withId(movieId).withVersion(1)).first();
        assertThat(first.getId()).isEqualTo(movieId);
        assertThat(first.getVersion()).isEqualTo(1);
        assertThat(first.getTitle()).isEqualTo(originalTitle);

        Movie second = movieRepository.find(movie -> movie.withId(movieId).withVersion(2)).first();
        assertThat(second.getId()).isEqualTo(movieId);
        assertThat(second.getVersion()).isEqualTo(2);
        assertThat(second.getTitle()).isEqualTo(newTitle);
    }


}
