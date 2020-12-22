
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

INSERT INTO orders
(cust_id, order_type, comments)
VALUES(1, 'ONLINE', 'comment for first order' );

INSERT INTO orders
(cust_id, order_type, comments)
VALUES(1, 'SHOP', 'comment for first order' );

INSERT INTO support_level
(level)
VALUES
('GOLD');

INSERT INTO support_level
(level)
VALUES
('SILVER');

INSERT INTO support_level
(level)
VALUES
('BRONZE');

INSERT INTO customers
(username, first_name, last_name, email)
VALUES
('jdoe1', 'John', 'Doe', 'jdoe1@demo.com');



INSERT INTO customers
(username, first_name, last_name, email)
VALUES
('jdoe2', 'John', 'Doe', 'jdoe2@demo.com');

INSERT INTO customers
(username, first_name, last_name, email)
VALUES
('jdoe3', 'John', 'Doe', 'jdoe3@demo.com');

INSERT INTO customers
(username, first_name, last_name, email)
VALUES
('jdoe4', 'John', 'Doe', 'jdoe4@demo.com');

INSERT INTO customers
(username, first_name, last_name, email)
VALUES
('jdoe5', 'John', 'Doe', 'jdoe5@demo.com');

INSERT INTO customers
(username, first_name, last_name, email)
VALUES
('jdoe6', 'John', 'Doe', 'jdoe6@demo.com');

INSERT INTO customers
(username, first_name, last_name, email)
VALUES
('jdoe7', 'John', 'Doe', 'jdoe7@demo.com');

INSERT INTO customers
(username, first_name, last_name, email)
VALUES
('jdoe8', 'John', 'Doe', 'jdoe8@demo.com');

INSERT INTO customers
(username, first_name, last_name, email)
VALUES
('jdoe9', 'John', 'Doe', 'jdoe9@demo.com');


