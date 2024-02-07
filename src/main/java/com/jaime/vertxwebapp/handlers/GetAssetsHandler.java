package com.jaime.vertxwebapp.handlers;

import com.jaime.vertxwebapp.api.AssetsRestApi;
import com.jaime.vertxwebapp.models.Asset;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;

public class GetAssetsHandler implements Handler <RoutingContext> {

  @Override
  public void handle(RoutingContext context) {
    final JsonArray response = new JsonArray();
    AssetsRestApi.ASSETS.stream().map(asset -> new Asset(asset)).forEach(response::add);
    AssetsRestApi.LOG.info("Path {} responds with {}", context.normalizedPath(), response.encode());
    context.response()
      .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
      .end(response.toBuffer());
  }
}
