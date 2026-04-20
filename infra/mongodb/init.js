// Initialize collections with proper structure and indexes

db = db.getSiblingDB('eduplatform');

// ==================== USERS COLLECTION ====================
db.createCollection('users');
db.users.createIndex({ tenantId: 1, role: 1 });
db.users.createIndex({ createdAt: -1 });

db.users.insertOne({
  _id: ObjectId(),
  email: 'admin@example.com',
  passwordHash: '$2a$10$...',  // bcrypt hashed "Admin123!"
  firstName: 'Admin',
  lastName: 'User',
  phone: '+91xxxxxxxxxx',
  role: 'ADMIN',
  status: 'ACTIVE',
  tenantId: 'default',
  profilePicture: null,
  createdAt: new Date(),
  updatedAt: new Date(),
  lastLoginAt: null,
  deviceSessions: []
});

// ==================== COURSES COLLECTION ====================
db.createCollection('courses');
db.courses.createIndex({ tenantId: 1, instructorId: 1 });
db.courses.createIndex({ category: 1, status: 1 });
db.courses.createIndex({ createdAt: -1 });
db.courses.createIndex({ 'modules._id': 1 });

// ==================== LESSONS COLLECTION ====================
db.createCollection('lessons');
db.lessons.createIndex({ courseId: 1, moduleId: 1 });
db.lessons.createIndex({ tenantId: 1, courseId: 1 });

// ==================== ENROLLMENTS COLLECTION ====================
db.createCollection('enrollments');
db.enrollments.createIndex(
  { tenantId: 1, userId: 1, courseId: 1 },
  { unique: true }
);
db.enrollments.createIndex({ userId: 1, status: 1 });
db.enrollments.createIndex({ courseId: 1, enrolledAt: -1 });

// ==================== LIVE SESSIONS COLLECTION ====================
db.createCollection('liveSessions');
db.liveSessions.createIndex({ tenantId: 1, courseId: 1 });
db.liveSessions.createIndex({ roomId: 1 }, { unique: true });
db.liveSessions.createIndex({ scheduledAt: 1, status: 1 });

// ==================== CHAT MESSAGES COLLECTION ====================
db.createCollection('chatMessages');
db.chatMessages.createIndex({ tenantId: 1, roomId: 1, createdAt: -1 });
db.chatMessages.createIndex({ userId: 1, createdAt: -1 });

// ==================== NOTIFICATIONS COLLECTION ====================
db.createCollection('notifications');
db.notifications.createIndex({ tenantId: 1, userId: 1, read: 1 });
db.notifications.createIndex({ createdAt: -1 });
db.notifications.createIndex(
  { createdAt: 1 },
  { expireAfterSeconds: 2592000 }  // Auto-delete after 30 days
);

// ==================== REFRESH TOKENS COLLECTION ====================
db.createCollection('refreshTokens');
db.refreshTokens.createIndex({ token: 1 }, { unique: true });
db.refreshTokens.createIndex({ userId: 1 });
db.refreshTokens.createIndex(
  { expiresAt: 1 },
  { expireAfterSeconds: 0 }  // TTL index
);

// ==================== PAYMENTS COLLECTION ====================
db.createCollection('payments');
db.payments.createIndex({ tenantId: 1, userId: 1, createdAt: -1 });
db.payments.createIndex({ razorpayOrderId: 1 }, { unique: true });
db.payments.createIndex({ status: 1, createdAt: -1 });

// ==================== AUDIT LOG COLLECTION ====================
db.createCollection('auditLogs');
db.auditLogs.createIndex({ userId: 1, action: 1, createdAt: -1 });
db.auditLogs.createIndex(
  { createdAt: 1 },
  { expireAfterSeconds: 5184000 }  // Auto-delete after 60 days
);

print('✅ MongoDB initialization complete!');
print('Collections created: users, courses, lessons, enrollments, etc.');