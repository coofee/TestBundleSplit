
```bash
# 1. 切换到demo目录
cd tests

# 2. 生成core.android.bundle and core.android.manifest.json
node ../bin/rnpackager bundle --entry-file node_modules/react-native/Libraries/react-native/react-native.js --bundle-output assets/core.android.bundle --platform android --dev false --assets-dest assets --manifest-output assets/core.android.manifest.json

# 3. 使用core.android.manifest.json生成app.bundle
node ../bin/rnpackager bundle --entry-file index.js --bundle-output assets/HelloWorldApp.android.bundle --platform android --dev false --assets-dest assets --manifest-file assets/core.android.manifest.json 

# 4. 复制core.android.bundle和HelloWorldApp.android.bundle到app/src/assets.
cp assets/core.android.bundle ../../android/app/src/main/assets/core.android.bundle

cp assets/HelloWorldApp.android.bundle ../../android/app/src/main/assets/HelloWorldApp.android.bundle
```