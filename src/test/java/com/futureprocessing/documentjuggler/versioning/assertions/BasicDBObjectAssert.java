package com.futureprocessing.documentjuggler.versioning.assertions;

import com.mongodb.BasicDBObject;
import org.assertj.core.api.AbstractAssert;

import static com.futureprocessing.documentjuggler.versioning.VersionedDocument.VERSION;

public class BasicDBObjectAssert extends AbstractAssert<BasicDBObjectAssert, BasicDBObject> {
    protected BasicDBObjectAssert(BasicDBObject actual) {
        super(actual, BasicDBObjectAssert.class);
    }

    public BasicDBObjectAssert hasVersion(int expectedVersion) {
        isNotNull();

        Object versionObj = actual.get(VERSION);
        if (versionObj == null) {
            failWithMessage("Version field was not present");
        }

        int version = (int) versionObj;
        if (version != expectedVersion) {
            failWithMessage("Expected document version to be <%d> but was <%s>", expectedVersion, version);
        }
        return this;
    }
}
