package com.jaime.vertxwebapp.api;

import com.jaime.vertxwebapp.response.DbResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.templates.SqlTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@Data
public class DeleteWatchListFromDataBaseHandler implements Handler<RoutingContext> {
  private final static Logger LOG = LoggerFactory.getLogger(DeleteWatchListFromDataBaseHandler.class);
  private final Pool db;

  @Override
  public void handle(RoutingContext context) {
    final String accountId = WatchListRestApi.getAccountId(context);
    SqlTemplate.forUpdate(db,
      "DELETE FROM broker.watchlist where account_id = #{account_id}")
      .execute(Collections.singletonMap("account_id", accountId))
      .onFailure(DbResponse.getThrowableHandler(context, "Failure to delete for account id: " + accountId))
      .onSuccess(result -> {
        LOG.debug("Deleted {} rows for accountId {}", result.rowCount(), accountId);
        context.response()
          .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
          .end();
      });
  }
}
