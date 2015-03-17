package com.futureprocessing.documentjuggler.versioning.assertions;

import com.mongodb.BasicDBObject;
import org.assertj.core.api.Assertions;

public class CustomAssertions extends Assertions {

    public static BasicDBObjectAssert assertThat(BasicDBObject dbObject) {
        return new BasicDBObjectAssert(dbObject);
    }

}
