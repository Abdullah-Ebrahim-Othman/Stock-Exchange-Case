-- Drop tables if they exist (for clean restart)
DROP TABLE IF EXISTS stock_exchange_stock;
DROP TABLE IF EXISTS stock;
DROP TABLE IF EXISTS stock_exchange;

-- Create Stock Exchange table
CREATE TABLE stock_exchange (
    stock_exchange_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    live_in_market BOOLEAN DEFAULT FALSE,
    version INT DEFAULT 0
);

-- Create Stock table
CREATE TABLE stock (
    stock_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    current_price DECIMAL(19, 4) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INT DEFAULT 0
);

-- Create Stock Listing (junction table)
CREATE TABLE stock_exchange_stock (
    stock_exchange_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,
    PRIMARY KEY (stock_exchange_id, stock_id),
    CONSTRAINT fk_stock_exchange
        FOREIGN KEY (stock_exchange_id)
        REFERENCES stock_exchange(stock_exchange_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_stock
        FOREIGN KEY (stock_id)
        REFERENCES stock(stock_id)
        ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_stock_name ON stock(name);
CREATE INDEX idx_stock_exchange_name ON stock_exchange(name);
CREATE INDEX idx_listing_stock ON stock_exchange_stock(stock_id);
CREATE INDEX idx_listing_exchange ON stock_exchange_stock(stock_exchange_id);