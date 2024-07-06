package com.stardust.atjs.runtime.api;

import com.stardust.atjs.ScriptEngineService;
import com.stardust.atjs.engine.JavaScriptEngine;
import com.stardust.atjs.execution.ExecutionConfig;
import com.stardust.atjs.execution.ScriptExecution;
import com.stardust.atjs.runtime.ScriptRuntime;
import com.stardust.atjs.script.AutoFileSource;
import com.stardust.atjs.script.JavaScriptFileSource;
import com.stardust.atjs.script.StringScriptSource;

import java.lang.ref.WeakReference;

/**
 * Created by Stardust on 2017/8/4.
 */

public class Engines {

    private ScriptEngineService mEngineService;
    private WeakReference<JavaScriptEngine> mScriptEngine;
    private WeakReference<ScriptRuntime> mScriptRuntime;

    public Engines(ScriptEngineService engineService, ScriptRuntime scriptRuntime) {
        mEngineService = engineService;
        mScriptRuntime = new WeakReference<>(scriptRuntime);
    }

    public ScriptExecution execScript(String name, String script, ExecutionConfig config) {
        return mEngineService.execute(new StringScriptSource(name, script), config);
    }

    public ScriptExecution execScriptFile(String path, ExecutionConfig config) {
        return mEngineService.execute(new JavaScriptFileSource(mScriptRuntime.get().files.path(path)), config);
    }

    public ScriptExecution execAutoFile(String path, ExecutionConfig config) {
        return mEngineService.execute(new AutoFileSource(mScriptRuntime.get().files.path(path)), config);
    }

    public Object all() {
        return mScriptRuntime.get().bridges.toArray(mEngineService.getEngines());
    }

    public int stopAll() {
        return mEngineService.stopAll();
    }

    public void stopAllAndToast() {
        mEngineService.stopAllAndToast();
    }


    public void setCurrentEngine(JavaScriptEngine engine) {
        if (mScriptEngine != null)
            throw new IllegalStateException();
        mScriptEngine = new WeakReference<>(engine);
    }

    public JavaScriptEngine myEngine() {
        return mScriptEngine.get();
    }
}
