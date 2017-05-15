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