public class Query extends BaseQuery {

  public Query(OkGraphQL okGraphql, String query) {
    this(okGraphql, "query", query);
  }

  public Query(OkGraphQL okGraphql, String name, String query) {
    super(okGraphql, name, query);
  }
}
