package com.jaime.vertxwebapp.api;

import com.jaime.vertxwebapp.handlers.GetQuoteFromDatabaseHandler;
import com.jaime.vertxwebapp.handlers.GetQuotesHandler;
import com.jaime.vertxwebapp.models.Asset;
import com.jaime.vertxwebapp.models.Quote;
import io.vertx.ext.web.Router;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class QuotesRestApi {
  public static final Logger LOG = LoggerFactory.getLogger(QuotesRestApi.class);

  public static void attach(Router router, Pool db){
    final Map<String, Quote> cachedQuotes = new HashMap<>();
    AssetsRestApi.ASSETS.forEach(symbol ->
      cachedQuotes.put(symbol, initRandomQuote(symbol)));
    router.get("/quotes/:asset").handler(new GetQuotesHandler(cachedQuotes));
    router.get("/pg/quotes/:asset").handler(new GetQuoteFromDatabaseHandler(db));
  }

  private static Quote initRandomQuote(String assetParam) {
    return Quote.builder()
      .asset(new Asset(assetParam))
      .bid(randomValue())
      .ask(randomValue())
      .lastPrice(randomValue())
      .volume(randomValue())
      .build();
  }

  private static BigDecimal randomValue(){
    return BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(1,100));
  }
}
