package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.query.QueriedDocumentsImpl;
import com.futureprocessing.documentjuggler.read.ReadProcessor;
import com.futureprocessing.documentjuggler.update.RootUpdateBuilder;
import com.futureprocessing.documentjuggler.update.UpdatConsumer;
import com.futureprocessing.documentjuggler.update.UpdateProcessor;
import com.futureprocessing.documentjuggler.update.UpdateResult;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import org.bson.types.ObjectId;

import static com.futureprocessing.documentjuggler.versioning.VersionedDocument.PENDING_ARCHIVE;
import static com.futureprocessing.documentjuggler.versioning.VersionedDocument.VERSION;

public class VersionedQuerriedDocuments<MODEL extends VersionedDocument<MODEL>> extends QueriedDocumentsImpl<MODEL> {

    private final DBCollection collection;
    private final DBCollection archiveCollection;
    private final DBObject query;
    private final UpdateProcessor<MODEL> updateProcessor;

    public VersionedQuerriedDocuments(DBCollection collection, DBCollection archiveCollection, DBObject query, ReadProcessor<MODEL> readProcessor, UpdateProcessor<MODEL> updateProcessor) {
        super(collection, query, readProcessor, updateProcessor);
        this.collection = collection;
        this.archiveCollection = archiveCollection;
        this.query = query;
        this.updateProcessor = updateProcessor;
    }

    @Override
    public UpdateResult update(UpdatConsumer<MODEL> consumer) {
        BasicDBObject document = updateProcessor.process(consumer);

        RootUpdateBuilder updateBuilder = new RootUpdateBuilder(document);
        updateBuilder.set(PENDING_ARCHIVE, true);
        updateBuilder.inc(VERSION, 1);

        BasicDBObject modified = (BasicDBObject) collection.findAndModify(query, null, null, false, updateBuilder.getDocument(), true, false);
        final ObjectId originalId = modified.getObjectId("_id");
        modified.remove("_id");
        modified.remove(PENDING_ARCHIVE);

        archiveCollection.insert(modified);

        updateBuilder = new RootUpdateBuilder();
        updateBuilder.unset(PENDING_ARCHIVE);

        WriteResult result = collection.update(new BasicDBObject("_id", originalId), updateBuilder.getDocument());
        return new UpdateResult(result);
    }
}
