public class Mutation extends BaseQuery {

  public Mutation(OkGraphQL okGraphql, String query) {
    super(okGraphql, "mutation", query);
  }
}
