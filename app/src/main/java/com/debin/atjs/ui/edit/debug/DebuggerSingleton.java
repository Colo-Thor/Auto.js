package com.debin.atjs.ui.edit.debug;

import com.stardust.atjs.rhino.debug.Debugger;

import com.debin.atjs.autojs.AutoJs;
import org.mozilla.javascript.ContextFactory;

public class DebuggerSingleton {

    private static Debugger sDebugger = new Debugger(AutoJs.getInstance().getScriptEngineService(), ContextFactory.getGlobal());

    public static Debugger get(){
        return sDebugger;
    }
}
