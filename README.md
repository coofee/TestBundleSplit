使用[rn-packager](https://github.com/react-component/rn-packager)拆分react-native的jsbundle(core.android.bundle + business.android.bundle)，然后在程序启动时分步加载拆分后的bundle，以达到热更新目的。


# 0x00 分步加载jsbundle
将rn-packager打包生成的jsbundle+图片资源统一放到assets目录中，应用程序启动时，复制到files目录，只要保持目录结构不变，js就可以正常访问图片资源。故而，如果需要热更新jsbundle和图片资源时，只需要直接更新files目录中的图片和jsbundle文件即可，具体可以看[packager-bundle-split](https://github.com/facebook/react-native/pull/10804)。

* 加载core.android.bundle

```java
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
    // 加载core.android.bundle
    builder.setJSBundleLoader(JSBundleLoader.createFileLoader(coreBundleFile.getAbsolutePath()));
    Log.e("ReactNativeHost", "set core bundle");
    return builder.build();
```

* 加载business.android.bundle

```java
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
```

* 热更新jsbundle/图片
假设从assets复制到files目录后，rn目录结构如下:

```
files
|--rn
  |--core.android.bundle
  |--business.android.bundle
  |--drawable-mdpi/image_liking.png
```

同时使用`require`的方式加载图片，
```javascript
<Image source={require('./image/liking.png')}/>
```

如果需要热更新business.android.bundle或者image_liking.png，直接从服务器下载然后替换files/rn目录对应的资源，然后`recreateReactContextInBackground()`重新加载即可。

# 0x01 如何运行?

```bash
# 1. clone代码
git clone https://github.com/coofee/TestBundleSplit

# 2. 安装rn-packager依赖
cd rn-packager
npm install

# 3. 安装tests例子依赖
cd tests
npm install

# 4. 生成core.android.bundle and core.android.manifest.json
node ../bin/rnpackager bundle --entry-file node_modules/react-native/Libraries/react-native/react-native.js --bundle-output assets/core.android.bundle --platform android --dev false --assets-dest assets --manifest-output assets/core.android.manifest.json

# 5. 使用core.android.manifest.json生成app.bundle
node ../bin/rnpackager bundle --entry-file index.js --bundle-output assets/HelloWorldApp.android.bundle --platform android --dev false --assets-dest assets --manifest-file assets/core.android.manifest.json 

# 6. 复制core.android.bundle和HelloWorldApp.android.bundle到app/src/assets.
cp assets/core.android.bundle ../../android/app/src/main/assets/core.android.bundle

cp assets/HelloWorldApp.android.bundle ../../android/app/src/main/assets/HelloWorldApp.android.bundle

# 7. 安装android app
cd ../../android
# mac/linux执行安装app.
./gradlew :app:installDebug
# windows执行安装app.
./gradlew.bat :app:installDebug

```


# 0x02 Libraries

* [rn-packager](https://github.com/react-component/rn-packager)
* [UIExplorer](https://github.com/facebook/react-native/Examples/UIExplorer)