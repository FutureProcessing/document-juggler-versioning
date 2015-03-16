package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.query.QueriedDocumentsImpl;
import com.futureprocessing.documentjuggler.read.ReadProcessor;
import com.futureprocessing.documentjuggler.update.RemoveResult;
import com.futureprocessing.documentjuggler.update.UpdateConsumer;
import com.futureprocessing.documentjuggler.update.UpdateProcessor;
import com.futureprocessing.documentjuggler.update.UpdateResult;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

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
    public UpdateResult update(UpdateConsumer<MODEL> consumer) {
        throw new UnsupportedOperationException("This method is not suppported in versioned repository.");
    }

    @Override
    public RemoveResult remove() {
        //todo implements
        throw new UnsupportedOperationException("This method is not suppported in versioned repository.");
    }
}
