package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.Repository;
import com.futureprocessing.documentjuggler.insert.InsertConsumer;
import com.futureprocessing.documentjuggler.insert.InsertProcessor;
import com.futureprocessing.documentjuggler.query.QueriedDocuments;
import com.futureprocessing.documentjuggler.query.QueryConsumer;
import com.futureprocessing.documentjuggler.query.QueryProcessor;
import com.futureprocessing.documentjuggler.read.ReadProcessor;
import com.futureprocessing.documentjuggler.update.UpdateProcessor;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;
import org.bson.types.ObjectId;

import java.util.Date;

import static com.futureprocessing.documentjuggler.versioning.VersionedDocument.*;

public class VersioningRepository<MODEL extends VersionedDocument<MODEL>> implements Repository<MODEL> {

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

    @Override
    public QueriedDocuments<MODEL> find(QueryConsumer<MODEL> consumer) {
        return new VersionedQuerriedDocuments<>(dbCollection, archiveCollection, queryProcessor.process(consumer), readProcessor, updateProcessor);
    }

    @Override
    public QueriedDocuments<MODEL> find() {
        return new VersionedQuerriedDocuments<>(dbCollection, archiveCollection, null, readProcessor, updateProcessor);
    }

    @Override
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
}
