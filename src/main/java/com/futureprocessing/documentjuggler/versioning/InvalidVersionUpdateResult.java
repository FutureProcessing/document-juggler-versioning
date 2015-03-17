package com.futureprocessing.documentjuggler.versioning;

import com.futureprocessing.documentjuggler.update.UpdateResult;
import com.futureprocessing.documentjuggler.versioning.exception.InvalidVersionException;

public class InvalidVersionUpdateResult implements UpdateResult {
    @Override
    public void ensureOneUpdated() {
        throw new InvalidVersionException();
    }

    @Override
    public void ensureUpdated(int expected) {
        if (expected > 0) {
            throw new InvalidVersionException();
        }
    }

    @Override
    public int getAffectedCount() {
        return 0;
    }
}
