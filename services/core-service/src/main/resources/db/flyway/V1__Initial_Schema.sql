-- Flyway Migration: V1__Initial_Schema.sql
-- Initializes database schema for EduPlatform

-- Users table (for SQL-based services if needed)
CREATE TABLE IF NOT EXISTS users (
                                     id VARCHAR(36) PRIMARY KEY,
                                     tenant_id VARCHAR(255) NOT NULL,
                                     email VARCHAR(255) NOT NULL UNIQUE,
                                     password_hash VARCHAR(255) NOT NULL,
                                     role VARCHAR(50) NOT NULL,
                                     status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Courses table
CREATE TABLE IF NOT EXISTS courses (
                                       id VARCHAR(36) PRIMARY KEY,
                                       tenant_id VARCHAR(255) NOT NULL,
                                       instructor_id VARCHAR(36) NOT NULL,
                                       title VARCHAR(500) NOT NULL,
                                       description LONGTEXT,
                                       price DECIMAL(10, 2),
                                       status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (instructor_id) REFERENCES users(id)
);

-- Enrollments table
CREATE TABLE IF NOT EXISTS enrollments (
                                           id VARCHAR(36) PRIMARY KEY,
                                           tenant_id VARCHAR(255) NOT NULL,
                                           user_id VARCHAR(36) NOT NULL,
                                           course_id VARCHAR(36) NOT NULL,
                                           status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                                           progress_percentage INT DEFAULT 0,
                                           enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           completed_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Lessons table
CREATE TABLE IF NOT EXISTS lessons (
                                       id VARCHAR(36) PRIMARY KEY,
                                       tenant_id VARCHAR(255) NOT NULL,
                                       course_id VARCHAR(36) NOT NULL,
                                       title VARCHAR(500) NOT NULL,
                                       duration_minutes INT,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
                                        id VARCHAR(36) PRIMARY KEY,
                                        tenant_id VARCHAR(255) NOT NULL,
                                        user_id VARCHAR(36) NOT NULL,
                                        course_id VARCHAR(36) NOT NULL,
                                        amount DECIMAL(10, 2) NOT NULL,
                                        status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                        razorpay_order_id VARCHAR(255),
                                        razorpay_payment_id VARCHAR(255),
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Audit Log table
CREATE TABLE IF NOT EXISTS audit_logs (
                                          id VARCHAR(36) PRIMARY KEY,
                                          tenant_id VARCHAR(255) NOT NULL,
                                          user_id VARCHAR(36),
                                          action VARCHAR(255) NOT NULL,
                                          entity_type VARCHAR(100) NOT NULL,
                                          entity_id VARCHAR(36) NOT NULL,
                                          old_values LONGTEXT,
                                          new_values LONGTEXT,
                                          `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
