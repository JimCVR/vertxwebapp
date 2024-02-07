package com.jaime.vertxwebapp.handlers;

import com.jaime.vertxwebapp.api.WatchListRestApi;
import com.jaime.vertxwebapp.models.WatchList;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class DeleteWatchListHandler implements Handler<RoutingContext> {
  public static final Logger LOG = LoggerFactory.getLogger(DeleteWatchListHandler.class);
  private final Map<UUID, WatchList> watchListPerAccount;

  public DeleteWatchListHandler(final Map <UUID,WatchList> watchListPerAccount) {
    this.watchListPerAccount = watchListPerAccount;
  }

  @Override
  public void handle(RoutingContext context) {
    String accountId = WatchListRestApi.getAccountId(context);
    WatchList watchListRemove = watchListPerAccount.remove(UUID.fromString(accountId));
    LOG.info("DELETED: {}, Remaining: {}", watchListRemove, watchListPerAccount.values());
    context.response()
      .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
      .end(watchListRemove.toJsonObject().toBuffer());
  }
}
