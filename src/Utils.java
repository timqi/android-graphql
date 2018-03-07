import android.content.res.AssetManager;

import com.bixin.bixin_android.global.App;
import com.bixin.bixin_android.global.utils.BxUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {
  private Utils() {
  }

  private static AssetManager mAssetManager;

  static {
    mAssetManager = App.ctx().getAssets();
  }

  public static String renderFromAsset(String fileName) {
    try {
      InputStream is = mAssetManager.open(fileName);
      StringBuilder sb = new StringBuilder();
      if (is != null) {
        BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String str;
        while ((str = in.readLine()) != null) {
          sb.append(str);
        }
        in.close();
      }
      return sb == null ? "" : sb.toString().replace("\"", "\\\"");
    } catch (IOException e) {
      e.printStackTrace();
      return "";
    }
  }

  public static String getCacheKey(String content) {
    return BxUtils.md5(content);
  }
}
