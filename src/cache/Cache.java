public interface Cache {

  String get(String key);

  void save(String key, String value);
}
