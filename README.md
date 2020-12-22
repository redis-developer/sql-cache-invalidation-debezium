## Caching Update with Debezium

This demonstration shows how you can use Debezium to create, update, and delete data cached in Redis.


```
+------------+      +-----------+       +---------+
|            |      |           |       |         |
| PostgreSQL | ---> | Debdezium |  ---> |  Redis  |
|            |      |           |       |         |
+------------+      +-----------+       +---------+
```

**Prerequisites**

* Docker
   * To run Postgresl & Redis
* Java Developer Kit (8 or later)
* Apache Maven

### Running the demonstration

1. Get the source code from the repository

    ```
    > git clone https://github.com/redis-developer/sql-cache-invalidation-debezium.git

    > cd sql-cache-invalidation-debezium
    ```

1. Start Redis

    The Java Spring application use Jedis to connect to Redis. The connection string is located in the `application.properties` file.

    Open a new terminal and run the command:

    ```
    > docker run -it --rm --name redis -p 6379:6379 redis
    ```

1. Build and Run PostgreSQL database

    In the sql-cache-invalidation-debezium run the following commands:

    ```
    > cd posgresql

    > docker build -t postgresql-cdc .

    >  docker run -it --rm --name postgresql-cdc -p 5432:5432 -e POSTGRES_PASSWORD=password postgresql-cdc

    ```

    The PostreSQL instance is started with the configuration options needed by the [Debezium Postgres Connector](https://debezium.io/documentation/reference/1.3/connectors/postgresql.html).



1. Connect to PostgreSQL & create tables
    
    Use the Docker container to connect to PostgreSQL.

    ```
    > docker exec -it postgresql-cdc bash
    ```

    Connect to PostgreSQL
    ```
    bash-5.0# psql -U postgres
    ```

    Create table and insert rows
    
    ```sql
    CREATE TABLE customers (
        customer_id serial PRIMARY KEY,
        username VARCHAR ( 50 ) UNIQUE NOT NULL,
        first_name VARCHAR ( 50 )  NOT NULL,
        last_name VARCHAR ( 50 )  NOT NULL,
        email VARCHAR ( 255 ) UNIQUE NOT NULL
    );


    CREATE TABLE support_level (
        level VARCHAR (30)  UNIQUE NOT NULL
    );

    CREATE TABLE orders (
        cust_id INT NOT NULL,
        order_type VARCHAR (10) NOT NULL,
        order_date DATE DEFAULT CURRENT_DATE,
        comments TEXT,
        PRIMARY KEY (cust_id, order_type)
    );

    ```

    You can check that tables are created using the command:

    ```sql
    \dt
    ```

1. Insert rows into tables

    ```sql 
    INSERT INTO customers
    (username, first_name, last_name, email)
    VALUES
    ('jdoe1', 'John', 'Doe', 'jdoe1@demo.com');
    ```



1. Build and run the Java application

    The Java application is a SpringBoot application that exposes a REST endpoint to start and stop the Debezium service. This service captures tthe connector continuously captures row-level changes that insert, update, and delete database content and that were committed to a PostgreSQL database


    Open a new terminal in the `sql-cache-invalidation-debezium` directory and run the following commands:

    ```
    > cd debezium-redis-demo

    > mvn clean spring-boot:run

    ```

1. Start the Debezium server

    Open your browser and go to:

    http://localhost:8082/start



1. Insert new rows into tables

    ```sql 
    INSERT INTO customers
    (username, first_name, last_name, email)
    VALUES
    ('jdoe2', 'John', 'Doe', 'jdoe2@demo.com');
    ```


    ```sql 
    INSERT INTO orders
    (cust_id, order_type, comments)
    VALUES(1, 'ONLINE', 'comment for first order' );
    ```

    ```sql
    INSERT INTO support_level
    (level)
    VALUES
    ('GOLD');
    ```
    This last row is not synchronized to Redis since the `support_level` table is not in the `table.include.list`. (`application.properties`)



Note: When you restart the Debezium service, you need to send a transaction to start the replication, so be sure you reset the service using `http://localhost:8082/start` if you have anty issue. 