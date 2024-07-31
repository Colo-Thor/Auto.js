package com.debin.atjs.ui.log;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;

import com.stardust.atjs.core.console.ConsoleView;
import com.stardust.atjs.core.console.ConsoleImpl;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import com.debin.atjs.R;
import com.debin.atjs.autojs.AutoJs;
import com.debin.atjs.ui.BaseActivity;

@EActivity(R.layout.activity_log)
public class LogActivity extends BaseActivity {

    @ViewById(R.id.console)
    ConsoleView mConsoleView;

    private ConsoleImpl mConsoleImpl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyDayNightMode();
    }

    @AfterViews
    void setupViews() {
        setToolbarAsBack(getString(R.string.text_log));
        mConsoleImpl = AutoJs.getInstance().getGlobalConsole();
        mConsoleView.setConsole(mConsoleImpl);
        mConsoleView.findViewById(R.id.input_container).setVisibility(View.GONE);
    }

    @Click(R.id.fab)
    void clearConsole() {
        mConsoleImpl.clear();
    }
}
