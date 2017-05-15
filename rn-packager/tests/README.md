
```bash
# 1. 切换到demo目录
cd tests

# 2. 生成core.android.bundle and core.android.manifest.json
node ../bin/rnpackager bundle --entry-file node_modules/react-native/Libraries/react-native/react-native.js --bundle-output ../../android/app/src/main/assets/core.android.bundle --platform android --dev false --assets-dest ../../android/app/src/main/assets --manifest-output ../../android/app/src/main/assets/core.android.manifest.json

# 3. 使用core.android.manifest.json生成app.bundle
node ../bin/rnpackager bundle --entry-file index.js --bundle-output ../../android/app/src/main/assets/HelloWorldApp.android.bundle --platform android --dev false --assets-dest ../../android/app/src/main/assets --manifest-file ../../android/app/src/main/assets/core.android.manifest.json 
```