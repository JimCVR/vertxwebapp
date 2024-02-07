package com.jaime.vertxwebapp.api;

import com.jaime.vertxwebapp.handlers.DeleteWatchListHandler;
import com.jaime.vertxwebapp.handlers.GetWatchListHandler;
import com.jaime.vertxwebapp.handlers.PutWatchListHandler;
import com.jaime.vertxwebapp.models.WatchList;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;

public class WatchListRestApi {

  static final Logger LOG = LoggerFactory.getLogger(WatchListRestApi.class);
  public static void attach(Router router, Pool db) {
    final HashMap<UUID, WatchList> watchListPerAccount = new HashMap<>();
    String path = "/account/watchlist/:accountId";
    router.get(path).handler(new GetWatchListHandler(watchListPerAccount));
    router.put(path).handler(new PutWatchListHandler(watchListPerAccount));
    router.delete(path).handler(new DeleteWatchListHandler(watchListPerAccount));
    final String pgPath = "/pg/watchlist/:accountId";
    router.get(pgPath).handler(new GetWatchListFromDatabaseHandler(db));
    router.put(pgPath).handler(new PutWatchListToDataBaseHandler(db));
    router.delete(pgPath).handler(new DeleteWatchListFromDataBaseHandler(db));

  }

  public static String getAccountId(RoutingContext context) {
    var accountId = context.pathParam("accountId");
    LOG.debug("{} for account {}", context.normalizedPath(), accountId);
    return accountId;
  }
}
