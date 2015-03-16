package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.query.QueriedDocumentsImpl;
import com.futureprocessing.documentjuggler.read.ReadProcessor;
import com.futureprocessing.documentjuggler.update.RootUpdateBuilder;
import com.futureprocessing.documentjuggler.update.UpdatConsumer;
import com.futureprocessing.documentjuggler.update.UpdateProcessor;
import com.futureprocessing.documentjuggler.update.UpdateResult;
import com.mongodb.*;
import org.bson.types.ObjectId;

import static com.futureprocessing.documentjuggler.versioning.VersionedDocument.TRANSACTION;
import static com.futureprocessing.documentjuggler.versioning.VersionedDocument.VERSION;

public class VersionedQuerriedDocuments<MODEL extends VersionedDocument<MODEL>> extends QueriedDocumentsImpl<MODEL> {

    private final DBCollection collection;
    private final DBCollection archiveCollection;
    private final DBObject query;
    private final UpdateProcessor<MODEL> updateProcessor;

    public VersionedQuerriedDocuments(DBCollection collection, DBCollection archiveCollection, DBObject query,
                                      ReadProcessor<MODEL> readProcessor, UpdateProcessor<MODEL> updateProcessor) {
        super(collection, query, readProcessor, updateProcessor);
        this.collection = collection;
        this.query = query;
        this.updateProcessor = updateProcessor;
        this.archiveCollection = archiveCollection;
    }

    @Override
    public UpdateResult update(UpdatConsumer<MODEL> consumer) {
        final BasicDBObject updateOperation = updateProcessor.process(consumer);
        final ObjectId transactionId = new ObjectId();

        RootUpdateBuilder updateBuilder = new RootUpdateBuilder(updateOperation);
        updateBuilder.set(TRANSACTION, transactionId);
        updateBuilder.inc(VERSION, 1);
        collection.update(query, updateBuilder.getDocument(), false, true);

        copyToArchive(transactionId);

        WriteResult result = removeTransactionFromOriginals(transactionId);
        return new UpdateResult(result);
    }

    private BulkWriteResult copyToArchive(ObjectId transactionId) {
        BulkWriteOperation bulkWriteOperation = archiveCollection.initializeUnorderedBulkOperation();
        try (DBCursor cursor = collection.find(new BasicDBObject(TRANSACTION, transactionId))) {
            while (cursor.hasNext()) {
                BasicDBObject document = (BasicDBObject) cursor.next();
                document.remove("_id");
                document.remove(TRANSACTION);
                bulkWriteOperation.insert(document);
            }
        }

        BulkWriteResult result = bulkWriteOperation.execute();
        return result;
    }

    private WriteResult removeTransactionFromOriginals(ObjectId transactionId) {
        BasicDBObject query = new BasicDBObject(TRANSACTION, transactionId);

        RootUpdateBuilder updateBuilder = new RootUpdateBuilder();
        updateBuilder.unset(TRANSACTION);

        return collection.update(query, updateBuilder.getDocument(), false, true);
    }
}
