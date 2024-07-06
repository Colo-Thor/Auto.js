package com.stardust.atjs.core.database;

import android.database.SQLException;

public interface StatementErrorCallback {

    boolean handleEvent(Transaction transaction, SQLException error);

}
