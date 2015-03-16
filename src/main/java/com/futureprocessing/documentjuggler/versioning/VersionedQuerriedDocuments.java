package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.query.QueriedDocumentsImpl;
import com.futureprocessing.documentjuggler.read.ReadProcessor;
import com.futureprocessing.documentjuggler.update.UpdatConsumer;
import com.futureprocessing.documentjuggler.update.UpdateProcessor;
import com.futureprocessing.documentjuggler.update.UpdateResult;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

import static com.futureprocessing.documentjuggler.versioning.VersionedDocument.VERSION;

public class VersionedQuerriedDocuments<MODEL extends VersionedDocument<MODEL>> extends QueriedDocumentsImpl<MODEL>{

    private final DBCollection collection;
    private final DBObject query;
//    private final ReadProcessor<MODEL> readProcessor;
    private final UpdateProcessor<MODEL> updateProcessor;

    public VersionedQuerriedDocuments(DBCollection collection, DBObject query, ReadProcessor<MODEL> readProcessor, UpdateProcessor<MODEL> updateProcessor) {
        super(collection, query, readProcessor, updateProcessor);
        this.collection = collection;
        this.query = query;
//        this.readProcessor = readProcessor;
        this.updateProcessor = updateProcessor;
    }

    @Override
    public UpdateResult update(UpdatConsumer<MODEL> consumer) {
        BasicDBObject document = updateProcessor.process(consumer);

        BasicDBObject original = (BasicDBObject) collection.findOne(query);
        int originalVersion = original.getInt(VERSION);;
        originalVersion++;
        original.remove("_id");
        original.put(VERSION, originalVersion);
        collection.insert(original);

        BasicDBObject newVersionQuery = new BasicDBObject("_id", original.getObjectId("_id"));

        WriteResult result = collection.update(newVersionQuery, document);
        return new UpdateResult(result);
    }
}
