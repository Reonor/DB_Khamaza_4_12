-- =====================================================================
--  ZLAGODA AIS - PostgreSQL schema
--  Run once:  psql -U <user> -d zlagoda -f schema-postgresql.sql -f data.sql
--
--  Note: the entity «Чек» is mapped to table `receipt`, because CHECK is a
--  reserved SQL keyword. All attribute names follow the relational model.
--  Two columns (login, password_hash) are added to `employee` to satisfy
--  the authentication / "no plaintext passwords" requirement.
-- =====================================================================

DROP TABLE IF EXISTS sale           CASCADE;
DROP TABLE IF EXISTS receipt        CASCADE;
DROP TABLE IF EXISTS store_product  CASCADE;
DROP TABLE IF EXISTS product        CASCADE;
DROP TABLE IF EXISTS category       CASCADE;
DROP TABLE IF EXISTS customer_card  CASCADE;
DROP TABLE IF EXISTS employee       CASCADE;

-- ---------------------------------------------------------------------
CREATE TABLE category (
    category_number INTEGER     NOT NULL,
    category_name   VARCHAR(50) NOT NULL,
    CONSTRAINT pk_category PRIMARY KEY (category_number)
);

CREATE TABLE product (
    id_product      INTEGER      NOT NULL,
    category_number INTEGER      NOT NULL,
    product_name    VARCHAR(50)  NOT NULL,
    characteristics VARCHAR(100) NOT NULL,
    CONSTRAINT pk_product PRIMARY KEY (id_product),
    CONSTRAINT fk_product_category FOREIGN KEY (category_number)
        REFERENCES category (category_number) ON UPDATE CASCADE ON DELETE NO ACTION
);

CREATE TABLE store_product (
    upc                 VARCHAR(12)   NOT NULL,
    upc_prom            VARCHAR(12),
    id_product          INTEGER       NOT NULL,
    selling_price       NUMERIC(13,4) NOT NULL,
    products_number     INTEGER       NOT NULL,
    promotional_product BOOLEAN       NOT NULL,
    CONSTRAINT pk_store_product PRIMARY KEY (upc),
    CONSTRAINT fk_store_product_product FOREIGN KEY (id_product)
        REFERENCES product (id_product) ON UPDATE CASCADE ON DELETE NO ACTION,
    CONSTRAINT fk_store_product_prom FOREIGN KEY (upc_prom)
        REFERENCES store_product (upc) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT chk_sp_price CHECK (selling_price >= 0),
    CONSTRAINT chk_sp_number CHECK (products_number >= 0)
);

CREATE TABLE employee (
    id_employee     VARCHAR(10)   NOT NULL,
    empl_surname    VARCHAR(50)   NOT NULL,
    empl_name       VARCHAR(50)   NOT NULL,
    empl_patronymic VARCHAR(50),
    empl_role       VARCHAR(10)   NOT NULL,
    salary          NUMERIC(13,4) NOT NULL,
    date_of_birth   DATE          NOT NULL,
    date_of_start   DATE          NOT NULL,
    phone_number    VARCHAR(13)   NOT NULL,
    city            VARCHAR(50)   NOT NULL,
    street          VARCHAR(50)   NOT NULL,
    zip_code        VARCHAR(9)    NOT NULL,
    login           VARCHAR(50)   NOT NULL,
    password_hash   VARCHAR(72)   NOT NULL,
    CONSTRAINT pk_employee PRIMARY KEY (id_employee),
    CONSTRAINT uq_employee_login UNIQUE (login),
    CONSTRAINT chk_emp_role CHECK (empl_role IN ('менеджер', 'касир')),
    CONSTRAINT chk_emp_salary CHECK (salary >= 0),
    CONSTRAINT chk_emp_phone CHECK (char_length(phone_number) <= 13)
);

CREATE TABLE customer_card (
    card_number      VARCHAR(13) NOT NULL,
    cust_surname     VARCHAR(50) NOT NULL,
    cust_name        VARCHAR(50) NOT NULL,
    cust_patronymic  VARCHAR(50),
    phone_number     VARCHAR(13) NOT NULL,
    city             VARCHAR(50),
    street           VARCHAR(50),
    zip_code         VARCHAR(9),
    discount_percent INTEGER     NOT NULL,
    CONSTRAINT pk_customer_card PRIMARY KEY (card_number),
    CONSTRAINT chk_cc_percent CHECK (discount_percent >= 0 AND discount_percent <= 100),
    CONSTRAINT chk_cc_phone CHECK (char_length(phone_number) <= 13)
);

CREATE TABLE receipt (
    check_number VARCHAR(10)   NOT NULL,
    id_employee  VARCHAR(10)   NOT NULL,
    card_number  VARCHAR(13),
    print_date   TIMESTAMP     NOT NULL,
    sum_total    NUMERIC(13,4) NOT NULL,
    vat          NUMERIC(13,4) NOT NULL,
    CONSTRAINT pk_receipt PRIMARY KEY (check_number),
    CONSTRAINT fk_receipt_employee FOREIGN KEY (id_employee)
        REFERENCES employee (id_employee) ON UPDATE CASCADE ON DELETE NO ACTION,
    CONSTRAINT fk_receipt_card FOREIGN KEY (card_number)
        REFERENCES customer_card (card_number) ON UPDATE CASCADE ON DELETE NO ACTION,
    CONSTRAINT chk_receipt_sum CHECK (sum_total >= 0),
    CONSTRAINT chk_receipt_vat CHECK (vat >= 0)
);

CREATE TABLE sale (
    upc            VARCHAR(12)   NOT NULL,
    check_number   VARCHAR(10)   NOT NULL,
    product_number INTEGER       NOT NULL,
    selling_price  NUMERIC(13,4) NOT NULL,
    CONSTRAINT pk_sale PRIMARY KEY (upc, check_number),
    CONSTRAINT fk_sale_store_product FOREIGN KEY (upc)
        REFERENCES store_product (upc) ON UPDATE CASCADE ON DELETE NO ACTION,
    CONSTRAINT fk_sale_receipt FOREIGN KEY (check_number)
        REFERENCES receipt (check_number) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT chk_sale_number CHECK (product_number > 0),
    CONSTRAINT chk_sale_price CHECK (selling_price >= 0)
);

CREATE INDEX idx_product_category   ON product (category_number);
CREATE INDEX idx_store_product_prod ON store_product (id_product);
CREATE INDEX idx_receipt_employee   ON receipt (id_employee);
CREATE INDEX idx_receipt_date       ON receipt (print_date);
CREATE INDEX idx_sale_check         ON sale (check_number);
