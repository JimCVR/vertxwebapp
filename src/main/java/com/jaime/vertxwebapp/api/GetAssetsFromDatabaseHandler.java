package com.jaime.vertxwebapp.api;

import com.jaime.vertxwebapp.response.DbResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class GetAssetsFromDatabaseHandler implements Handler<RoutingContext> {
  private final Pool db;
  private static final Logger LOG = LoggerFactory.getLogger(GetAssetsFromDatabaseHandler.class);
  @Override
  public void handle(RoutingContext context) {

    db.query("SELECT a.value FROM broker.assets a")
      .execute()
      .onFailure(DbResponse.getThrowableHandler(context, "Failed to get asset from db!")).onSuccess(result ->{
        var response = new JsonArray();
        result.forEach(row -> {
          response.add(row.getValue("value"));
        });
        LOG.info("Path {} responds with {}", context.normalizedPath(), response.encode());
        context.response()
          .putHeader(HttpHeaders.CONTENT_TYPE,HttpHeaderValues.APPLICATION_JSON)
          .setStatusCode(200)
          .end(response.toBuffer());
      });

  }

}
