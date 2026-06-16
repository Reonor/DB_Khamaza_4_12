-- =====================================================================
--  ZLAGODA AIS - seed data (portable: H2 + PostgreSQL)
--  Demo accounts:
--    manager  / manager123   (роль: менеджер)
--    cashier1 / cashier123    (роль: касир)
--    cashier2 / cashier123    (роль: касир)
-- =====================================================================

INSERT INTO category (category_number, category_name) VALUES
 (1, 'Молочні продукти'),
 (2, 'Напої'),
 (3, 'Хлібобулочні вироби');

INSERT INTO product (id_product, category_number, product_name, characteristics) VALUES
 (1, 1, 'Молоко', '2.5% жирності'),
 (2, 1, 'Сир',    'Твердий сир'),
 (3, 2, 'Кола',   'Газований напій'),
 (4, 3, 'Хліб',   'Пшеничний');

INSERT INTO store_product (upc, upc_prom, id_product, selling_price, products_number, promotional_product) VALUES
 ('100000000001', NULL, 1,  30.0000,  50, FALSE),
 ('100000000002', NULL, 2, 120.0000,  20, FALSE),
 ('100000000003', NULL, 3,  40.0000, 100, TRUE),
 ('100000000004', NULL, 4,  25.0000,  60, FALSE);

INSERT INTO customer_card (card_number, cust_surname, cust_name, cust_patronymic, phone_number, city, street, zip_code, discount_percent) VALUES
 ('C001', 'Коваленко', 'Анна', 'Петрівна',  '+380444444444', 'Київ', 'Шевченка',     '01004',  5),
 ('C002', 'Бондар',    'Олег', 'Іванович',  '+380555555555', 'Київ', 'Грушевського', '01005', 10);

INSERT INTO employee (id_employee, empl_surname, empl_name, empl_patronymic, empl_role, salary, date_of_birth, date_of_start, phone_number, city, street, zip_code, login, password_hash) VALUES
 ('E001', 'Іваненко',  'Іван',  'Іванович',    'касир',    12000.0000, DATE '1990-01-01', DATE '2020-01-01', '+380111111111', 'Київ', 'Хрещатик',         '01001', 'cashier1', '$2a$10$xY8W250CnM12z1oWTQinyOIahfMnOjTcANfMrldKVt3rDJgsOpcr6'),
 ('E002', 'Петренко',  'Олена', 'Ігорівна',    'касир',    13000.0000, DATE '1992-05-05', DATE '2021-03-03', '+380222222222', 'Київ', 'Лесі Українки',    '01002', 'cashier2', '$2a$10$RVeZHsPiByH1B9dfQJy2zeUJ0Z5H9/0oq3dTf4wcZS90YuvRMfWiO'),
 ('E003', 'Сидоренко', 'Петро', 'Олексійович', 'менеджер', 20000.0000, DATE '1985-07-07', DATE '2019-02-02', '+380333333333', 'Київ', 'Саксаганського',   '01003', 'manager',  '$2a$10$iYDEcAcdA.VnP0HNaPGgu.TL3Mm6Ye83iFem9wy3UfK8yVNzzlvhy');

INSERT INTO receipt (check_number, id_employee, card_number, print_date, sum_total, vat) VALUES
 ('CH001', 'E001', 'C001', TIMESTAMP '2023-03-15 10:20:00', 150.0000, 30.0000),
 ('CH002', 'E001', 'C002', TIMESTAMP '2023-04-10 14:05:00', 200.0000, 40.0000),
 ('CH003', 'E002', 'C001', TIMESTAMP '2023-05-05 09:45:00', 100.0000, 20.0000);

INSERT INTO sale (upc, check_number, product_number, selling_price) VALUES
 ('100000000001', 'CH001', 2,  30.0000),
 ('100000000002', 'CH001', 1, 120.0000),
 ('100000000001', 'CH002', 1,  30.0000),
 ('100000000003', 'CH002', 3,  40.0000),
 ('100000000001', 'CH003', 1,  30.0000),
 ('100000000002', 'CH003', 1, 120.0000);

-- Додатковий чек, щоб клієнт C001 мав покупки з УСІХ категорій
-- (потрібно для демонстрації запиту з подвійним запереченням).
INSERT INTO receipt (check_number, id_employee, card_number, print_date, sum_total, vat) VALUES
 ('CH004', 'E001', 'C001', TIMESTAMP '2023-06-01 12:00:00', 65.0000, 13.0000);

INSERT INTO sale (upc, check_number, product_number, selling_price) VALUES
 ('100000000003', 'CH004', 1, 40.0000),
 ('100000000004', 'CH004', 1, 25.0000);
