package com.stardust.atjs.core.database;

public interface TransactionCallback {
    void handleEvent(Transaction transaction);
}
