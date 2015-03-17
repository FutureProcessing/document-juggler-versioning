package com.futureprocessing.documentjuggler.versioning.integration;

import com.futureprocessing.documentjuggler.update.UpdateResult;
import com.futureprocessing.documentjuggler.versioning.VersioningRepository;
import com.futureprocessing.documentjuggler.versioning.example.MovieRepository;
import com.futureprocessing.documentjuggler.versioning.example.model.Movie;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import static com.futureprocessing.documentjuggler.versioning.VersionedDocument.*;
import static com.futureprocessing.documentjuggler.versioning.example.model.Movie.TITLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Matchers.any;

public class UpdateIntegrationTest extends BaseIntegrationTest {

    private static final String originalTitle = "Star Wars";
    private static final String newTitle = "Armageddon";
    private static final String anotherNewTitle = "Indiana Jones";

    private static DBCollection collection;
    private static DBCollection archiveCollection;
    private static DBCollection spyCollectionArchive;
    private static MovieRepository movieRepository;

    @BeforeClass
    public static void init() throws Exception {
        movieRepository = new MovieRepository(db());
        collection = db().getCollection(Movie.COLLECTION);
        archiveCollection = db().getCollection(Movie.COLLECTION + "_archive");
        spyCollectionArchive = Mockito.spy(archiveCollection);
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
        BasicDBObject second = (BasicDBObject) archiveCollection.findOne(new BasicDBObject(DOC_ID, new ObjectId(movieId)).append(VERSION, 2));
        assertThat(second.getObjectId(DOC_ID)).isEqualTo(new ObjectId(movieId));
        assertThat(second.getInt(VERSION)).isEqualTo(2);
        assertThat(second.getString(TITLE)).isEqualTo(newTitle);
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
        BasicDBObject first = (BasicDBObject) archiveCollection.findOne(new BasicDBObject(DOC_ID, new ObjectId(movieId)).append(VERSION, 1));
        assertThat(first.getObjectId(DOC_ID)).isEqualTo(new ObjectId(movieId));
        assertThat(first.getInt(VERSION)).isEqualTo(1);
        assertThat(first.getString(TITLE)).isEqualTo(originalTitle);
    }

    @Test
    public void shouldSetPendingArchiveWhenInsertToArchiveFailed() {
        //given
        final String movieId = movieRepository.insert(movie -> movie.withTitle(originalTitle));

        BDDMockito.doThrow(new RuntimeException("Power outage!")).when(spyCollectionArchive).insert(any(DBObject.class));
        VersioningRepository<Movie> versioningRepository = new VersioningRepository<>(collection, spyCollectionArchive, Movie.class);

        //when
        try {
            versioningRepository.update(movieId, 1, movie -> movie.withTitle(newTitle)).ensureOneUpdated();
        } catch (Exception e) {
            assertThat(e).hasMessage("Power outage!");
            givenPendingArchiveDocumentBeforeInsertToArchive(() -> {
                BasicDBObject pendingArchiveMovie = (BasicDBObject) collection.findOne(new BasicDBObject(DOC_ID, new ObjectId(movieId)));
                assertThat(pendingArchiveMovie.getInt(VERSION)).isEqualTo(2);
                assertThat(pendingArchiveMovie.getString(TITLE)).isEqualTo(newTitle);
                assertThat(pendingArchiveMovie.getBoolean(PENDING_ARCHIVE)).isTrue();
            });
            return;
        }

        //then
        fail("Should have thrown exception");
    }

    @Test
    public void shouldArchivePendingDocumentNotExistingInArchiveBeforeNextUpdate() {
        //given
        final String movieId = givenPendingArchiveDocumentBeforeInsertToArchive(() -> {
        });

        //when
        UpdateResult result = movieRepository.update(movieId, 2, movie -> movie.withTitle(anotherNewTitle));

        //then
        assertThat(result.getAffectedCount()).isEqualTo(1);

        //assert document Is Modified In Main Collection
        BasicDBObject currentMovie = (BasicDBObject) collection.findOne(new BasicDBObject(DOC_ID, new ObjectId(movieId)));
        assertThat(currentMovie.getObjectId(DOC_ID)).isEqualTo(new ObjectId(movieId));
        assertThat(currentMovie.getInt(VERSION)).isEqualTo(3);
        assertThat(currentMovie.getString(TITLE)).isEqualTo(anotherNewTitle);
        assertThat(currentMovie.get(Movie.PENDING_ARCHIVE)).isNull();

        //asssert that in archive is previous version archived
        BasicDBObject previousArchived = (BasicDBObject) archiveCollection.findOne(new BasicDBObject(DOC_ID, new ObjectId(movieId)).append(VERSION, 2));
        assertThat(previousArchived.getObjectId(DOC_ID)).isEqualTo(new ObjectId(movieId));
        assertThat(previousArchived.getInt(VERSION)).isEqualTo(2);
        assertThat(previousArchived.getString(TITLE)).isEqualTo(newTitle);
        assertThat(previousArchived.get(Movie.PENDING_ARCHIVE)).isNull();

        //asssert that in archive is current version archived
        BasicDBObject currentArchived = (BasicDBObject) archiveCollection.findOne(new BasicDBObject(DOC_ID, new ObjectId(movieId)).append(VERSION, 3));
        assertThat(currentArchived.getObjectId(DOC_ID)).isEqualTo(new ObjectId(movieId));
        assertThat(currentArchived.getInt(VERSION)).isEqualTo(3);
        assertThat(currentArchived.getString(TITLE)).isEqualTo(anotherNewTitle);
        assertThat(currentArchived.get(Movie.PENDING_ARCHIVE)).isNull();

        //assert that in main collection previous version not exists
        BasicDBObject shouldNotExist = (BasicDBObject) collection.findOne(new BasicDBObject(DOC_ID, new ObjectId(movieId)).append(VERSION, 2));
        assertThat(shouldNotExist).isNull();
    }

    private String givenPendingArchiveDocumentBeforeInsertToArchive(Runnable assertions) {
        final String movieId = movieRepository.insert(movie -> movie.withTitle(originalTitle));

        BDDMockito.doThrow(new RuntimeException("Power outage!")).when(spyCollectionArchive).insert(any(DBObject.class));
        VersioningRepository<Movie> versioningRepository = new VersioningRepository<>(collection, spyCollectionArchive, Movie.class);

        try {
            versioningRepository.update(movieId, 1, movie -> movie.withTitle(newTitle)).ensureOneUpdated();
        } catch (Exception e) {
            assertThat(e).hasMessage("Power outage!");
            assertions.run();
            return movieId;
        }
        fail("Should have thrown exception");
        return null;
    }

}
