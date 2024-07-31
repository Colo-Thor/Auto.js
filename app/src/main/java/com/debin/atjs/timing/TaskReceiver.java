package com.debin.atjs.timing;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.debin.atjs.autojs.AutoJs;
import com.debin.atjs.external.ScriptIntents;

/**
 * Created by Stardust on 2017/11/27.
 */

public class TaskReceiver extends BroadcastReceiver {

    public static final String ACTION_TASK = "com.debin.atjs.action.task";
    public static final String EXTRA_TASK_ID = "task_id";
    private static final String LOG_TAG = "TaskReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "receive intent:" + intent.getAction());
        Log.d(LOG_TAG, "taskInfo [id=" + intent.getLongExtra(TaskReceiver.EXTRA_TASK_ID, -1)
                + ", path=" + intent.getStringExtra(ScriptIntents.EXTRA_KEY_PATH)
                + "]");
        AutoJs.getInstance().debugInfo("receive intent:" + intent.getAction());
        long id = intent.getLongExtra(EXTRA_TASK_ID, -1);
        if (id >= 0) {
            TimedTask task = TimedTaskManager.getInstance().getTimedTask(id);
            if (task == null || task.isExecuted()) {
                AutoJs.getInstance().debugInfo("task[" + id + "] is executed");
                return;
            }
            TimedTaskManager.getInstance().notifyTaskFinished(id);
        }
        ScriptIntents.handleIntent(context, intent);
    }
}
