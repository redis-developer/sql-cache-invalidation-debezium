package io.redis.demo.cdc;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import io.debezium.data.Envelope.Operation;
import io.debezium.data.Envelope;
import static io.debezium.data.Envelope.FieldName.*;
import io.debezium.embedded.Connect;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import lombok.extern.slf4j.Slf4j;
import io.debezium.config.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Component
@org.springframework.context.annotation.Configuration
public class DebeziumToRedisService {

  @Value("${redis.uri}")
  private String redisURI;

  private JedisPool jedisPool;


  private String status="STOPPED";


  // Thread for the Debezium engine
  private ExecutorService executor = Executors.newSingleThreadExecutor();
  DebeziumEngine<ChangeEvent<SourceRecord, SourceRecord>> engine = null;

  private final Properties configAsProperties;

  public DebeziumToRedisService(Configuration config) throws IOException {
    configAsProperties = config.asProperties();
  }



  public void startDebezium() throws IOException, URISyntaxException {
    log.info("Starting Debezium....");

    try (DebeziumEngine<ChangeEvent<SourceRecord, SourceRecord>> newEngine = DebeziumEngine.create(Connect.class).using(configAsProperties)
        .notifying(record -> {
          handleEvent(record);
        }).build())
    {
      engine = newEngine;
      executor = Executors.newSingleThreadExecutor();
      executor.execute(engine);
      status = "RUNNING";
    }

    // if JedisPool is null create it
    if (jedisPool == null) {
      URI uri = new URI(redisURI);
      jedisPool = new JedisPool( new GenericObjectPoolConfig(), uri);
      log.info("Redis Connection Pool Created : {}", redisURI);

    }

  }

  public void stopDebezium(){
    log.info("Stopping Debezium....");
    try {
      engine.close();
      executor.shutdown();
      executor = null;
    } catch (Exception e) {
      e.printStackTrace();
      log.error( e.getMessage() );
    } finally {
      status = "STOPPED";
    }
  }

  public void resetDebezium() throws IOException, URISyntaxException {
    stopDebezium();

    // remove offset file
    String offsetFile = (String)configAsProperties.get("offset.storage.file.filename");

    File file = new File(offsetFile);
    if (file.delete()) {
      log.info("{} file delete", offsetFile);
    } else {
      log.info("cannot delete {} file", offsetFile);
    }

    startDebezium();
  }

  private void handleEvent(ChangeEvent<SourceRecord, SourceRecord> record) {
    String structureType = AFTER;

    SourceRecord recordInfo = record.value();
    Struct payload = (Struct) recordInfo.value();

    if (payload != null) {
      Envelope.Operation op = Envelope.Operation.forCode(payload.getString("op"));
      log.info("\tMessage Received : {}", op);


      // In case of delete we are only interested by the "before data"
      if (op == Operation.DELETE) {
        structureType = BEFORE;
      }

      Struct data = (Struct)payload.get(structureType);


      // prepare header
      Struct sourcePayload = (Struct) payload.get(SOURCE);
      Map<String, Object> cdcHeader = new HashMap<>();
      cdcHeader.put("source.db", sourcePayload.getString("db"));
      cdcHeader.put("source.table", sourcePayload.getString("table"));
      cdcHeader.put("source.operation", op);


      // create the Redis key using the schema information
      List<String> keyFieldNames = recordInfo.keySchema().fields().stream().map(field -> field.name()).collect(Collectors.toList());
      List keyValues = keyFieldNames.stream().map(fieldName -> fieldName +":"+ data.get(fieldName).toString()).collect(Collectors.toList());

      Map<String, String> cdcPayload = getCDCEventAsMap( structureType, payload );

      // add the table name at the beginning of the list
      keyValues.add(0, cdcHeader.get("source.table"));
      String redisKey = String.join(":", keyValues); // create unique id as redis Key

      log.info("\tRedis Hash Key : {}", redisKey);


      try (Jedis jedis = jedisPool.getResource()) {


        if (op == Operation.DELETE) {
          jedis.del(redisKey);
        } else {
          jedis.hmset( redisKey, cdcPayload );
        }

      }
    } else {
      // no payload
    }
  }


  /**
   * Helper method that transform the Debezium Structure into a simple Map
   * @param operation
   * @param payload
   * @return
   */
  private Map<String,String> getCDCEventAsMap(String operation, Struct payload) {
    Struct messagePayload = (Struct) payload.get(operation);
    Map<String, String> cdcPayload = messagePayload.schema().fields().stream().map(Field::name)
        .filter(fieldName -> messagePayload.get(fieldName) != null)
        .map(fieldName -> Pair.of(fieldName, String.valueOf(messagePayload.get(fieldName))))
        .collect(toMap(Pair::getKey,  Pair::getValue));
    return cdcPayload;
  }


  public String getState() {
    return this.status;
  }

}
