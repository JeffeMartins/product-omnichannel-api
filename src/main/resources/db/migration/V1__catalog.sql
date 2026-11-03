CREATE TABLE products (
 sku VARCHAR(64) PRIMARY KEY, slug VARCHAR(160) UNIQUE NOT NULL,
 name VARCHAR(160) NOT NULL, description TEXT NOT NULL, brand VARCHAR(80) NOT NULL,
 category VARCHAR(80) NOT NULL, family VARCHAR(80) NOT NULL,
 label VARCHAR(40) NOT NULL, color VARCHAR(7) NOT NULL, image VARCHAR(200) NOT NULL,
 active BOOLEAN NOT NULL DEFAULT true
);
CREATE INDEX idx_product_family ON products(family);
CREATE TABLE locations(id VARCHAR(40) PRIMARY KEY, name VARCHAR(100) NOT NULL, channel VARCHAR(10) NOT NULL CHECK(channel IN ('WEB','PICKUP')));
CREATE TABLE offers (
 sku VARCHAR(64) REFERENCES products(sku), channel VARCHAR(10) NOT NULL,
 location_id VARCHAR(40) REFERENCES locations(id),
 list_price NUMERIC(12,2) NOT NULL CHECK(list_price>=0),
 price NUMERIC(12,2) NOT NULL CHECK(price>=0 AND price<=list_price),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), PRIMARY KEY(sku,channel,location_id)
);
CREATE TABLE inventory (
 sku VARCHAR(64) REFERENCES products(sku), location_id VARCHAR(40) REFERENCES locations(id),
 quantity INTEGER NOT NULL CHECK(quantity>=0), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 PRIMARY KEY(sku,location_id)
);
