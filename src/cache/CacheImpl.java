import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class CacheImpl implements Cache {

  DiskLruCache diskLruCache;

  public CacheImpl(File directory, long maxSize) {
    try {
      if (!directory.exists()) {
        directory.mkdirs();
      }
      diskLruCache = DiskLruCache.open(directory, 20180307, 1, maxSize);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String get(String key) {
    try {
      DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
      if (snapshot != null) {
        InputStream is = snapshot.getInputStream(0);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
          sb.append(line);
        }
        return sb.toString();
      } else {
        return null;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void save(String key, String value) {
    try {
      DiskLruCache.Editor editor = diskLruCache.edit(key);
      OutputStream os = editor.newOutputStream(0);
      os.write(value.getBytes());
      editor.commit();
      diskLruCache.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
