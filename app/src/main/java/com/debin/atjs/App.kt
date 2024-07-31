package com.debin.atjs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.view.View
import android.widget.ImageView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.multidex.MultiDexApplication
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.debin.atjs.autojs.AutoJs
import com.debin.atjs.autojs.key.GlobalKeyObserver
import com.debin.atjs.external.receiver.DynamicBroadcastReceivers
import com.debin.atjs.theme.ThemeColorManagerCompat
import com.debin.atjs.timing.TimedTaskManager
import com.debin.atjs.timing.TimedTaskScheduler
import com.debin.atjs.tool.AccessibilityServiceTool
import com.stardust.app.GlobalAppContext
import com.stardust.atjs.core.ui.inflater.ImageLoader
import com.stardust.atjs.core.ui.inflater.util.Drawables
import com.stardust.theme.ThemeColor
import com.stardust.view.accessibility.AccessibilityService
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Stardust on 2017/1/27.
 */

class App : MultiDexApplication() {
    lateinit var dynamicBroadcastReceivers: DynamicBroadcastReceivers
        private set

    private var mActivityLifecycleCallbacks: ActivityLifecycleCallbacks? = null

    override fun onCreate() {
        super.onCreate()
        GlobalAppContext.set(this)
        instance = WeakReference(this)
        init()
    }

    private fun init() {
        ThemeColorManagerCompat.init(
            this,
            ThemeColor(
                resources.getColor(R.color.colorPrimary),
                resources.getColor(R.color.colorPrimaryDark),
                resources.getColor(R.color.colorAccent)
            )
        )
        AutoJs.initInstance(this)
        if (Pref.isRunningVolumeControlEnabled()) {
            GlobalKeyObserver.init()
        }
        setupDrawableImageLoader()
        TimedTaskScheduler.init(this)
        initDynamicBroadcastReceivers()
        registerActivityListener(true)
    }

    @SuppressLint("CheckResult")
    private fun initDynamicBroadcastReceivers() {
        dynamicBroadcastReceivers = DynamicBroadcastReceivers(this)
        val localActions = ArrayList<String>()
        val actions = ArrayList<String>()
        TimedTaskManager.getInstance().allIntentTasks
            .filter { task -> task.action != null }
            .doOnComplete {
                if (localActions.isNotEmpty()) {
                    dynamicBroadcastReceivers.register(localActions, true)
                }
                if (actions.isNotEmpty()) {
                    dynamicBroadcastReceivers.register(actions, false)
                }
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
                    Intent(
                        DynamicBroadcastReceivers.ACTION_STARTUP
                    )
                )
            }
            .subscribe({
                if (it.isLocal) {
                    localActions.add(it.action)
                } else {
                    actions.add(it.action)
                }
            }, { it.printStackTrace() })


    }

    private fun setupDrawableImageLoader() {
        Drawables.setDefaultImageLoader(object : ImageLoader {
            override fun loadInto(imageView: ImageView, uri: Uri) {
                Glide.with(imageView)
                    .load(uri)
                    .into(imageView)
            }

            override fun loadIntoBackground(view: View, uri: Uri) {
                Glide.with(view)
                    .load(uri)
                    .into(object : SimpleTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            view.background = resource
                        }
                    })
            }

            override fun load(view: View, uri: Uri): Drawable {
                throw UnsupportedOperationException()
            }

            override fun load(
                view: View,
                uri: Uri,
                drawableCallback: ImageLoader.DrawableCallback
            ) {
                Glide.with(view)
                    .load(uri)
                    .into(object : SimpleTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            drawableCallback.onLoaded(resource)
                        }
                    })
            }

            override fun load(view: View, uri: Uri, bitmapCallback: ImageLoader.BitmapCallback) {
                Glide.with(view)
                    .asBitmap()
                    .load(uri)
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            bitmapCallback.onLoaded(resource)
                        }
                    })
            }
        })
    }

    private fun registerActivityListener(register: Boolean) {
        if (register) {
            mActivityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    /**
                     * 监听到 Activity创建事件 将该 Activity 加入list
                     */
                    pushActivity(activity)
                }

                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                override fun onActivityDestroyed(activity: Activity) {
                    if (mActivities == null || mActivities.isEmpty()) {
                        return
                    }
                    if (mActivities.contains(activity)) {
                        /**
                         * 监听到 Activity销毁事件 将该Activity 从list中移除
                         */
                        popActivity(activity)
                    }
                }
            }
            registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
        } else if (mActivityLifecycleCallbacks != null) {
            unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
        }
    }

    /**
     * @param activity 作用说明 ：添加一个activity到管理里
     */
    fun pushActivity(activity: Activity) {
        mActivities.add(activity)
    }

    /**
     * @param activity 作用说明 ：删除一个activity在管理里
     */
    fun popActivity(activity: Activity) {
        mActivities.remove(activity)
    }

    companion object {

        private val TAG = "App"

        private lateinit var instance: WeakReference<App>

        //维护Activity 的list
        private val mActivities = Collections.synchronizedList(LinkedList<Activity>())

        val app: App
            get() = instance.get()!!

        /**
         * 开始退出App
         */
        fun startAppExit() {
            if (instance.get() != null) {
                instance.get()?.registerActivityListener(false)
                // 停止无障碍服务
                try {
                    if (AccessibilityServiceTool.isAccessibilityServiceEnabled(instance.get())) {
                        AccessibilityService.disable()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            finishAllActivity();
            try {
                Process.killProcess(Process.myPid())
                System.exit(0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * 结束所有Activity
         */
        private fun finishAllActivity() {
            if (mActivities == null || mActivities.isEmpty()) {
                return
            }
            for (activity in mActivities) {
                if (activity != null && !activity.isFinishing) {
                    activity.finish()
                }
            }
            mActivities.clear()
        }
    }


}
