import com.bixin.bixin_android.R;
import com.bixin.bixin_android.extras.rx.exception.GsonNullException;
import com.bixin.bixin_android.extras.rx.exception.HttpException;
import com.bixin.bixin_android.extras.rx.exception.ServerException;
import com.bixin.bixin_android.global.App;
import com.bixin.bixin_android.global.network.graphql.cache.CachePolicy;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class BaseQuery {
  private CachePolicy cachePolicy = CachePolicy.NOCACHE;

  private final String query;
  private final String prefix;
  private final OkGraphQL okGraphQL;

  private final List<VariableValues> variableValues = new ArrayList<>();
  private final List<String> fragments = new ArrayList<>();

  public BaseQuery(OkGraphQL okGraphql, String prefix, String query) {
    this.okGraphQL = okGraphql;
    this.prefix = prefix;
    this.query = query;
  }

  public BaseQuery variable(String key, Object value) {
    variableValues.add(new VariableValues(key, value));
    return this;
  }

  public BaseQuery fragmentFrom(String fileName) {
    fragments.add(Utils
        .renderFromAsset("graphql/fragments/" + fileName));
    return this;
  }

  public BaseQuery fragment(String fragment) {
    fragments.add(fragment);
    return this;
  }

  public BaseQuery cachePolicy(CachePolicy cachePolicy) {
    this.cachePolicy = cachePolicy;
    return this;
  }

  public String getContent() {
    StringBuilder completeQuery = new StringBuilder();
    StringBuilder realQuery = new StringBuilder();

    completeQuery.append("{\"query\":")
        .append("\"");
    if (prefix != null) {
      completeQuery.append(prefix).append(" ");
    }
    realQuery.append(query);
    for (String fragment : fragments) {
      realQuery.append("fragment ")
          .append(fragment);
    }
    completeQuery.append(realQuery)
        .append("\"")
        .append(",")
        .append("\"variables\":");
    if (variableValues.isEmpty()) {
      completeQuery.append("null");
    } else {
      completeQuery.append("{");
      int size = variableValues.size();
      for (int i = 0; i < size; i++) {
        VariableValues variableValues = this.variableValues.get(i);
        completeQuery.append("\"").append(variableValues.name).append("\":");

        Object value = variableValues.value;
        if (value == null) {
          completeQuery.append("null");
        } else if (value instanceof Number || value instanceof Boolean) {
          completeQuery.append(value.toString());
        } else {
          completeQuery.append("\"").append(value.toString()).append("\"");
        }
        if (i != size - 1) {
          completeQuery.append(",");
        }
      }
      completeQuery.append("}");
    }
    completeQuery.append("}");

    return completeQuery.toString();
  }

  public <T> Observable<T> toObservable(Class<T> clz) {
    return Observable.create((Observable.OnSubscribe<T>) subscriber -> {
      String queryContent = getContent();
      String cacheKey = Utils.getCacheKey(queryContent);

      if (cachePolicy != CachePolicy.NOCACHE) {
        String cachedString = okGraphQL.getCache().get(cacheKey);
        if (cachedString != null) {
          T bean = App.gson.fromJson(cachedString, clz);
          if (bean != null) {
            subscriber.onNext(bean);

            if (cachePolicy == CachePolicy.CACHEONLY) {
              subscriber.onCompleted();
              return;
            }
          }
        }
      }


      Response resp = null;
      try {
        resp = okGraphQL.getOkHttpClient().newCall(
            new Request.Builder()
                .url(okGraphQL.getBaseUrl())
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .post(RequestBody.create(MediaType.parse("application/json"), queryContent))
                .build())
            .execute();
      } catch (IOException e) {
        e.printStackTrace();
        subscriber.onError(new HttpException(e.getMessage(), 0));
        return;
      }

      if (resp.isSuccessful() || resp.code() == 400) {
        // normal situations
        JsonObject respObj = null;
        try {
          respObj = App.gson.fromJson(resp.body().string(), JsonObject.class);
        } catch (IOException e) {
          e.printStackTrace();
          subscriber.onError(new GsonNullException("Error when parse respObj!"));
          return;
        }

        if (respObj.get("errors") != null) {
          JsonObject errObj = respObj.get("errors").getAsJsonArray().get(0).getAsJsonObject();
          String errMessage = errObj.get("message").getAsString();
          subscriber.onError(new ServerException(errMessage));
          return;
        }

        if (respObj.get("data") != null) {
          JsonElement dataElement = respObj.get("data");
          T bean = App.gson.fromJson(dataElement, clz);
          if (bean == null) {
            subscriber.onError(new GsonNullException("Can't deserilize data object."));
          } else {
            // The most normalized condition

            okGraphQL.getCache().save(cacheKey, dataElement.toString());

            subscriber.onNext(bean);
            subscriber.onCompleted();
          }
          return;
        }

      } else if (resp.code() == 401 || resp.code() == 403) {
        // Not authorized, trigger signout flow
        try {
          String errorBody = resp.body().string();
          JsonObject body = new JsonParser().parse(errorBody).getAsJsonObject();
          String msg = body != null ? body.get("error").getAsString() : "";
          subscriber.onError(new HttpException(msg, resp.code()));
        } catch (IOException e) {
          e.printStackTrace();
          subscriber.onError(new HttpException(App.ctx().getString(R.string.signout_prompt),
              resp.code()));
        }
      } else {
        subscriber.onError(new HttpException(resp.message(), resp.code()));
      }
    }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }
}
