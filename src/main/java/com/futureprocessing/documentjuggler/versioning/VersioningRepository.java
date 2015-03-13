package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.BaseRepository;
import com.futureprocessing.documentjuggler.Repository;
import com.futureprocessing.documentjuggler.insert.InsertProxy;
import com.futureprocessing.documentjuggler.insert.InserterConsumer;
import com.futureprocessing.documentjuggler.insert.InserterMapper;
import com.futureprocessing.documentjuggler.query.QueriedDocuments;
import com.futureprocessing.documentjuggler.query.QuerierConsumer;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class VersioningRepository<MODEL> implements Repository<MODEL> {

    private BaseRepository<VersionedDocument> repository;

    public VersioningRepository(DBCollection dbCollection, Class<MODEL> modelClass) {
        repository = new BaseRepository<VersionedDocument>(dbCollection, VersionedDocument.class);
    }

    @Override
    public QueriedDocuments<MODEL> find(QuerierConsumer<MODEL> querierConsumer) {
        return repository.find(querierConsumer);
    }

    @Override
    public QueriedDocuments<MODEL> find() {
        return null;
    }

    @Override
    public String insert(InserterConsumer<MODEL> inserterConsumer) {
        Object inserter = InsertProxy.create(this.inserterOperator.getRootClass(), ((InserterMapper) this.inserterOperator.getMapper()).get());
        inserterConsumer.accept(inserter);
        BasicDBObject document = InsertProxy.extract(inserter).getDocument();




        this.dbCollection.insert(new DBObject[]{document});
        return document.getObjectId("_id").toHexString();
    }
}
