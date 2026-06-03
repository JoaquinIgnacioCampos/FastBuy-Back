CREATE TABLE IF NOT EXISTS categories (
    id    VARCHAR(50)  NOT NULL PRIMARY KEY,
    label VARCHAR(100) NOT NULL,
    emoji VARCHAR(10)  NOT NULL
);

CREATE TABLE IF NOT EXISTS bars (
    id       VARCHAR(50)  NOT NULL PRIMARY KEY,
    label    VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id       VARCHAR(50)  NOT NULL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    category VARCHAR(50)  NOT NULL,
    price    DOUBLE       NOT NULL,
    stock    INT          NOT NULL,
    image    VARCHAR(255),
    emoji    VARCHAR(16),
    subtitle VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS bar_products (
    bar_id     VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (bar_id, product_id),
    FOREIGN KEY (bar_id)     REFERENCES bars(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS events (
    id        VARCHAR(50)  NOT NULL PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    venue     VARCHAR(255) NOT NULL,
    hours     VARCHAR(100) NOT NULL,
    starts_at TIMESTAMP    NOT NULL,
    ends_at   TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
    id     INT           NOT NULL PRIMARY KEY AUTO_INCREMENT,
    total  DOUBLE        NOT NULL,
    items  VARCHAR(2000) NOT NULL,
    status VARCHAR(20)   NOT NULL,
    bar    VARCHAR(50)   NOT NULL,
    time   VARCHAR(10)   NOT NULL
);
