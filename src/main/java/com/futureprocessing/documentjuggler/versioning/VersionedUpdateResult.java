package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.exception.InvalidNumberOfDocumentsAffected;
import com.futureprocessing.documentjuggler.update.UpdateResult;

public class VersionedUpdateResult implements UpdateResult {
    private final int affectedCount;

    public VersionedUpdateResult(int affectedCount) {
        this.affectedCount = affectedCount;
    }

    @Override
    public int getAffectedCount() {
        return affectedCount;
    }

    @Override
    public void ensureOneUpdated() {
        ensureUpdated(1);
    }

    @Override
    public void ensureUpdated(int expected) {
        if (affectedCount != expected) {
            throw new InvalidNumberOfDocumentsAffected(affectedCount, expected);
        }
    }
}
