package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.insert.InsertProcessor;
import com.futureprocessing.documentjuggler.query.QueryProcessor;
import com.futureprocessing.documentjuggler.query.ReadQueriedDocuments;
import com.futureprocessing.documentjuggler.read.ReadProcessor;
import com.futureprocessing.documentjuggler.update.BaseUpdateResult;
import com.futureprocessing.documentjuggler.update.RootUpdateBuilder;
import com.futureprocessing.documentjuggler.update.UpdateProcessor;
import com.futureprocessing.documentjuggler.update.UpdateResult;
import com.mongodb.*;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.function.Consumer;

import static com.futureprocessing.documentjuggler.versioning.VersionedDocument.*;

public class VersioningRepository<MODEL extends VersionedDocument<MODEL>> {

    private final DBCollection dbCollection;
    private final DBCollection archiveCollection;


    private final ReadProcessor<MODEL> readProcessor;
    private final QueryProcessor<MODEL> queryProcessor;
    private final InsertProcessor<MODEL> insertProcessor;
    private final UpdateProcessor<MODEL> updateProcessor;

    public VersioningRepository(DBCollection dbCollection, DBCollection archiveCollection, Class<MODEL> modelClass) {

        this.dbCollection = dbCollection;
        this.archiveCollection = archiveCollection;

        this.readProcessor = new ReadProcessor<>(modelClass, dbCollection);
        this.queryProcessor = new QueryProcessor<>(modelClass);
        this.insertProcessor = new InsertProcessor<>(modelClass);
        this.updateProcessor = new UpdateProcessor<>(modelClass);
    }

    public ReadQueriedDocuments<MODEL> find(Consumer<MODEL> consumer) {
        return new VersionedQueriedDocuments<>(dbCollection, queryProcessor.process(consumer), readProcessor, updateProcessor);
    }

    public ReadQueriedDocuments<MODEL> find() {
        return new VersionedQueriedDocuments<>(dbCollection, null, readProcessor, updateProcessor);
    }

    public String insert(Consumer<MODEL> consumer) {
        BasicDBObject document = insertProcessor.process(consumer);

        final ObjectId docId = new ObjectId();

        document.put(DOC_ID, docId);
        document.put(VERSION, 1);
        document.put(DATE, new Date());
        document.put(PENDING_ARCHIVE, true);

        dbCollection.insert(document);

        document.remove(PENDING_ARCHIVE);
        archiveCollection.insert(document);

        unsetPending(docId);

        return docId.toHexString();
    }

    public UpdateResult update(final String docIdString, int version, Consumer<MODEL> consumer) {
        final ObjectId docId = new ObjectId(docIdString);
        final BasicDBObject updateOperation = updateProcessor.process(consumer);

        RootUpdateBuilder updateBuilder = new RootUpdateBuilder(updateOperation);
        updateBuilder.set(PENDING_ARCHIVE, true);
        updateBuilder.inc(VERSION, 1);


        final DBObject query = QueryBuilder.start(DOC_ID).is(docId)
                .and(VERSION).is(version)
                .and(PENDING_ARCHIVE).exists(false).get();

        BasicDBObject modified = (BasicDBObject) dbCollection.findAndModify(query, null, null, false, updateBuilder.getDocument(), true, false);
        if (modified == null) {

            query.removeField(PENDING_ARCHIVE);
            BasicDBObject foundPending = (BasicDBObject) dbCollection.findOne(query);
            if (foundPending == null) {
                return new InvalidVersionUpdateResult();
            }
            copyToArchive(foundPending);
            unsetPending(docId);
            return update(docIdString, version, consumer);
        }

        copyToArchive(modified);

        WriteResult result = unsetPending(docId);
        return new BaseUpdateResult(result);

    }

    private WriteResult copyToArchive(BasicDBObject document) {

        DBObject docIdVersionQuery = QueryBuilder.start(DOC_ID).is(document.getObjectId(DOC_ID))
                .and(VERSION).is(document.getInt(VERSION)).get();

        document.remove("_id");
        document.remove(PENDING_ARCHIVE);

        return archiveCollection.update(docIdVersionQuery, document, true, false);
    }

    private WriteResult unsetPending(ObjectId docId) {
        BasicDBObject query = new BasicDBObject(DOC_ID, docId);

        RootUpdateBuilder updateBuilder = new RootUpdateBuilder();
        updateBuilder.unset(PENDING_ARCHIVE);

        return dbCollection.update(query, updateBuilder.getDocument());
    }
}
