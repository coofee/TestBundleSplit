package com.uiexplorerbundlesplit;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zhaocongying on 17/5/11.
 */

public class AssetsUtils {

  public static final int BUF_SIZE = 8192;

  public static void copyFile(Context app, String assetFile, String dest) {
    new File(dest).getParentFile().mkdirs();

    BufferedInputStream in = null;
    BufferedOutputStream out = null;
    try {
      in = new BufferedInputStream(app.getAssets().open(assetFile), BUF_SIZE);
      out = new BufferedOutputStream(new FileOutputStream(dest), BUF_SIZE);
      byte[] buf = new byte[BUF_SIZE];
      int len = -1;
      while ((len = in.read(buf, 0, buf.length)) != -1) {
        out.write(buf, 0, len);
      }
      Log.e("AssetsUtils", "copy asset file " + assetFile + " to " + dest + " done.");
    } catch (IOException e) {
      Log.e("AssetsUtils", "error copy asset file " + assetFile + " to " + dest, e);
    } finally {
      closeWithWarning(in);
      closeWithWarning(out);
    }
  }

  private static void closeWithWarning(Closeable c) {
    if (c != null) {
      try {
        c.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
