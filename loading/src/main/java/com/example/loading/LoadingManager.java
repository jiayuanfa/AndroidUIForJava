package com.example.loading;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import java.lang.ref.WeakReference;

/**
 * Loading管理器，支持在Activity、Fragment或自定义View中显示loading
 * 自动处理内存泄露和生命周期管理
 */
public class LoadingManager {
    private static final String TAG = "LoadingManager";
    private LoadingView loadingView;
    private WeakReference<Object> targetRef;
    private LifecycleEventObserver lifecycleObserver;
    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks;
    private boolean isLifecycleRegistered = false;

    private LoadingManager(Object target) {
        this.targetRef = new WeakReference<>(target);
        String targetType = target instanceof Activity ? "Activity" : 
                           target instanceof Fragment ? "Fragment" : 
                           target instanceof View ? "View" : "Unknown";
        Log.d(TAG, "[创建] LoadingManager - Target类型: " + targetType + ", Target: " + target.getClass().getSimpleName());
        initLifecycleObserver();
    }

    /**
     * 在Activity中显示loading
     */
    public static LoadingManager with(Activity activity) {
        return new LoadingManager(activity);
    }

    /**
     * 在Fragment中显示loading
     */
    public static LoadingManager with(Fragment fragment) {
        return new LoadingManager(fragment);
    }

    /**
     * 在自定义View中显示loading
     */
    public static LoadingManager with(View view) {
        return new LoadingManager(view);
    }

    /**
     * 显示loading
     */
    public void show() {
        show(null);
    }

    /**
     * 显示loading，带文字提示
     */
    public void show(String text) {
        Object target = targetRef.get();
        if (target == null) {
            Log.w(TAG, "[显示] show() - Target已被回收，无法显示");
            return;
        }

        if (loadingView != null && loadingView.getParent() != null) {
            // 如果已经显示，只更新文字
            Log.d(TAG, "[显示] show() - Loading已显示，只更新文字: " + text);
            if (text != null) {
                loadingView.setLoadingText(text);
            }
            return;
        }

        ViewGroup parentView = getParentView(target);
        if (parentView == null) {
            Log.e(TAG, "[显示] show() - 无法获取父容器View");
            return;
        }

        Log.d(TAG, "[显示] show() - 开始显示Loading, 文字: " + text + ", 父容器: " + parentView.getClass().getSimpleName());

        // 创建loading view
        if (loadingView == null) {
            loadingView = new LoadingView(parentView.getContext());
            Log.d(TAG, "[显示] show() - 创建新的LoadingView");
        } else {
            // 如果loadingView已存在但被移除过，重置尺寸计算标志
            loadingView.resetSizeCalculation();
            Log.d(TAG, "[显示] show() - 复用已存在的LoadingView，重置尺寸计算");
        }

        if (text != null) {
            loadingView.setLoadingText(text);
        }

        // 添加到父容器
        parentView.addView(loadingView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        Log.d(TAG, "[显示] show() - LoadingView已添加到父容器");

        // 强制重新计算尺寸，确保每次显示时尺寸一致
        loadingView.post(() -> {
            if (loadingView != null && loadingView.getParent() != null) {
                loadingView.forceRecalculateSize();
                Log.d(TAG, "[显示] show() - 尺寸计算完成");
            }
        });

        // 注册生命周期监听（避免重复注册）
        if (!isLifecycleRegistered) {
            registerLifecycleListener(target);
            isLifecycleRegistered = true;
            Log.d(TAG, "[显示] show() - 生命周期监听已注册");
        } else {
            Log.d(TAG, "[显示] show() - 生命周期监听已存在，跳过注册");
        }
    }

    /**
     * 隐藏loading
     */
    public void hide() {
        if (loadingView != null && loadingView.getParent() != null) {
            ViewGroup parent = (ViewGroup) loadingView.getParent();
            Log.d(TAG, "[隐藏] hide() - 开始隐藏Loading, 父容器: " + parent.getClass().getSimpleName());
            parent.removeView(loadingView);
            Log.d(TAG, "[隐藏] hide() - LoadingView已从父容器移除");
        } else {
            Log.d(TAG, "[隐藏] hide() - LoadingView不存在或未添加到父容器");
        }
        // 注意：不在这里取消注册生命周期监听，让生命周期自动处理
        // loadingView对象保留，以便下次show()时复用
    }

    /**
     * 获取父容器View
     */
    private ViewGroup getParentView(Object target) {
        if (target instanceof Activity) {
            Activity activity = (Activity) target;
            Window window = activity.getWindow();
            if (window != null) {
                View decorView = window.getDecorView();
                if (decorView instanceof ViewGroup) {
                    ViewGroup contentView = decorView.findViewById(android.R.id.content);
                    return contentView != null ? contentView : (ViewGroup) decorView;
                }
            }
        } else if (target instanceof Fragment) {
            Fragment fragment = (Fragment) target;
            View view = fragment.getView();
            if (view != null) {
                // Fragment的view本身如果是ViewGroup，直接使用
                if (view instanceof ViewGroup) {
                    return (ViewGroup) view;
                }
                // 否则使用父容器
                if (view.getParent() instanceof ViewGroup) {
                    return (ViewGroup) view.getParent();
                }
            }
        } else if (target instanceof View) {
            View view = (View) target;
            // 如果target本身就是ViewGroup，直接使用它作为父容器
            if (view instanceof ViewGroup) {
                return (ViewGroup) view;
            }
            // 否则使用它的父容器
            if (view.getParent() instanceof ViewGroup) {
                return (ViewGroup) view.getParent();
            }
        }
        return null;
    }

    /**
     * 初始化生命周期观察者
     */
    private void initLifecycleObserver() {
        lifecycleObserver = (source, event) -> {
            Log.d(TAG, "[生命周期] 生命周期事件: " + event.name() + ", Source: " + source.getClass().getSimpleName());
            if (event == Lifecycle.Event.ON_PAUSE || 
                event == Lifecycle.Event.ON_STOP || 
                event == Lifecycle.Event.ON_DESTROY) {
                Log.d(TAG, "[生命周期] 自动隐藏Loading - 事件: " + event.name());
                hide();
            }
        };

        activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                Object target = targetRef.get();
                if (target == activity) {
                    Log.d(TAG, "[生命周期] onActivityPaused() - 自动隐藏Loading: " + activity.getClass().getSimpleName());
                    hide();
                }
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                Object target = targetRef.get();
                if (target == activity) {
                    Log.d(TAG, "[生命周期] onActivityStopped() - 自动隐藏Loading: " + activity.getClass().getSimpleName());
                    hide();
                }
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                Object target = targetRef.get();
                if (target == activity) {
                    Log.d(TAG, "[生命周期] onActivityDestroyed() - 自动隐藏并清理Loading: " + activity.getClass().getSimpleName());
                    hide();
                    unregisterLifecycleListener();
                }
            }

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(@NonNull Activity activity) {}

            @Override
            public void onActivityResumed(@NonNull Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
        };
    }

    /**
     * 注册生命周期监听
     */
    private void registerLifecycleListener(Object target) {
        if (target instanceof LifecycleOwner) {
            LifecycleOwner lifecycleOwner = (LifecycleOwner) target;
            if (lifecycleObserver != null) {
                lifecycleOwner.getLifecycle().addObserver(lifecycleObserver);
                Log.d(TAG, "[生命周期] registerLifecycleListener() - 注册LifecycleOwner监听: " + target.getClass().getSimpleName());
            }
        } else if (target instanceof Activity) {
            Activity activity = (Activity) target;
            Application application = activity.getApplication();
            if (application != null && activityLifecycleCallbacks != null) {
                application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
                Log.d(TAG, "[生命周期] registerLifecycleListener() - 注册Activity生命周期回调: " + activity.getClass().getSimpleName());
            }
        } else if (target instanceof Fragment) {
            Fragment fragment = (Fragment) target;
            // Fragment本身也是LifecycleOwner
            if (fragment instanceof LifecycleOwner && lifecycleObserver != null) {
                ((LifecycleOwner) fragment).getLifecycle().addObserver(lifecycleObserver);
                Log.d(TAG, "[生命周期] registerLifecycleListener() - 注册Fragment生命周期监听: " + fragment.getClass().getSimpleName());
            }
        }
    }

    /**
     * 取消注册生命周期监听
     */
    private void unregisterLifecycleListener() {
        if (!isLifecycleRegistered) {
            Log.d(TAG, "[生命周期] unregisterLifecycleListener() - 生命周期监听未注册，跳过");
            return;
        }

        Object target = targetRef.get();
        if (target == null) {
            Log.d(TAG, "[生命周期] unregisterLifecycleListener() - Target已被回收");
            isLifecycleRegistered = false;
            return;
        }

        if (target instanceof LifecycleOwner && lifecycleObserver != null) {
            LifecycleOwner lifecycleOwner = (LifecycleOwner) target;
            lifecycleOwner.getLifecycle().removeObserver(lifecycleObserver);
            Log.d(TAG, "[生命周期] unregisterLifecycleListener() - 取消注册LifecycleOwner监听: " + target.getClass().getSimpleName());
        } else if (target instanceof Activity && activityLifecycleCallbacks != null) {
            Activity activity = (Activity) target;
            Application application = activity.getApplication();
            if (application != null) {
                application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
                Log.d(TAG, "[生命周期] unregisterLifecycleListener() - 取消注册Activity生命周期回调: " + activity.getClass().getSimpleName());
            }
        } else if (target instanceof Fragment) {
            Fragment fragment = (Fragment) target;
            if (fragment instanceof LifecycleOwner && lifecycleObserver != null) {
                ((LifecycleOwner) fragment).getLifecycle().removeObserver(lifecycleObserver);
                Log.d(TAG, "[生命周期] unregisterLifecycleListener() - 取消注册Fragment生命周期监听: " + fragment.getClass().getSimpleName());
            }
        }
        
        isLifecycleRegistered = false;
    }

    /**
     * 释放资源
     */
    public void release() {
        Log.d(TAG, "[释放] release() - 开始释放LoadingManager资源");
        hide();
        unregisterLifecycleListener();
        isLifecycleRegistered = false;
        targetRef.clear();
        loadingView = null;
        lifecycleObserver = null;
        activityLifecycleCallbacks = null;
        Log.d(TAG, "[释放] release() - LoadingManager资源已释放");
    }
}

