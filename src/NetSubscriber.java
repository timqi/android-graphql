import android.content.Intent;
import android.util.Log;

import com.bixin.bixin_android.R;
import com.bixin.bixin_android.extras.rx.exception.GsonNullException;
import com.bixin.bixin_android.extras.rx.exception.HttpException;
import com.bixin.bixin_android.extras.rx.exception.ServerException;
import com.bixin.bixin_android.global.App;
import com.bixin.bixin_android.global.network.SyncDnsIntentService;
import com.bixin.bixin_android.global.utils.ToastUtils;

import java.net.UnknownHostException;

import rx.Subscriber;

public class NetSubscriber<T> extends Subscriber<T> {

  private static final String TAG = "NetSubscriber";

  private NetSubOnNext mSubOnNext;

  private NetSubOnError mSubOnError;

  private NetSubOnComplete mSubOnComplete;

  public interface NetSubOnComplete {
    void onComplete();
  }

  public interface NetSubOnNext<T> {
    void onNext(T bean);
  }

  public interface NetSubOnError {
    void onError(Throwable e);
  }

  public NetSubscriber() {
  }

  public NetSubscriber(NetSubOnComplete subOnComplete) {
    this.mSubOnComplete = subOnComplete;
  }

  public NetSubscriber(NetSubOnNext<T> subOnNext) {
    this.mSubOnNext = subOnNext;
  }

  public NetSubscriber(NetSubOnNext<T> subOnNext, NetSubOnError subOnError) {
    this.mSubOnNext = subOnNext;
    this.mSubOnError = subOnError;
  }

  public NetSubscriber(NetSubOnNext<T> subOnNext,
                       NetSubOnError subOnError,
                       NetSubOnComplete subOnComplete) {
    this.mSubOnNext = subOnNext;
    this.mSubOnError = subOnError;
    this.mSubOnComplete = subOnComplete;
  }

  @Override
  public final void onCompleted() {
    if (mSubOnComplete != null) mSubOnComplete.onComplete();
  }

  @Override
  public final void onError(Throwable e) {
    e.printStackTrace();

    if (e instanceof ServerException) {
      ToastUtils.showShort(e.getMessage());
    } else if (e instanceof HttpException) {
      HttpException httpException = (HttpException) e;
      if (httpException.statusCode() == 401 || httpException.statusCode() == 403) {
        App.signOut(false, null, e.getMessage());
      } else if (httpException.statusCode() == 429) {
        ToastUtils.showShort(App.ctx().getString(R.string.error_too_many_requests));
      } else {
        ToastUtils.showShort(App.ctx().getString(R.string.BggNBAcJCwo));
        App.ctx().startService(new Intent(App.ctx(), SyncDnsIntentService.class));
      }
    } else if (e instanceof GsonNullException) {
//      ToastUtils.showShort(e.getMessage());

    } else if (e instanceof UnknownHostException) {
//      App.networkInfo = null;
//      NetInfoBus.getInstance().post(new NetInfoEvent(false));

      ToastUtils.showShort(App.ctx().getString(R.string.BggNBAcJCwo));
      App.ctx().startService(new Intent(App.ctx(), SyncDnsIntentService.class));
    } else {
      ToastUtils.showShort(App.ctx().getString(R.string.BggNBAcJCwo));
      App.ctx().startService(new Intent(App.ctx(), SyncDnsIntentService.class));
    }

    if (mSubOnError != null) mSubOnError.onError(e);
  }

  @Override
  public final void onNext(T bean) {
    if (mSubOnNext != null) {
      try {
        mSubOnNext.onNext(bean);
      } catch (Exception e) {
        Log.d("NetSubscriber", "Error in subscriber");
        e.printStackTrace();
      }
    }
  }

}
