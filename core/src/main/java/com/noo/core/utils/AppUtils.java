package com.noo.core.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.os.EnvironmentCompat;
import android.text.TextUtils;

import java.io.File;
import java.util.List;

/**
 * Application Utils
 *
 * @author Mars.Wong(noneorone@yeah.net) at 2017/2/21 11:09<br/>
 * @since 1.0
 */
public class AppUtils {

    private AppUtils() {
        throw new AssertionError();
    }

    /**
     * whether this process is named with processName
     *
     * @param context
     * @param processName
     * @return <ul>
     * return whether this process is named with processName
     * <li>if context is null, return false</li>
     * <li>if {@link ActivityManager#getRunningAppProcesses()} is null, return false</li>
     * <li>if one process of {@link ActivityManager#getRunningAppProcesses()} is equal to processName, return
     * true, otherwise return false</li>
     * </ul>
     */
    public static boolean isNamedProcess(Context context, String processName) {
        if (context == null) {
            return false;
        }

        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processInfoList = manager.getRunningAppProcesses();
        if (processInfoList == null || processInfoList.size() == 0) {
            return false;
        }

        for (RunningAppProcessInfo processInfo : processInfoList) {
            if (processInfo != null && processInfo.pid == pid
                    && ((processName == null ? processInfo.processName == null : processName.equals(processInfo.processName)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * whether application is in background
     * <ul>
     * <li>need use permission android.permission.GET_TASKS in Manifest.xml</li>
     * </ul>
     *
     * @param context
     * @return if application is in background return true, otherwise return false
     */
    public static boolean isApplicationInBackground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> taskList = am.getRunningTasks(1);
        if (taskList != null && !taskList.isEmpty()) {
            ComponentName topActivity = taskList.get(0).topActivity;
            if (topActivity != null && !topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取manifest中配置的不同类型meta节点下面指定名称对应值
     *
     * @param context
     * @param clz     分为{@link Application} | {@link Service} | {@link BroadcastReceiver} | {@link Activity}
     * @param name
     * @return
     */
    public static final String getMetaData(Context context, Class<?> clz, String name) {
        String value = null;

        try {
            PackageItemInfo info = null;
            PackageManager manager = context.getPackageManager();

            if (clz == Application.class) {
                info = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            } else if (clz == Service.class) {
                info = manager.getServiceInfo(new ComponentName(context, clz), PackageManager.GET_META_DATA);
            } else if (clz == BroadcastReceiver.class) {
                info = manager.getReceiverInfo(new ComponentName(context, clz), PackageManager.GET_META_DATA);
            } else if (clz == Activity.class) {
                if (context instanceof Activity) {
                    boolean isActive = false;
                    Activity activity = (Activity) context;
                    if (activity != null && !activity.isFinishing()) {
                        isActive = true;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        isActive = isActive && !activity.isDestroyed();
                    }
                    if (isActive) {
                        info = manager.getActivityInfo(activity.getComponentName(), PackageManager.GET_META_DATA);
                    }
                }
            }

            value = info.metaData.getString(name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return TextUtils.isEmpty(value) ? "" : value;
    }


    /**
     * 获取应用包信息
     *
     * @return
     */
    public static final PackageInfo getPackageInfo(Context context) {
        return getPackageInfo(context, null);
    }

    /**
     * 获取应用包信息
     *
     * @return
     */
    public static final PackageInfo getPackageInfo(Context context, String packageName) {
        PackageInfo packageInfo = null;

        try {
            if (TextUtils.isEmpty(packageName)) {
                packageName = context.getPackageName();
            }
            PackageManager packageManager = context.getPackageManager();
            packageInfo = packageManager.getPackageInfo(packageName, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return packageInfo;
    }

    /**
     * 获取应用名
     *
     * @return 应用名
     */
    public static final String getAppName(Context context) {
        String appName = null;
        PackageInfo packageInfo = getPackageInfo(context);
        if (packageInfo != null) {
            int labelRes = packageInfo.applicationInfo.labelRes;
            appName = context.getResources().getString(labelRes);
        }
        return appName;
    }

    /**
     * 获取应用包名<br/>
     *
     * @return 应用包名
     */
    public static final String getPackageName(Context context) {
        PackageInfo packageInfo = getPackageInfo(context);
        if (packageInfo != null) {
            return packageInfo.packageName;
        }
        return "";
    }

    /**
     * 检测内存卡是否挂载<br/>
     *
     * @return 挂载true，未挂载false
     */
    public static final boolean isStorageMounted() {
        File storageDirectory = Environment.getExternalStorageDirectory();
        String storageState = EnvironmentCompat.getStorageState(storageDirectory);
        return Environment.MEDIA_MOUNTED.equals(storageState);
    }

    /**
     * 同{@link AppUtils#getLauncher(Context, String)}
     *
     * @param context
     * @return
     */
    public static final ResolveInfo getLauncher(Context context) {
        return getLauncher(context, null);
    }

    /**
     * 获取指定应用包的启动页信息
     *
     * @param context     应用上下文
     * @param packageName 应用包名
     * @return {@link ResolveInfo}
     */
    public static final ResolveInfo getLauncher(Context context, String packageName) {
        ResolveInfo resolveInfo = null;
        PackageInfo packageInfo = getPackageInfo(context, packageName);
        if (packageInfo != null) {
            String packName = packageInfo.packageName;

            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(packName);

            PackageManager pm = context.getApplicationContext().getPackageManager();
            List<ResolveInfo> apps = pm.queryIntentActivities(resolveIntent, 0);
            resolveInfo = apps.iterator().next();
        }
        return resolveInfo;
    }

    /**
     * 通过指定的应用包名打开指定的应用
     *
     * @param context     应用上下文
     * @param packageName 应用包名
     */
    public static final void openApp(Context context, String packageName) {
        ResolveInfo resolveInfo = getLauncher(context, packageName);
        if (resolveInfo != null) {
            String startAppName = resolveInfo.activityInfo.packageName;
            String className = resolveInfo.activityInfo.name;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName(startAppName, className);
            intent.setComponent(cn);
            context.getApplicationContext().startActivity(intent);
        }
    }

    /**
     * 判断手机是否有浏览器
     *
     * @param context 应用上下文
     * @return 若存在则返回true，否则返回false
     */
    public static final boolean hasBrowser(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://"));

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, PackageManager.GET_INTENT_FILTERS);
        return (infos != null && infos.size() > 0);
    }

    /**
     * 判断应用是否置于后台
     *
     * @param context {@link Context}
     * @return true表示置于后台，否则返回false
     */
    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses != null && !appProcesses.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.processName.equals(context.getPackageName()) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 将应用置于前台
     *
     * @param context {@link Context}
     */
    public static void moveTaskToFront(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses != null && !appProcesses.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.processName.equals(context.getPackageName())) {
                    activityManager.moveTaskToFront(appProcess.pid, ActivityManager.MOVE_TASK_WITH_HOME);
                }
            }
        }
    }

    /**
     * 检测应用是否为debug模式
     *
     * @param context {@link Context}
     * @return 若为true则是debug模式，否则不是
     */
    public static boolean isDebuggable(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
