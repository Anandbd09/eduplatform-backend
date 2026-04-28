// Flyway Migration: V3__Seed_Initial_Data.js
// Seeds initial data for EduPlatform

// Create default tenant
db.tenants.insertOne({
    _id: "default",
    name: "Default Tenant",
    plan: "STARTER",
    status: "ACTIVE",
    createdAt: new Date()
});

// Create admin user
db.users.insertOne({
    _id: ObjectId(),
    email: "admin@eduplatform.com",
    role: "ADMIN",
    status: "ACTIVE",
    tenantId: "default",
    createdAt: new Date()
});

// Create sample achievements
db.achievements.insertMany([
    {
        _id: ObjectId(),
        achievementCode: "FIRST_LESSON",
        title: "First Steps",
        description: "Complete your first lesson",
        category: "LEARNING",
        pointsReward: 50,
        tenantId: "default"
    },
    {
        _id: ObjectId(),
        achievementCode: "SEVEN_DAY_STREAK",
        title: "On Fire!",
        description: "Maintain a 7-day learning streak",
        category: "STREAK",
        pointsReward: 250,
        tenantId: "default"
    },
    {
        _id: ObjectId(),
        achievementCode: "THIRTY_DAY_STREAK",
        title: "Month Master",
        description: "Maintain a 30-day learning streak",
        category: "STREAK",
        pointsReward: 1000,
        tenantId: "default"
    },
    {
        _id: ObjectId(),
        achievementCode: "PERFECT_SCORE",
        title: "Perfect!",
        description: "Score 100% on a lesson",
        category: "LEARNING",
        pointsReward: 500,
        tenantId: "default"
    }
]);

// Create sample coupons
db.coupons.insertMany([
    {
        _id: ObjectId(),
        code: "WELCOME50",
        discountPercentage: 50,
        maxUses: 1000,
        usedCount: 0,
        expiresAt: new Date(Date.now() + 90 * 24 * 60 * 60 * 1000),
        status: "ACTIVE",
        tenantId: "default"
    },
    {
        _id: ObjectId(),
        code: "REFERRAL20",
        discountPercentage: 20,
        maxUses: 5000,
        usedCount: 0,
        status: "ACTIVE",
        tenantId: "default"
    }
]);

print("✓ Initial data seeded successfully");