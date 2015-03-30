package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.query.QueriedDocumentsImpl;
import com.futureprocessing.documentjuggler.read.ReadProcessor;
import com.futureprocessing.documentjuggler.update.RemoveResult;
import com.futureprocessing.documentjuggler.update.UpdateProcessor;
import com.futureprocessing.documentjuggler.update.UpdateResult;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.util.function.Consumer;

public class VersionedQueriedDocuments<MODEL extends VersionedDocument<MODEL>> extends QueriedDocumentsImpl<MODEL> {

    public VersionedQueriedDocuments(DBCollection collection, DBObject query,
                                     ReadProcessor<MODEL> readProcessor, UpdateProcessor<MODEL> updateProcessor) {
        super(collection, query, readProcessor, updateProcessor);
    }

    @Override
    public UpdateResult update(
            Consumer<MODEL> consumer) {
        throw new UnsupportedOperationException("This method is not supported in versioned repository.");
    }

    @Override
    public RemoveResult remove() {
        //todo implements
        throw new UnsupportedOperationException("This method is not supported in versioned repository.");
    }
}
