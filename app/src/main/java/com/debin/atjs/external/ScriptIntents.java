package com.debin.atjs.external;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.debin.atjs.App;
import com.debin.atjs.autojs.AutoJs;
import com.debin.atjs.model.script.PathChecker;
import com.debin.atjs.tool.AccessibilityServiceTool;
import com.stardust.atjs.execution.ExecutionConfig;
import com.stardust.atjs.script.JavaScriptFileSource;
import com.stardust.atjs.script.ScriptSource;
import com.stardust.atjs.script.SequenceScriptSource;
import com.stardust.atjs.script.StringScriptSource;

import com.debin.atjs.Pref;

import java.io.File;

/**
 * Created by Stardust on 2017/4/1.
 */

public class ScriptIntents {

    private static final String LOG_TAG = ScriptIntents.class.getSimpleName();

    public static final String EXTRA_KEY_PATH = "path";
    public static final String EXTRA_KEY_PRE_EXECUTE_SCRIPT = "script";
    public static final String EXTRA_KEY_LOOP_TIMES = "loop";
    public static final String EXTRA_KEY_LOOP_INTERVAL = "interval";
    public static final String EXTRA_KEY_DELAY = "delay";

    public static boolean isTaskerBundleValid(Bundle bundle) {
        return bundle.containsKey(ScriptIntents.EXTRA_KEY_PATH) || bundle.containsKey(EXTRA_KEY_PRE_EXECUTE_SCRIPT);
    }

    public static boolean handleIntent(Context context, Intent intent) {
        String path = getPath(intent);
        String script = intent.getStringExtra(ScriptIntents.EXTRA_KEY_PRE_EXECUTE_SCRIPT);
        int loopTimes = intent.getIntExtra(EXTRA_KEY_LOOP_TIMES, 1);
        long delay = intent.getLongExtra(EXTRA_KEY_DELAY, 0);
        long interval = intent.getLongExtra(EXTRA_KEY_LOOP_INTERVAL, 0);
        ScriptSource source = null;
        ExecutionConfig config = new ExecutionConfig();
        config.setDelay(delay);
        config.setLoopTimes(loopTimes);
        config.setInterval(interval);
        config.setArgument("intent", intent);
        if (path == null && script != null) {
            source = new StringScriptSource(script);
        } else if (path != null && new PathChecker(context).checkAndToastError(path)) {
            JavaScriptFileSource fileScriptSource = new JavaScriptFileSource(path);
            if (script != null) {
                source = new SequenceScriptSource(fileScriptSource.getName(), new StringScriptSource(script), fileScriptSource);
            } else {
                source = fileScriptSource;
            }
            config.setWorkingDirectory(new File(path).getParent());
        } else {
            config.setWorkingDirectory(Pref.getScriptDirPath());
        }
        if (source == null) {
            return false;
        }

        boolean isAccessibilityServiceEnabled = false;
        if (AccessibilityServiceTool.isAccessibilityServiceEnabled(App.Companion.getApp())) {
            isAccessibilityServiceEnabled = true;
        } else if (Pref.haveAdbPermission(App.Companion.getApp())) {
            isAccessibilityServiceEnabled = AccessibilityServiceTool.enableAccessibilityServiceByAdb();
        } else if (Pref.shouldEnableAccessibilityServiceByRoot()) {
            isAccessibilityServiceEnabled = AccessibilityServiceTool.enableAccessibilityServiceByRoot();
        }

        if (!isAccessibilityServiceEnabled) {
            String msg = "accessibility service not enable";
            Log.d(LOG_TAG, msg);
            AutoJs.getInstance().debugInfo(msg);
            return false;
        }

        AutoJs.getInstance().getScriptEngineService().execute(source, config);
        return true;
    }

    private static String getPath(Intent intent) {
        if (intent.getData() != null && intent.getData().getPath() != null)
            return intent.getData().getPath();
        return intent.getStringExtra(ScriptIntents.EXTRA_KEY_PATH);
    }
}
