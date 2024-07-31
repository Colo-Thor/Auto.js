package com.debin.atjs.external.fileprovider;

import android.content.Context;
import android.net.Uri;

import com.debin.atjs.BuildConfig;

import androidx.core.content.FileProvider;

import java.io.File;

public class AppFileProvider extends FileProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider";

    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context, AUTHORITY, file);
    }
}
