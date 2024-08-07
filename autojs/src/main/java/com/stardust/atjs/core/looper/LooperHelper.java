package com.stardust.atjs.core.looper;

import android.os.Looper;


import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Stardust on 2017/12/27.
 */

public class LooperHelper {

    private static volatile ConcurrentHashMap<Thread, Looper> sLoopers = new ConcurrentHashMap<>();

    public static void prepare() {
        if (Looper.myLooper() == Looper.getMainLooper())
            return;
        if (Looper.myLooper() == null)
            Looper.prepare();
        Looper l = Looper.myLooper();
        if (l != null)
            sLoopers.put(Thread.currentThread(), l);
    }

    public static void quitForThread(Thread thread) {
        if (thread == null) {
            return;
        }
        Looper looper = sLoopers.remove(thread);
        if (looper != null && looper != Looper.getMainLooper())
            looper.quit();
    }
}
