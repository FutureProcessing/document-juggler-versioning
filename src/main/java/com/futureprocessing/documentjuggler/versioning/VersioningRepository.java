package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.insert.InsertConsumer;
import com.futureprocessing.documentjuggler.insert.InsertProcessor;
import com.futureprocessing.documentjuggler.query.QueryConsumer;
import com.futureprocessing.documentjuggler.query.QueryProcessor;
import com.futureprocessing.documentjuggler.query.ReadQueriedDocuments;
import com.futureprocessing.documentjuggler.read.ReadProcessor;
import com.futureprocessing.documentjuggler.update.*;
import com.mongodb.*;
import org.bson.types.ObjectId;

import java.util.Date;

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

    public ReadQueriedDocuments<MODEL> find(QueryConsumer<MODEL> consumer) {
        return new VersionedQuerriedDocuments<>(dbCollection, archiveCollection, queryProcessor.process(consumer), readProcessor, updateProcessor);
    }

    public ReadQueriedDocuments<MODEL> find() {
        return new VersionedQuerriedDocuments<>(dbCollection, archiveCollection, null, readProcessor, updateProcessor);
    }

    public String insert(InsertConsumer<MODEL> consumer) {
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

    private void unsetPending(ObjectId docId) {

        BasicDBObject query = new BasicDBObject(DOC_ID, docId);
        BasicDBObject unset = new BasicDBObject("$unset", new BasicDBObject(PENDING_ARCHIVE, null));
        WriteResult updateResult = dbCollection.update(query, unset);
        //todo process exceptions (write results)
    }

    public UpdateResult update(final String docIdString, int version, UpdateConsumer<MODEL> consumer) {
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
            if (foundPending == null){
                return new VersionedUpdateResult(0);
            }
            copyToArchive(foundPending);
            removeTransactionFromOriginals(docId);
            return update(docIdString, version, consumer);
        }

        copyToArchive(modified);

        WriteResult result = removeTransactionFromOriginals(docId);
        return new BaseUpdateResult(result);

    }

    private WriteResult copyToArchive(BasicDBObject document) {
        document.remove("_id");
        document.remove(PENDING_ARCHIVE);

        return archiveCollection.insert(document);
    }

    private WriteResult removeTransactionFromOriginals(ObjectId docId) {
        BasicDBObject query = new BasicDBObject(DOC_ID, docId);

        RootUpdateBuilder updateBuilder = new RootUpdateBuilder();
        updateBuilder.unset(PENDING_ARCHIVE);

        return dbCollection.update(query, updateBuilder.getDocument());
    }
}
