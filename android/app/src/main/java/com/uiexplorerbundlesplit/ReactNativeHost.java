/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.uiexplorerbundlesplit;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactInstanceManagerBuilder;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.cxxbridge.JSBundleLoader;
import com.facebook.react.devsupport.RedBoxHandler;
import com.facebook.react.uimanager.UIImplementationProvider;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Simple class that holds an instance of {@link ReactInstanceManager}. This can be used in your
 * {@link Application class} (see {@link ReactApplication}), or as a static field.
 */
public abstract class ReactNativeHost {

  private final Application mApplication;
  private
  @Nullable
  ReactInstanceManager mReactInstanceManager;

  protected ReactNativeHost(Application application) {
    mApplication = application;
  }

  /**
   * Get the current {@link ReactInstanceManager} instance, or create one.
   */
  public ReactInstanceManager getReactInstanceManager() {
    if (mReactInstanceManager == null) {
      mReactInstanceManager = createReactInstanceManager();
    }
    return mReactInstanceManager;
  }


  private static final int STATUS_NOT_LOAD = 0;
  private static final int STATUS_LOADING = 1;
  private static final int STATUS_LOADED = 2;


  private int mCoreBundleLoadStatus = STATUS_NOT_LOAD;
  private Handler mUIHandler = new Handler(Looper.getMainLooper());
  private ArrayList<Runnable> mNeedLoadBusinessBundleTasks= new ArrayList<Runnable>();
  private ReactContext mReactContext;

  private Method mMethod_LoadScriptFile;

  public void preLoadCoreBundle(final Runnable callable) {
    getReactInstanceManager();
    if (mCoreBundleLoadStatus == STATUS_LOADING) {
      Log.e("ReactNativeHost", "preLoadCoreBundle is loading, just return.");
      addPendingBusinessBundleTask(callable);
      return;
    }

    if (mCoreBundleLoadStatus == STATUS_LOADED) {
      Log.e("ReactNativeHost", "preLoadCoreBundle has loaded.");
      execPendingBusinessBundleTasks();
      execBusinessBundleTask(callable);
      return;
    }

    mCoreBundleLoadStatus = STATUS_LOADING;

    Log.e("ReactNativeHost", "preLoadCoreBundle...");
    mReactInstanceManager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
      @Override
      public void onReactContextInitialized(ReactContext context) {
        Log.e("ReactNativeHost", "preLoadCoreBundle done.");
        mCoreBundleLoadStatus = STATUS_LOADED;
        mReactContext = context;

        String srcImageLikingPng = "drawable-mdpi/image_liking.png";
        File imageLikingPngFile = new File(mApplication.getFilesDir(), "rn/" + srcImageLikingPng);
        AssetsUtils.copyFile(context, srcImageLikingPng, imageLikingPngFile.getAbsolutePath());

        execPendingBusinessBundleTasks();
        execBusinessBundleTask(callable);
      }
    });
    mReactInstanceManager.createReactContextInBackground();
  }

  public void loadBussinessBundle(final File bundleFile) {
    if (mMethod_LoadScriptFile == null) {
      try {
        mMethod_LoadScriptFile = com.facebook.react.cxxbridge.CatalystInstanceImpl.class.getDeclaredMethod("loadScriptFromFile", new Class[]{String.class, String.class});
        mMethod_LoadScriptFile.setAccessible(true);
      } catch (NoSuchMethodException e) {
        Log.e("ReactNativeHost", "cannot found method: CatalystInstanceImpl.loadScriptFromFile(String, String)", e);
        return;
      }
    }

    CatalystInstance catalystInstance = mReactContext.getCatalystInstance();
    String businessBundlePath = bundleFile.getAbsolutePath();
    Log.e("ReactNativeHost", "loadBussinessBundle " + businessBundlePath + "...");
    try {
      mMethod_LoadScriptFile.invoke(catalystInstance, businessBundlePath, businessBundlePath);
      Log.e("ReactNativeHost", "loadBussinessBundle " + businessBundlePath + " done.");
    } catch (Throwable e) {
      Log.e("ReactNativeHost", "loadBussinessBundle " + businessBundlePath + " error.");
      Log.e("ReactNativeHost", "error invoke method: CatalystInstanceImpl.loadScriptFromFile(String, String)", e);
    }
  }

  private void addPendingBusinessBundleTask(final Runnable task) {
    if (task == null) {
      return;
    }

    mUIHandler.post(new Runnable() {
      @Override
      public void run() {
        mNeedLoadBusinessBundleTasks.add(task);
      }
    });
  }

  private void execPendingBusinessBundleTasks() {
    mUIHandler.post(new Runnable() {
      @Override
      public void run() {
        if (!mNeedLoadBusinessBundleTasks.isEmpty()) {
          for (Runnable task : mNeedLoadBusinessBundleTasks) {
            task.run();
          }
          mNeedLoadBusinessBundleTasks.clear();
        }
      }
    });
  }

  private void execBusinessBundleTask(Runnable task) {
    if (task == null) {
      return;
    }
    mUIHandler.post(task);
  }

  /**
   * Get whether this holder contains a {@link ReactInstanceManager} instance, or not. I.e. if
   * {@link #getReactInstanceManager()} has been called at least once since this object was created
   * or {@link #clear()} was called.
   */
  public boolean hasInstance() {
    return mReactInstanceManager != null;
  }

  /**
   * Destroy the current instance and release the internal reference to it, allowing it to be GCed.
   */
  public void clear() {
    if (mReactInstanceManager != null) {
      mReactInstanceManager.destroy();
      mReactInstanceManager = null;
    }
  }

  protected ReactInstanceManager createReactInstanceManager() {
    ReactInstanceManagerBuilder builder = ReactInstanceManager.builder()
      .setApplication(mApplication)
      .setJSMainModuleName(getJSMainModuleName())
      .setUseDeveloperSupport(getUseDeveloperSupport())
      .setRedBoxHandler(getRedBoxHandler())
      .setUIImplementationProvider(getUIImplementationProvider())
      .setInitialLifecycleState(LifecycleState.BEFORE_CREATE);

    for (ReactPackage reactPackage : getPackages()) {
      builder.addPackage(reactPackage);
    }

    String jsBundleFile = getJSBundleFile();
    if (jsBundleFile != null) {
      builder.setJSBundleFile(jsBundleFile);
    } else {
      builder.setBundleAssetName(Assertions.assertNotNull(getBundleAssetName()));
    }

    File coreBundleFile = new File(mApplication.getFilesDir(), "rn/core.android.bundle");
    if (!coreBundleFile.exists()) {
      Log.e("ReactNativeHost", "copy assets://core.android.bundle to " + coreBundleFile);
      AssetsUtils.copyFile(mApplication, "core.android.bundle", coreBundleFile.getAbsolutePath());
    }
    builder.setJSBundleLoader(JSBundleLoader.createFileLoader(coreBundleFile.getAbsolutePath()));
    Log.e("ReactNativeHost", "set core bundle");
    return builder.build();
  }

  /**
   * Get the {@link RedBoxHandler} to send RedBox-related callbacks to.
   */
  protected
  @Nullable
  RedBoxHandler getRedBoxHandler() {
    return null;
  }

  protected final Application getApplication() {
    return mApplication;
  }

  /**
   * Get the {@link UIImplementationProvider} to use. Override this method if you want to use a
   * custom UI implementation.
   * <p>
   * Note: this is very advanced functionality, in 99% of cases you don't need to override this.
   */
  protected UIImplementationProvider getUIImplementationProvider() {
    return new UIImplementationProvider();
  }

  /**
   * Returns the name of the main module. Determines the URL used to fetch the JS bundle
   * from the packager server. It is only used when dev support is enabled.
   * This is the first file to be executed once the {@link ReactInstanceManager} is created.
   * e.g. "index.android"
   */
  protected String getJSMainModuleName() {
    return "index.android";
  }

  /**
   * Returns a custom path of the bundle file. This is used in cases the bundle should be loaded
   * from a custom path. By default it is loaded from Android assets, from a path specified
   * by {@link #getBundleAssetName}.
   * e.g. "file://sdcard/myapp_cache/index.android.bundle"
   */
  protected
  @Nullable
  String getJSBundleFile() {
    return null;
  }

  /**
   * Returns the name of the bundle in assets. If this is null, and no file path is specified for
   * the bundle, the app will only work with {@code getUseDeveloperSupport} enabled and will
   * always try to load the JS bundle from the packager server.
   * e.g. "index.android.bundle"
   */
  protected
  @Nullable
  String getBundleAssetName() {
    return "index.android.bundle";
  }

  /**
   * Returns whether dev mode should be enabled. This enables e.g. the dev menu.
   */
  public abstract boolean getUseDeveloperSupport();

  /**
   * Returns a list of {@link ReactPackage} used by the app.
   * You'll most likely want to return at least the {@code MainReactPackage}.
   * If your app uses additional views or modules besides the default ones,
   * you'll want to include more packages here.
   */
  protected abstract List<ReactPackage> getPackages();
}
