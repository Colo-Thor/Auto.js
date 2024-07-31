package com.debin.atjs.autojs;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.debin.atjs.BuildConfig;
import com.debin.atjs.tool.AccessibilityServiceTool;
import com.debin.atjs.ui.floating.layoutinspector.LayoutBoundsFloatyWindow;
import com.debin.atjs.ui.floating.layoutinspector.LayoutHierarchyFloatyWindow;
import com.stardust.app.GlobalAppContext;
import com.stardust.atjs.core.console.GlobalConsole;
import com.stardust.atjs.runtime.ScriptRuntime;
import com.stardust.atjs.runtime.accessibility.AccessibilityConfig;
import com.stardust.atjs.runtime.api.AppUtils;
import com.stardust.atjs.runtime.exception.ScriptException;
import com.stardust.atjs.runtime.exception.ScriptInterruptedException;

import com.debin.atjs.Pref;
import com.debin.atjs.R;
import com.debin.atjs.external.fileprovider.AppFileProvider;
import com.debin.atjs.pluginclient.DevPluginService;
import com.debin.atjs.ui.floating.FloatyWindowManger;
import com.debin.atjs.ui.floating.FullScreenFloatyWindow;

import com.debin.atjs.ui.log.LogActivity_;
import com.debin.atjs.ui.settings.SettingsActivity_;

import com.stardust.view.accessibility.AccessibilityService;
import com.stardust.view.accessibility.LayoutInspector;
import com.stardust.view.accessibility.NodeInfo;


/**
 * Created by Stardust on 2017/4/2.
 */

public class AutoJs extends com.stardust.atjs.AutoJs {

    private static AutoJs instance;

    public static AutoJs getInstance() {
        return instance;
    }


    public synchronized static void initInstance(Application application) {
        if (instance != null) {
            return;
        }
        instance = new AutoJs(application);
    }

    private interface LayoutInspectFloatyWindow {
        FullScreenFloatyWindow create(NodeInfo nodeInfo);
    }

    private boolean enableDebugLog = false;

    private BroadcastReceiver mLayoutInspectBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                ensureAccessibilityServiceEnabled();
                String action = intent.getAction();
                if (LayoutBoundsFloatyWindow.class.getName().equals(action)) {
                    capture(LayoutBoundsFloatyWindow::new);
                } else if (LayoutHierarchyFloatyWindow.class.getName().equals(action)) {
                    capture(LayoutHierarchyFloatyWindow::new);
                }
            } catch (Exception e) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    throw e;
                }
            }
        }
    };

    private AutoJs(final Application application) {
        super(application);
        getScriptEngineService().registerGlobalScriptExecutionListener(new ScriptExecutionGlobalListener());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LayoutBoundsFloatyWindow.class.getName());
        intentFilter.addAction(LayoutHierarchyFloatyWindow.class.getName());
        LocalBroadcastManager.getInstance(application).registerReceiver(mLayoutInspectBroadcastReceiver, intentFilter);
        this.enableDebugLog = PreferenceManager.getDefaultSharedPreferences(GlobalAppContext.get())
                .getBoolean(GlobalAppContext.getString(R.string.key_enable_debug_log), false);
        setLogFilePath(Pref.getScriptDirPath(), BuildConfig.DEBUG);
    }

    private void capture(LayoutInspectFloatyWindow window) {
        LayoutInspector inspector = getLayoutInspector();
        LayoutInspector.CaptureAvailableListener listener = new LayoutInspector.CaptureAvailableListener() {
            @Override
            public void onCaptureAvailable(NodeInfo capture) {
                inspector.removeCaptureAvailableListener(this);
                getUiHandler().post(() ->
                        FloatyWindowManger.addWindow(getApplication().getApplicationContext(), window.create(capture))
                );
            }
        };
        inspector.addCaptureAvailableListener(listener);
        if (!inspector.captureCurrentWindow()) {
            inspector.removeCaptureAvailableListener(listener);
        }
    }

    @Override
    protected AppUtils createAppUtils(Context context) {
        return new AppUtils(context, AppFileProvider.AUTHORITY);
    }

    @Override
    protected GlobalConsole createGlobalConsole() {
        return new GlobalConsole(getUiHandler()) {
            @Override
            public String println(int level, CharSequence charSequence) {
                String log = super.println(level, charSequence);
                DevPluginService.getInstance().log(log);
                return log;
            }
        };
    }

    private String checkAccessibilityServiceEnabled() {
        if (AccessibilityService.Companion.getInstance() != null) {
            return null;
        }
        String errorMessage = null;
        if (AccessibilityServiceTool.isAccessibilityServiceEnabled(GlobalAppContext.get())) {
            if (Pref.haveAdbPermission(getApplication())) {
                // 尝试通过ADB权限移除无障碍权限 再重新通过ADB获取权限
                if (AccessibilityServiceTool.disableAccessibilityServiceByAdb()
                        && AccessibilityServiceTool.enableAccessibilityServiceByAdbAndWaitFor( 2000)) {
                    // 重新获取无障碍权限成功
                    return null;
                }
            }
            errorMessage = GlobalAppContext.getString(R.string.text_auto_operate_service_enabled_but_not_running);
        } else {
            if (Pref.haveAdbPermission(getApplication())) {
                if (AccessibilityServiceTool.enableAccessibilityServiceByAdbAndWaitFor( 2000)) {
                    return null;
                }
            }
            if (Pref.shouldEnableAccessibilityServiceByRoot()) {
                if (!AccessibilityServiceTool.enableAccessibilityServiceByRootAndWaitFor(2000)) {
                    errorMessage = GlobalAppContext.getString(R.string.text_enable_accessibility_service_by_root_timeout);
                }
            } else {
                errorMessage = GlobalAppContext.getString(R.string.text_no_accessibility_permission);
            }
        }
        return errorMessage;
    }

    public void ensureAccessibilityServiceEnabled() {
        String errorMessage = checkAccessibilityServiceEnabled();
        if (errorMessage != null) {
            AccessibilityServiceTool.goToAccessibilitySetting();
            throw new ScriptException(errorMessage);
        }
    }

    @Override
    public void waitForAccessibilityServiceEnabled() {
        String errorMessage = checkAccessibilityServiceEnabled();
        if (errorMessage != null) {
            AccessibilityServiceTool.goToAccessibilitySetting();
            if (!AccessibilityService.Companion.waitForEnabled(-1)) {
                throw new ScriptInterruptedException();
            }
        }
    }

    @Override
    protected AccessibilityConfig createAccessibilityConfig() {
        AccessibilityConfig config = super.createAccessibilityConfig();
        return config;
    }

    @Override
    protected ScriptRuntime createRuntime() {
        ScriptRuntime runtime = super.createRuntime();
        runtime.putProperty("class.settings", SettingsActivity_.class);
        runtime.putProperty("class.console", LogActivity_.class);
        runtime.putProperty("broadcast.inspect_layout_bounds", LayoutBoundsFloatyWindow.class.getName());
        runtime.putProperty("broadcast.inspect_layout_hierarchy", LayoutHierarchyFloatyWindow.class.getName());
        return runtime;
    }

    public void debugInfo(String content) {
        if (this.enableDebugLog) {
            AutoJs.getInstance().getGlobalConsole().println(Log.VERBOSE, content);
        }
    }

    public void setDebugEnabled(boolean enableDebugLog) {
        this.enableDebugLog = enableDebugLog;
    }

}
