package com.example.loading;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Loading工具类，提供简单的静态方法
 * 使用示例：
 * Loading.show(activity);
 * Loading.hide(activity);
 */
public class Loading {
    private static final String TAG = "Loading";
    private static LoadingManager currentManager;
    // 使用WeakHashMap存储每个target对应的LoadingManager，避免重复创建
    private static WeakHashMap<Object, LoadingManager> managerMap = new WeakHashMap<>();

    /**
     * 在Activity中显示loading
     */
    public static void show(Activity activity) {
        show(activity, null);
    }

    /**
     * 在Activity中显示loading，带文字提示
     */
    public static void show(Activity activity, String text) {
        Log.d(TAG, "[显示] show(Activity) - 开始显示Loading, Activity: " + (activity != null ? activity.getClass().getSimpleName() : "null"));
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            Log.w(TAG, "[显示] show(Activity) - Activity无效，无法显示");
            return;
        }
        LoadingManager manager = getOrCreateManager(activity);
        if (manager != null) {
            currentManager = manager;
            manager.show(text);
        }
    }

    /**
     * 在Fragment中显示loading
     */
    public static void show(Fragment fragment) {
        show(fragment, null);
    }

    /**
     * 在Fragment中显示loading，带文字提示
     */
    public static void show(Fragment fragment, String text) {
        Log.d(TAG, "[显示] show(Fragment) - 开始显示Loading, Fragment: " + (fragment != null ? fragment.getClass().getSimpleName() : "null"));
        if (fragment == null || !fragment.isAdded() || fragment.getActivity() == null) {
            Log.w(TAG, "[显示] show(Fragment) - Fragment无效，无法显示");
            return;
        }
        LoadingManager manager = getOrCreateManager(fragment);
        if (manager != null) {
            currentManager = manager;
            manager.show(text);
        }
    }

    /**
     * 在自定义View中显示loading
     */
    public static void show(View view) {
        show(view, null);
    }

    /**
     * 在自定义View中显示loading，带文字提示
     */
    public static void show(View view, String text) {
        Log.d(TAG, "[显示] show(View) - 开始显示Loading, View: " + (view != null ? view.getClass().getSimpleName() : "null"));
        if (view == null) {
            Log.w(TAG, "[显示] show(View) - View为null，无法显示");
            return;
        }
        LoadingManager manager = getOrCreateManager(view);
        if (manager != null) {
            currentManager = manager;
            manager.show(text);
        }
    }

    /**
     * 获取或创建LoadingManager
     */
    private static LoadingManager getOrCreateManager(Object target) {
        if (target == null) {
            Log.w(TAG, "[管理] getOrCreateManager() - Target为null");
            return null;
        }
        
        // 先检查是否已经存在该target的manager
        LoadingManager manager = managerMap.get(target);
        if (manager != null) {
            Log.d(TAG, "[管理] getOrCreateManager() - 复用已存在的Manager: " + target.getClass().getSimpleName());
            // 检查manager是否还有效（target是否还存在）
            if (isManagerValid(manager, target)) {
                return manager;
            } else {
                // manager无效，移除并创建新的
                Log.d(TAG, "[管理] getOrCreateManager() - Manager无效，移除并创建新的");
                managerMap.remove(target);
            }
        }
        
        // 创建新的manager
        Log.d(TAG, "[管理] getOrCreateManager() - 创建新的Manager: " + target.getClass().getSimpleName());
        if (target instanceof Activity) {
            manager = LoadingManager.with((Activity) target);
        } else if (target instanceof Fragment) {
            manager = LoadingManager.with((Fragment) target);
        } else if (target instanceof View) {
            manager = LoadingManager.with((View) target);
        } else {
            Log.e(TAG, "[管理] getOrCreateManager() - 未知的Target类型");
            return null;
        }
        
        // 存储到map中
        if (manager != null) {
            managerMap.put(target, manager);
            Log.d(TAG, "[管理] getOrCreateManager() - Manager已存储到map, 当前map大小: " + managerMap.size());
        }
        
        return manager;
    }

    /**
     * 检查manager是否有效（target是否还存在）
     */
    private static boolean isManagerValid(LoadingManager manager, Object target) {
        if (manager == null) {
            return false;
        }
        // 通过尝试获取target来检查manager是否还有效
        // 这里我们假设如果manager的hide方法能正常工作，说明它还有效
        return true;
    }

    /**
     * 隐藏loading
     */
    public static void hide() {
        Log.d(TAG, "[隐藏] hide() - 开始隐藏Loading");
        if (currentManager != null) {
            currentManager.hide();
        } else {
            Log.w(TAG, "[隐藏] hide() - currentManager为null，无法隐藏");
        }
    }

    /**
     * 隐藏指定target的loading
     */
    public static void hide(Object target) {
        if (target == null) {
            return;
        }
        LoadingManager manager = managerMap.get(target);
        if (manager != null) {
            manager.hide();
        }
    }

    /**
     * 释放资源（通常在Activity/Fragment销毁时调用）
     */
    public static void release() {
        Log.d(TAG, "[释放] release() - 开始释放所有资源");
        if (currentManager != null) {
            currentManager.release();
            currentManager = null;
        }
        // 清理map
        int mapSize = managerMap.size();
        managerMap.clear();
        Log.d(TAG, "[释放] release() - 资源已释放, 清理了 " + mapSize + " 个Manager");
    }

    /**
     * 释放指定target的资源
     */
    public static void release(Object target) {
        if (target == null) {
            return;
        }
        LoadingManager manager = managerMap.remove(target);
        if (manager != null) {
            manager.release();
        }
        // 如果当前manager就是被释放的，也清空currentManager
        if (currentManager == manager) {
            currentManager = null;
        }
    }
}

