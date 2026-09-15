CREATE TABLE IF NOT EXISTS orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_name TEXT,
    customer_phone TEXT,
    items_ordered TEXT,
    quantity TEXT,
    delivery_address TEXT,
    order_date TEXT
);
