import com.bixin.bixin_android.global.network.graphql.cache.Cache;

import okhttp3.OkHttpClient;

public class OkGraphQL {
  private String baseUrl;

  private OkHttpClient okHttpClient;

  private Cache cache;

  public Query queryFrom(String fileName) {
    return this.query(Utils
        .renderFromAsset("graphql/" + fileName));
  }

  public Query query(String query) {
    return new Query(this, query);
  }

  public Mutation mutationFrom(String fileName) {
    return this.mutation(Utils
        .renderFromAsset("graphql/" + fileName));
  }

  public Mutation mutation(String query) {
    return new Mutation(this, query);
  }

  public static class Builder {

    private OkGraphQL okGraphQL;

    public Builder() {
      okGraphQL = new OkGraphQL();
    }

    public OkGraphQL build() {
      return okGraphQL;
    }

    public Builder cache(Cache cache) {
      okGraphQL.cache = cache;
      return this;
    }

    public Builder okClient(OkHttpClient okHttpClient) {
      okGraphQL.okHttpClient = okHttpClient;
      return this;
    }

    public Builder baseUrl(String baseUrl) {
      okGraphQL.baseUrl = baseUrl;
      return this;
    }
  }

  public OkHttpClient getOkHttpClient() {
    return okHttpClient;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public Cache getCache() {
    return cache;
  }
}
