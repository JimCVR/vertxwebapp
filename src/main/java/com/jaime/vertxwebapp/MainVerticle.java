package com.jaime.vertxwebapp;

import com.jaime.vertxwebapp.config.ConfigLoader;
import com.jaime.vertxwebapp.db.migration.FlywayMigration;
import io.vertx.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

  static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.exceptionHandler(error ->
      LOG.error("Unhandled: ", error));
    vertx.deployVerticle(new MainVerticle()).onFailure( err ->
        LOG.error("Failed to deploy: ", err))
      .onSuccess( id -> LOG.info("Deployed {} with id {}!", MainVerticle.class.getSimpleName(), id));
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.deployVerticle(VersionInfoVerticle.class.getName())
      .onFailure(startPromise::fail)
      .onSuccess(id -> LOG.info("Deployed {} with id {}!", VersionInfoVerticle.class.getSimpleName(), id))
      .compose(next -> migrateDatabase())
      .onFailure(startPromise::fail)
      .onSuccess(id -> LOG.info("Migrated db schema to latest version!"))
      .compose(next -> deployRestApiVerticle(startPromise));
  }

  private Future<Void> migrateDatabase() {
    return ConfigLoader.load(vertx).compose(config -> {
      return FlywayMigration.migrate(vertx, config.getDbConfig());
    });
  }

  private Future<String> deployRestApiVerticle(Promise<Void> startPromise) {
    return vertx.deployVerticle(RestApiVerticle.class.getName(), new DeploymentOptions())
      .onFailure(startPromise::fail)
      .onSuccess(id -> {
        LOG.info("Deployed {} with id {}", RestApiVerticle.class.getSimpleName(), id);
        startPromise.complete();
      });
  }

  private static int getProcessors() {
    return Math.max(1,Runtime.getRuntime().availableProcessors()/2);
  }
}
