-- Insert Stock Exchanges
INSERT INTO stock_exchange (name, description, live_in_market) VALUES
                                                                   ('New York Stock Exchange', 'The world''s largest stock exchange by market capitalization', FALSE),
                                                                   ('NASDAQ', 'American stock exchange focused on technology stocks', FALSE),
                                                                   ('London Stock Exchange', 'One of the oldest stock exchanges in the world', FALSE),
                                                                   ('Tokyo Stock Exchange', 'The largest stock exchange in Asia', FALSE),
                                                                   ('Shanghai Stock Exchange', 'Major stock exchange in mainland China', FALSE);

-- Insert Stocks
INSERT INTO stock (name, description, current_price) VALUES
                                                         ('Apple Inc.', 'Technology company specializing in consumer electronics', 178.50),
                                                         ('Microsoft Corporation', 'Technology company developing computer software and services', 412.30),
                                                         ('Amazon.com Inc.', 'E-commerce and cloud computing company', 185.75),
                                                         ('Tesla Inc.', 'Electric vehicle and clean energy company', 248.90),
                                                         ('Alphabet Inc.', 'Technology company specializing in internet services', 142.65),
                                                         ('NVIDIA Corporation', 'Technology company designing graphics processing units', 495.20),
                                                         ('Meta Platforms Inc.', 'Social media and technology conglomerate', 512.85),
                                                         ('Berkshire Hathaway', 'Multinational conglomerate holding company', 622000.00),
                                                         ('Johnson & Johnson', 'Pharmaceutical and consumer goods company', 156.40),
                                                         ('JPMorgan Chase', 'Multinational investment bank and financial services company', 218.95);

-- Create Stock Listings (Associate stocks with exchanges)
-- NYSE listings
INSERT INTO stock_exchange_stock (stock_exchange_id, stock_id) VALUES
                                                                   (1, 8),  -- Berkshire Hathaway on NYSE
                                                                   (1, 9),  -- Johnson & Johnson on NYSE
                                                                   (1, 10); -- JPMorgan Chase on NYSE

-- NASDAQ listings
INSERT INTO stock_exchange_stock (stock_exchange_id, stock_id) VALUES
                                                                   (2, 1),  -- Apple on NASDAQ
                                                                   (2, 2),  -- Microsoft on NASDAQ
                                                                   (2, 3),  -- Amazon on NASDAQ
                                                                   (2, 4),  -- Tesla on NASDAQ
                                                                   (2, 5),  -- Alphabet on NASDAQ
                                                                   (2, 6),  -- NVIDIA on NASDAQ
                                                                   (2, 7);  -- Meta on NASDAQ

-- London Stock Exchange listings (cross-listings)
INSERT INTO stock_exchange_stock (stock_exchange_id, stock_id) VALUES
                                                                   (3, 1),  -- Apple on LSE
                                                                   (3, 2),  -- Microsoft on LSE
                                                                   (3, 9);  -- Johnson & Johnson on LSE

-- Tokyo Stock Exchange listings
INSERT INTO stock_exchange_stock (stock_exchange_id, stock_id) VALUES
                                                                   (4, 1),  -- Apple on TSE
                                                                   (4, 2);  -- Microsoft on TSE