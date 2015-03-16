package com.futureprocessing.documentjuggler.versioning.integration;

import com.futureprocessing.documentjuggler.versioning.example.MovieRepository;
import com.futureprocessing.documentjuggler.versioning.example.model.Movie;
import com.mongodb.DBCollection;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadIntegrationTest extends BaseIntegrationTest {

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
    public void shouldFindDocumentWithId() {
        //given
        String title = "Pocahontas";
        String starWarsId = movieRepository.insert(movie -> movie.withTitle("Star Wars"));
        String pocahontasId = movieRepository.insert(movie -> movie.withTitle(title));

        //when
        Movie pocahontas = movieRepository.find(movie -> movie.withId(pocahontasId)).first();

        //then
        assertThat(starWarsId).isNotNull();
        assertThat(pocahontas.getId()).isEqualTo(pocahontasId);
        assertThat(pocahontas.getTitle()).isEqualTo(title);
        assertThat(pocahontas.getVersion()).isEqualTo(1);
    }

}
