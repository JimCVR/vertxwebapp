package com.jaime.vertxwebapp.handlers;

import com.jaime.vertxwebapp.models.QuoteEntity;
import com.jaime.vertxwebapp.response.DbResponse;
import com.jaime.vertxwebapp.api.GetAssetsFromDatabaseHandler;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.templates.SqlTemplate;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@Data
public class GetQuoteFromDatabaseHandler implements Handler<RoutingContext> {
  private final static Logger LOG = LoggerFactory.getLogger(GetAssetsFromDatabaseHandler.class);
  private final Pool db;

  @Override
  public void handle(RoutingContext context) {
    final String assetParam = context.pathParam("asset");
    LOG.debug("Asset parameter: {}", assetParam);

    SqlTemplate.forQuery(db,
      "SELECT q.asset, q.ask, q.last_price, q.volume from broker.quotes q where asset=#{asset}")
      .mapTo(QuoteEntity.class)
      .execute(Collections.singletonMap("asset", assetParam))
      .onFailure(DbResponse.getThrowableHandler(context,  "Failed to get quote for asset "+ assetParam + "from db!"))
      .onSuccess(quotes -> {
        if(!quotes.iterator().hasNext()) {
          DbResponse.notFoundResponse(context, "quote for asset "+assetParam+" not available!");
          return;
        }
        var response = quotes.iterator().next().toJsonObject();
        LOG.info("Path {} responds with {}", context.normalizedPath(), response.encode());
        context.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
          .end(response.toBuffer());
      });
  }
}

