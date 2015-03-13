package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.Repository;
import com.futureprocessing.documentjuggler.insert.InsertConsumer;
import com.futureprocessing.documentjuggler.insert.InsertProcessor;
import com.futureprocessing.documentjuggler.query.QueriedDocuments;
import com.futureprocessing.documentjuggler.query.QueryConsumer;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;

import java.time.DateTimeException;
import java.util.Date;

public class VersioningRepository<MODEL extends VersionedDocument> implements Repository<MODEL> {

    private final DBCollection dbCollection;
    private final InsertProcessor<MODEL> insertProcessor;

    public VersioningRepository(DBCollection dbCollection, Class<MODEL> modelClass) {
        this.dbCollection = dbCollection;
        insertProcessor = new InsertProcessor<>(modelClass);
    }

    @Override
    public QueriedDocuments<MODEL> find(QueryConsumer<MODEL> consumer) {
        return null;
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
