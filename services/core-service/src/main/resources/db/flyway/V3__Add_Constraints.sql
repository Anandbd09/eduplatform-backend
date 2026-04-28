-- Flyway Migration: V3__Add_Constraints.sql
-- Adds constraints and triggers for data integrity

-- Prevent duplicate enrollments
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_enrollment
ON enrollments(user_id, course_id, tenant_id);

-- Prevent duplicate payments
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_payment_order
ON payments(razorpay_order_id, tenant_id);

-- Updated timestamp trigger for users
DELIMITER //
CREATE TRIGGER IF NOT EXISTS tr_users_updated BEFORE UPDATE ON users
FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//
DELIMITER ;

-- Updated timestamp trigger for courses
DELIMITER //
CREATE TRIGGER IF NOT EXISTS tr_courses_updated BEFORE UPDATE ON courses
FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//
DELIMITER ;

-- Enrollment status validation
DELIMITER //
CREATE TRIGGER IF NOT EXISTS tr_enrollment_status BEFORE UPDATE ON enrollments
FOR EACH ROW
BEGIN
    IF NEW.status NOT IN ('ACTIVE', 'COMPLETED', 'DROPPED', 'PAUSED') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid enrollment status';
END IF;
END//
DELIMITER ;

-- Payment status validation
DELIMITER //
CREATE TRIGGER IF NOT EXISTS tr_payment_status BEFORE UPDATE ON payments
FOR EACH ROW
BEGIN
    IF NEW.status NOT IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid payment status';
END IF;
END//
DELIMITER ;