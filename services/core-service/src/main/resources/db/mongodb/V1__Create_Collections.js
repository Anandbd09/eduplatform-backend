// Flyway Migration: V1__Create_Collections.js
// Initializes all MongoDB collections for EduPlatform

db.createCollection = function (users, param2) {

};
// Users collection
let db;
db.createCollection("users", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["email", "role", "tenantId"],
            properties: {
                _id: { bsonType: "string" },
                email: { bsonType: "string" },
                role: { enum: ["STUDENT", "INSTRUCTOR", "ADMIN"] },
                status: { enum: ["ACTIVE", "INACTIVE", "SUSPENDED"] },
                tenantId: { bsonType: "string" },
                createdAt: { bsonType: "date" }
            }
        }
    }
});

// Courses collection
db.createCollection("courses", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["instructorId", "title", "tenantId"],
            properties: {
                _id: { bsonType: "string" },
                instructorId: { bsonType: "string" },
                title: { bsonType: "string" },
                status: { enum: ["DRAFT", "PUBLISHED", "ARCHIVED"] },
                tenantId: { bsonType: "string" }
            }
        }
    }
});

// Enrollments collection
db.createCollection("enrollments", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["userId", "courseId", "tenantId"],
            properties: {
                _id: { bsonType: "string" },
                userId: { bsonType: "string" },
                courseId: { bsonType: "string" },
                status: { enum: ["ACTIVE", "COMPLETED", "DROPPED"] },
                progress: { bsonType: "int" },
                tenantId: { bsonType: "string" }
            }
        }
    }
});

// Payments collection
db.createCollection("payments", {
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: ["userId", "courseId", "amount", "tenantId"],
            properties: {
                _id: { bsonType: "string" },
                userId: { bsonType: "string" },
                courseId: { bsonType: "string" },
                amount: { bsonType: "decimal" },
                status: { enum: ["PENDING", "COMPLETED", "FAILED", "REFUNDED"] },
                tenantId: { bsonType: "string" }
            }
        }
    }
});

// Notifications collection
db.createCollection("notifications");

// Messages collection
db.createCollection("messages");

// Audit logs collection
db.createCollection("audit_logs");

print("✓ All MongoDB collections created successfully");