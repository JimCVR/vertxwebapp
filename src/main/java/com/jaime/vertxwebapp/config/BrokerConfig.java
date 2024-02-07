package com.jaime.vertxwebapp.config;

import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Builder
@Value
@ToString
public class BrokerConfig {

  int serverPort;
  String version;
  DbConfig dbConfig;

  public static BrokerConfig from(final JsonObject config){
    final Integer serverPort = config.getInteger(ConfigLoader.SERVER_PORT);
    if(serverPort == null){
      throw new RuntimeException(ConfigLoader.SERVER_PORT + " Not configured!");
    }
    final String version = config.getString("version");
    if(version == null){
      throw new RuntimeException( "version not configured in config file!");
    }
    return BrokerConfig.builder()
      .serverPort(config.getInteger(ConfigLoader.SERVER_PORT))
      .version(version)
      .dbConfig(parseDbConfig(config))
      .build();
  }

  private static DbConfig parseDbConfig(JsonObject config) {
    return
      DbConfig.builder()
        .host(config.getString(ConfigLoader.DB_HOST))
        .port(config.getInteger(ConfigLoader.DB_PORT))
        .database(config.getString(ConfigLoader.DB_DATABASE))
        .user(config.getString(ConfigLoader.DB_USER))
        .password(config.getString(ConfigLoader.DB_PASSWORD))
        .build();
  }

}
