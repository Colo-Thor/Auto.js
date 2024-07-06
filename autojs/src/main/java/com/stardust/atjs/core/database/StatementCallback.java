package com.stardust.atjs.core.database;

public interface StatementCallback {

    void handleEvent(Transaction transaction, DatabaseResultSet resultSet);
}
