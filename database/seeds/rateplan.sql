-- User 1
INSERT INTO users (id, name, address, birthdate, email, password)
VALUES (1, 'Ahmed Mohamed', 'Cairo, Egypt', '1990-05-15', 'user1@example.com', 'pass1');
INSERT INTO rateplan (id, plan_name, ror_data, ror_voice, ror_sms, monthly_fee)
VALUES (1, 'Basic Plan', 0.05, 0.10, 0.25, 50.00);
INSERT INTO contract (user_id, rateplan_id, msisdn, status, credit_limit, available_credit)
VALUES (1, 1, '00201221234567', 'active', 500.00, 500.00);

-- User 2
INSERT INTO users (id, name, address, birthdate, email, password)
VALUES (2, 'Mohamed Ali', 'Alexandria, Egypt', '1988-09-20', 'user2@example.com', 'pass2');
INSERT INTO rateplan (id, plan_name, ror_data, ror_voice, ror_sms, monthly_fee)
VALUES (2, 'Silver Plan', 0.04, 0.08, 0.20, 75.00);
INSERT INTO contract (user_id, rateplan_id, msisdn, status, credit_limit, available_credit)
VALUES (2, 2, '00201001234567', 'active', 700.00, 700.00);

-- User 3
INSERT INTO users (id, name, address, birthdate, email, password)
VALUES (3, 'Sara Hassan', 'Giza, Egypt', '1995-03-10', 'user3@example.com', 'pass3');
INSERT INTO rateplan (id, plan_name, ror_data, ror_voice, ror_sms, monthly_fee)
VALUES (3, 'Gold Plan', 0.03, 0.06, 0.15, 100.00);
INSERT INTO contract (user_id, rateplan_id, msisdn, status, credit_limit, available_credit)
VALUES (3, 3, '00201111234567', 'active', 1000.00, 1000.00);

-- User 4
INSERT INTO users (id, name, address, birthdate, email, password)
VALUES (4, 'Omar Khaled', 'Mansoura, Egypt', '1992-12-05', 'user4@example.com', 'pass4');
INSERT INTO rateplan (id, plan_name, ror_data, ror_voice, ror_sms, monthly_fee)
VALUES (4, 'Premium Plan', 0.02, 0.05, 0.10, 150.00);
INSERT INTO contract (user_id, rateplan_id, msisdn, status, credit_limit, available_credit)
VALUES (4, 4, '00201551234567', 'active', 1500.00, 1500.00);
