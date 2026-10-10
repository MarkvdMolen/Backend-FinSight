INSERT INTO wealth_accounts (id,institution,name,iban,currency,type) VALUES
(10001,'Rabobank','Betaal- en spaarrekening',NULL,'EUR','CASH'),
(10002,'Revolut','Betaalrekening',NULL,'EUR','CASH'),
(10003,'Trade Republic','Cashrekening',NULL,'EUR','CASH'),
(10004,'Trade Republic','Beleggingsrekening',NULL,'EUR','INVESTMENT'),
(10005,'DEGIRO','Beleggingsrekening',NULL,'EUR','INVESTMENT') ON CONFLICT DO NOTHING;
INSERT INTO wealth_categories (id,name) VALUES
(10001,'Salaris'),(10002,'Wonen'),(10003,'Boodschappen'),(10004,'Overige uitgaven'),(10005,'Eigen overboeking') ON CONFLICT DO NOTHING;
-- Fixed, reproducible demo history. Run only against a development database.
INSERT INTO wealth_balance_snapshots (account_id,date,balance)
SELECT a.id, (date_trunc('month',CURRENT_DATE) - (n || ' months')::interval)::date,
       round((CASE a.id WHEN 10001 THEN 12450 WHEN 10002 THEN 1850 WHEN 10003 THEN 8300 WHEN 10004 THEN 16400 ELSE 25750 END) * (1 - n * 0.02),2)
FROM wealth_accounts a CROSS JOIN generate_series(0,11) n
WHERE a.id BETWEEN 10001 AND 10005
ON CONFLICT (account_id,date) DO NOTHING;
INSERT INTO wealth_transactions (id,account_id,category_id,booking_date,amount,description,internal_transfer) VALUES
(10001,10001,10001,date_trunc('month',CURRENT_DATE)::date,3850,'Demo salaris',false),
(10002,10001,10002,date_trunc('month',CURRENT_DATE)::date,-1200,'Demo woonlasten',false),
(10003,10001,10003,date_trunc('month',CURRENT_DATE)::date,-440,'Demo boodschappen',false),
(10004,10002,10004,date_trunc('month',CURRENT_DATE)::date,-600,'Demo overige uitgaven',false),
(10005,10001,10005,date_trunc('month',CURRENT_DATE)::date,-500,'Demo eigen overboeking',true),
(10006,10003,10005,date_trunc('month',CURRENT_DATE)::date,500,'Demo eigen overboeking',true)
ON CONFLICT DO NOTHING;
SELECT setval(pg_get_serial_sequence('wealth_accounts','id'),GREATEST((SELECT MAX(id) FROM wealth_accounts),1));
SELECT setval(pg_get_serial_sequence('wealth_categories','id'),GREATEST((SELECT MAX(id) FROM wealth_categories),1));
SELECT setval(pg_get_serial_sequence('wealth_transactions','id'),GREATEST((SELECT MAX(id) FROM wealth_transactions),1));
