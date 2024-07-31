package com.debin.atjs.autojs;

import android.text.TextUtils;
import android.util.Log;

import com.debin.atjs.App;
import com.stardust.app.GlobalAppContext;
import com.stardust.atjs.execution.ScriptExecution;
import com.stardust.atjs.execution.ScriptExecutionListener;

import com.debin.atjs.R;

import java.util.Calendar;

/**
 * Created by Stardust on 2017/5/3.
 */

public class ScriptExecutionGlobalListener implements ScriptExecutionListener {
    private static final String TAG = ScriptExecutionGlobalListener.class.getSimpleName();
    private static final String ENGINE_TAG_START_TIME = "com.debin.atjs.autojs.Goodbye, World";

    @Override
    public void onStart(ScriptExecution execution) {
        execution.getEngine().setTag(ENGINE_TAG_START_TIME, System.currentTimeMillis());
    }

    @Override
    public void onSuccess(ScriptExecution execution, Object result) {
        onFinish(execution);
    }

    private void onFinish(ScriptExecution execution) {
        Long millis = (Long) execution.getEngine().getTag(ENGINE_TAG_START_TIME);
        if (millis == null) {
            checkNeedExitApp(execution, execution.getSource().toString());
            return;
        }

        double seconds = (System.currentTimeMillis() - millis) / 1000.0;
        AutoJs.getInstance().getScriptEngineService().getGlobalConsole()
                .verbose(GlobalAppContext.getString(R.string.text_execution_finished), execution.getSource().toString(), seconds);
        checkNeedExitApp(execution, execution.getSource().toString());
    }

    private void checkNeedExitApp(ScriptExecution execution, String scriptSource) {
        if (TextUtils.isEmpty(scriptSource) || !scriptSource.contains("Ant-Forest/main.js")) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if (hour == 7 && minute < 25) {
            Log.i(TAG, "checkNeedExitApp startAppExit");
            execution.getEngine().destroy();
            App.Companion.startAppExit();
        }
    }

    @Override
    public void onException(ScriptExecution execution, Throwable e) {
        onFinish(execution);
    }

}
