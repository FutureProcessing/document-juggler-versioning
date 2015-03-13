package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.Repository;
import com.futureprocessing.documentjuggler.insert.InsertConsumer;
import com.futureprocessing.documentjuggler.insert.InsertProcessor;
import com.futureprocessing.documentjuggler.query.QueriedDocuments;
import com.futureprocessing.documentjuggler.query.QueriedDocumentsImpl;
import com.futureprocessing.documentjuggler.query.QueryConsumer;
import com.futureprocessing.documentjuggler.query.QueryProcessor;
import com.futureprocessing.documentjuggler.read.ReadProcessor;
import com.futureprocessing.documentjuggler.update.UpdateProcessor;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.bson.types.ObjectId;

import java.util.Date;

public class VersioningRepository<MODEL extends VersionedDocument<MODEL>> implements Repository<MODEL> {

    private final DBCollection dbCollection;

    private final ReadProcessor<MODEL> readProcessor;
    private final QueryProcessor<MODEL> queryProcessor;
    private final InsertProcessor<MODEL> insertProcessor;
    private final UpdateProcessor<MODEL> updateProcessor;

    public VersioningRepository(DBCollection dbCollection, Class<MODEL> modelClass) {
        this.dbCollection = dbCollection;

        readProcessor = new ReadProcessor<>(modelClass, dbCollection);
        queryProcessor = new QueryProcessor<>(modelClass);
        insertProcessor = new InsertProcessor<>(modelClass);
        updateProcessor = new UpdateProcessor<>(modelClass);
    }

    @Override
    public QueriedDocuments<MODEL> find(QueryConsumer<MODEL> consumer) {
        return new QueriedDocumentsImpl<>(dbCollection, queryProcessor.process(consumer), readProcessor, updateProcessor);
    }

    @Override
    public QueriedDocuments<MODEL> find() {
        return null;
    }

    @Override
    public String insert(InsertConsumer<MODEL> consumer) {
        BasicDBObject document = insertProcessor.process(consumer);

        ObjectId docId = new ObjectId();

        document.put(VersionedDocument.DOC_ID, docId);
        document.put(VersionedDocument.VERSION, 1);
        document.put(VersionedDocument.DATE, new Date());

        dbCollection.insert(document);
        return docId.toHexString();
    }
}
