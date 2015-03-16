package com.futureprocessing.documentjuggler.versioning.integration;

import com.futureprocessing.documentjuggler.BaseRepository;
import com.futureprocessing.documentjuggler.Repository;
import com.futureprocessing.documentjuggler.update.UpdateResult;
import com.futureprocessing.documentjuggler.versioning.example.MovieRepository;
import com.futureprocessing.documentjuggler.versioning.example.model.Movie;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.futureprocessing.documentjuggler.versioning.VersionedDocument.DOC_ID;
import static com.futureprocessing.documentjuggler.versioning.VersionedDocument.PENDING_ARCHIVE;
import static com.futureprocessing.documentjuggler.versioning.VersionedDocument.VERSION;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateIntegrationTest extends BaseIntegrationTest {

    private static DBCollection collection;
    private static DBCollection collection_archive;
    private static MovieRepository movieRepository;
    private static Repository<Movie> archiveRepo;

    @BeforeClass
    public static void init() throws Exception {
        movieRepository = new MovieRepository(db());
        collection = db().getCollection(Movie.COLLECTION);
        collection_archive = db().getCollection(Movie.COLLECTION + "_archive");
        archiveRepo = new BaseRepository<>(collection_archive, Movie.class);
    }

    @Test
    public void shouldUpdateDocumentFoundWithId() {
        //given
        final String originalTitle = "Star Wars";
        final String newTitle = "Armageddon";
        String movieId = movieRepository.insert(movie -> movie.withTitle(originalTitle));

        //when
        movieRepository.update(movieId, 1, movie -> movie.withTitle(newTitle))
                .ensureOneUpdated();

        //then
        Movie second = movieRepository.find(movie -> movie.withId(movieId)).first();
        assertThat(second.getId()).isEqualTo(movieId);
        assertThat(second.getVersion()).isEqualTo(2);
        assertThat(second.getTitle()).isEqualTo(newTitle);
    }

    @Test
    public void shouldAddNewDocumentIntoArchive() {
        //given
        final String originalTitle = "Star Wars";
        final String newTitle = "Armageddon";
        String movieId = movieRepository.insert(movie -> movie.withTitle(originalTitle));

        //when
        movieRepository.update(movieId, 1, movie -> movie.withTitle(newTitle))
                .ensureOneUpdated();

        //then
        BasicDBObject second = (BasicDBObject) collection_archive.findOne(new BasicDBObject(DOC_ID, new ObjectId(movieId)).append(VERSION, 2));
        assertThat(second.getObjectId(DOC_ID)).isEqualTo(new ObjectId(movieId));
        assertThat(second.getInt(VERSION)).isEqualTo(2);
        assertThat(second.getString(Movie.TITLE)).isEqualTo(newTitle);
    }

    @Test
    public void shouldKeepOlderVersionOfDocumentInArchive() {
        //given
        final String originalTitle = "Star Wars";
        final String newTitle = "Armageddon";
        String movieId = movieRepository.insert(movie -> movie.withTitle(originalTitle));

        //when
        movieRepository.update(movieId, 1, movie -> movie.withTitle(newTitle))
                .ensureOneUpdated();

        //then
        BasicDBObject first = (BasicDBObject) collection_archive.findOne(new BasicDBObject(DOC_ID, new ObjectId(movieId)).append(VERSION, 1));
        assertThat(first.getObjectId(DOC_ID)).isEqualTo(new ObjectId(movieId));
        assertThat(first.getInt(VERSION)).isEqualTo(1);
        assertThat(first.getString(Movie.TITLE)).isEqualTo(originalTitle);
    }

    @Test
    public void shouldSkipUpdateDocumentsWithPendingArchiveSet(){
        //given
        final String originalTitle = "Star Wars";
        final String newTitle = "Armageddon";
        final String movieId = movieRepository.insert(movie -> movie.withTitle(originalTitle));
        collection.update(new BasicDBObject(DOC_ID, new ObjectId(movieId)), new BasicDBObject("$set", new BasicDBObject(PENDING_ARCHIVE, true)));

        //when
        UpdateResult result = movieRepository.update(movieId, 1, movie -> movie.withTitle(newTitle));

        //then
        assertThat(result.getAffectedCount()).isEqualTo(0);

        BasicDBObject notModified = (BasicDBObject) collection.findOne(new BasicDBObject(DOC_ID, new ObjectId(movieId)));
        assertThat(notModified.getObjectId(DOC_ID)).isEqualTo(new ObjectId(movieId));
        assertThat(notModified.getInt(VERSION)).isEqualTo(1);
        assertThat(notModified.getString(Movie.TITLE)).isEqualTo(originalTitle);
        assertThat(notModified.getBoolean(Movie.PENDING_ARCHIVE)).isEqualTo(true);

        BasicDBObject shouldNotExist = (BasicDBObject) collection.findOne(new BasicDBObject(DOC_ID, new ObjectId(movieId)).append(VERSION, 2));
        assertThat(shouldNotExist).isNull();
    }
}
