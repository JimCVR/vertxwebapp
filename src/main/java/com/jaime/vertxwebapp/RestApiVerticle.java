package com.jaime.vertxwebapp;

import com.jaime.vertxwebapp.api.AssetsRestApi;
import com.jaime.vertxwebapp.api.QuotesRestApi;
import com.jaime.vertxwebapp.api.WatchListRestApi;
import com.jaime.vertxwebapp.config.BrokerConfig;
import com.jaime.vertxwebapp.config.ConfigLoader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.pgclient.impl.PgPoolImpl;
import io.vertx.pgclient.impl.PgPoolOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestApiVerticle extends AbstractVerticle {
  static final Logger LOG = LoggerFactory.getLogger(RestApiVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    ConfigLoader.load(vertx)
        .onFailure(startPromise::fail)
          .onSuccess(configuration -> {
            LOG.info("Retrieved configuration: {}", configuration);
            startHttpServerAndRoutes(startPromise, configuration);
          });
  }

  private void startHttpServerAndRoutes(Promise<Void> startPromise, BrokerConfig configuration) {
    final Pool db = createDbPool(configuration);
    final Router restApi = Router.router(vertx);
    restApi.route()
      .handler(BodyHandler.create())
      .failureHandler(errorContext -> handleFailure(errorContext));
    AssetsRestApi.attach(restApi,db);
    QuotesRestApi.attach(restApi, db);
    WatchListRestApi.attach(restApi, db);
    vertx.createHttpServer()
      .requestHandler(restApi)
      .exceptionHandler(error -> LOG.error("HTTP server error: ", error))
      .listen(configuration.getServerPort(), http -> {
      if (http.succeeded()) {
        startPromise.complete();
        LOG.info("HTTP server started on port {}", configuration.getServerPort());
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private Pool createDbPool(BrokerConfig configuration) {
    final PgConnectOptions connectOptions = new PgConnectOptions()
      .setHost(configuration.getDbConfig().getHost())
      .setPort(configuration.getDbConfig().getPort())
      .setDatabase(configuration.getDbConfig().getDatabase())
      .setUser(configuration.getDbConfig().getUser())
      .setPassword(configuration.getDbConfig().getPassword());
    final PoolOptions poolOptions = new PoolOptions().setMaxSize(4);
    final Pool db = PgPool.pool(vertx, connectOptions, poolOptions);
    return db;
  }

  private static void handleFailure(RoutingContext errorContext) {
    if(errorContext.response().ended()){
      //ignore completed response
      return;
    }
    LOG.error("Route Error: ", errorContext.failure());
    errorContext.response()
      .setStatusCode(500)
      .end(new JsonObject().put("message","someting went wrong :( ").toBuffer());
  }
}
