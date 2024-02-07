package com.jaime.vertxwebapp.api;

import com.jaime.vertxwebapp.models.WatchList;
import com.jaime.vertxwebapp.response.DbResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.templates.SqlTemplate;
import lombok.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class PutWatchListToDataBaseHandler implements Handler<RoutingContext> {
  private final Pool db;

  @Override
  public void handle(RoutingContext context) {
    final String accountId = context.pathParam("accountId");

    var json = context.body().asJsonObject();
    var watchlist = json.mapTo(WatchList.class);
    var parameterBatch = watchlist.getAssets().stream().map(asset -> {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("account_id", accountId);
        parameters.put("asset", asset.getName());
        return parameters;
      }).collect(Collectors.toList());

    db.withTransaction(client ->
      SqlTemplate.forUpdate(client,
      "DELETE FROM broker.watchlist w where w.account_id = #{account_id}")
      .execute(Collections.singletonMap("account_id", accountId))
      .onFailure(DbResponse.getThrowableHandler(context, "failure to clear for id: " + accountId))
      .compose(clearDone -> addAllForAccount(client,context, accountId, parameterBatch)))
      .onSuccess(assets ->
        context.response()
          .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
          .end());
  }

  private Future<RowSet<WatchList>> addAllForAccount(SqlConnection client, RoutingContext context, String accountId, List<Map<String, Object>> parameterBatch) {
    return SqlTemplate.forUpdate(client,
        "INSERT INTO broker.watchlist VALUES (#{account_id},#{asset})").
      mapTo(WatchList.class)
      .executeBatch(parameterBatch)
      .onFailure(DbResponse.getThrowableHandler(context, "Failure to insert row for account id: " + accountId));
  }
}
