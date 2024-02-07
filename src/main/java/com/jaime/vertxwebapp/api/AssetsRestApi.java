package com.jaime.vertxwebapp.api;

import com.jaime.vertxwebapp.handlers.GetAssetsHandler;
import io.vertx.ext.web.Router;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class AssetsRestApi {
  public static final Logger LOG = LoggerFactory.getLogger(AssetsRestApi.class);
  public static final List<String> ASSETS = Arrays.asList("AAPL", "AMZN","TSLA","NFLX","FB","GOOG", "MFST");
  public static void attach(Router router, Pool db){
    router.get("/assets").handler(new GetAssetsHandler());
    router.get("/pg/assets").handler(new GetAssetsFromDatabaseHandler(db));
  }



}
