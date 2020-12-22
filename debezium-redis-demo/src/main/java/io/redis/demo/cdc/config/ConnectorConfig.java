package io.redis.demo.cdc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class ConnectorConfig {

  @Value("${connector.class}")
  private String connectorClass;

  @Value("${database.hostname}")
  private String databaseHostname;

  @Value("${database.name}")
  private String databaseName;

  @Value("${database.port}")
  private String databasePort;

  @Value("${database.user}")
  private String databaseUser;

  @Value("${database.password}")
  private String rdbmsDBPassword;

  @Value("${database.password}")
  private String databasePassword;

  @Value("${database.server.name}")
  private String databaseServerName;

  @Value("${offset.storage.file.filename}")
  private String OffsetStorageFile;

  @Value("${schema.include.list}")
  private String schemaIncludeList;

  @Value("${table.include.list}")
  private String tableIncludeList;

  @Bean
  public io.debezium.config.Configuration createConnectorConfig() {
    Properties props = new Properties();

    props.setProperty("name", "streamsserviceengine");
    props.setProperty("connector.class", connectorClass);

    props.setProperty("database.hostname", databaseHostname);
    props.setProperty("database.name", databaseName);
    props.setProperty("database.dbname", databaseName);
    props.setProperty("database.port", databasePort);
    props.setProperty("database.user", databaseUser);
    props.setProperty("database.password", databasePassword);
    //props.setProperty("database.server.id", databaseServerId);
    props.setProperty("database.server.name", databaseServerName);

    //props.setProperty("database.history", "io.debezium.relational.history.FileDatabaseHistory");
    //props.setProperty("database.history.file.filename", "./dbhistory-cdc-sync.dat");


    props.setProperty("snapshot.mode", "exported");

    props.setProperty("schema.include.list", schemaIncludeList);
    props.setProperty("table.include.list", tableIncludeList);


    props.setProperty("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore");
    props.setProperty("offset.flush.interval.ms", "5000");
    props.setProperty("offset.storage.file.filename", OffsetStorageFile);


    return io.debezium.config.Configuration.from(props);
  }


}
