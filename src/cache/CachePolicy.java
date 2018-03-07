public enum CachePolicy {

  /**
   * Response using cache only
   *
   * Will request for network data when there is no cache exists
   */
  CACHEONLY,

  /**
   * Request for network every time, won't use cache
   */
  NOCACHE,

  /**
   * Using cahce for subscriber, refreshing with network data another time
   *
   * It means that you will have got twice invoked for business logic
   */
  WITHCACHE
}
