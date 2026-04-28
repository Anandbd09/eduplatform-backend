// Flyway Migration: V2__Create_Indexes.js
// Creates all MongoDB indexes for optimal query performance

// Users indexes
db.users.createIndex({ email: 1 }, { unique: true });
db.users.createIndex({ tenantId: 1, role: 1 });
db.users.createIndex({ status: 1 });
db.users.createIndex({ createdAt: -1 });

// Courses indexes
db.courses.createIndex({ tenantId: 1, status: 1 });
db.courses.createIndex({ instructorId: 1 });
db.courses.createIndex({ category: 1 });
db.courses.createIndex({ createdAt: -1 });

// Enrollments indexes
db.enrollments.createIndex({ userId: 1, courseId: 1 }, { unique: true });
db.enrollments.createIndex({ courseId: 1 });
db.enrollments.createIndex({ status: 1 });
db.enrollments.createIndex({ progress: 1 });

// Payments indexes
db.payments.createIndex({ userId: 1 });
db.payments.createIndex({ courseId: 1 });
db.payments.createIndex({ status: 1 });
db.payments.createIndex({ razorpayOrderId: 1 }, { unique: true, sparse: true });
db.payments.createIndex({ createdAt: -1 });

// Notifications indexes
db.notifications.createIndex({ userId: 1, read: 1 });
db.notifications.createIndex({ createdAt: -1 });

// Messages indexes
db.messages.createIndex({ senderId: 1 });
db.messages.createIndex({ recipientId: 1, status: 1 });
db.messages.createIndex({ sentAt: -1 });

// Audit logs indexes
db.audit_logs.createIndex({ entityType: 1, entityId: 1 });
db.audit_logs.createIndex({ userId: 1 });
db.audit_logs.createIndex({ timestamp: -1 });
db.audit_logs.createIndex({ tenantId: 1 });

// TTL Indexes (auto-delete after expiration)
db.sessions.createIndex({ expiresAt: 1 }, { expireAfterSeconds: 0 });
db.otp_codes.createIndex({ expiresAt: 1 }, { expireAfterSeconds: 0 });
db.refresh_tokens.createIndex({ expiresAt: 1 }, { expireAfterSeconds: 0 });

print("✓ All MongoDB indexes created successfully");