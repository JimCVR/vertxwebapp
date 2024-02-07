package com.jaime.vertxwebapp.api;

import com.jaime.vertxwebapp.models.Asset;
import com.jaime.vertxwebapp.models.WatchList;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TestWatchListRestApi extends AbstractRestApiTest{

  static final Logger LOG = LoggerFactory.getLogger(TestWatchListRestApi.class);

  @Test
  void addsAndReturnsAllWatchList(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(TEST_SERVER_PORT));
    UUID accountId = UUID.randomUUID();
    client.put("/account/watchlist/"+ accountId)
      .sendJsonObject(getBody())
      .onComplete(testContext.succeeding(response -> {
        var json = response.bodyAsJsonObject();
        LOG.info("Response PUT: {}", json);
        assertEquals("{\"assets\":[{\"name\":\"AMZN\"},{\"name\":\"TSLA\"}]}", json.encode());
        assertEquals(200, response.statusCode());
        assertEquals(HttpHeaderValues.APPLICATION_JSON.toString(),
          response.getHeader(HttpHeaders.CONTENT_TYPE.toString()));
        testContext.completeNow();
      })).compose(next -> {
        client.get("/account/watchlist/"+accountId)
          .send()
          .onComplete(testContext.succeeding(response -> {
            var json = response.bodyAsJsonObject();
            LOG.info("Response GET: {}", json);
            assertEquals("{\"assets\":[{\"name\":\"AMZN\"},{\"name\":\"TSLA\"}]}", json.encode());
            assertEquals(200, response.statusCode());
            assertEquals(HttpHeaderValues.APPLICATION_JSON.toString(),
              response.getHeader(HttpHeaders.CONTENT_TYPE.toString()));
            testContext.completeNow();
          }));
        return Future.succeededFuture();
      });
  }

  @Test
  void removesWatchList(Vertx vertx, VertxTestContext testContext) throws Throwable{
    WebClient client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(TEST_SERVER_PORT));
    UUID accountId = UUID.randomUUID();
    client.put("/account/watchlist/"+ accountId)
      .sendJsonObject(getBody())
      .onComplete(testContext.succeeding(response -> {
          var json = response.bodyAsJsonObject();
          LOG.info("Response PUT: {}", json);
          assertEquals("{\"assets\":[{\"name\":\"AMZN\"},{\"name\":\"TSLA\"}]}", json.encode());
          assertEquals(200, response.statusCode());
          assertEquals(HttpHeaderValues.APPLICATION_JSON.toString(),
            response.getHeader(HttpHeaders.CONTENT_TYPE.toString()));
        })).compose( next -> {
          client.delete("/account/watchlist/"+accountId)
            .send()
            .onComplete( testContext.succeeding( response -> {
              var json = response.bodyAsJsonObject();
              LOG.info("Response DELETE {}", json);
              assertEquals("{\"assets\":[{\"name\":\"AMZN\"},{\"name\":\"TSLA\"}]}", json.encode());
              assertEquals(200, response.statusCode());
              assertEquals(HttpHeaderValues.APPLICATION_JSON.toString(),
                response.getHeader(HttpHeaders.CONTENT_TYPE.toString()));
              testContext.completeNow();
            }));
          return Future.succeededFuture();
        });

  }

  private static JsonObject getBody() {
    return new WatchList(Arrays
      .asList(
        new Asset("AMZN"),
         new Asset("TSLA")))
      .toJsonObject();
  }
}
