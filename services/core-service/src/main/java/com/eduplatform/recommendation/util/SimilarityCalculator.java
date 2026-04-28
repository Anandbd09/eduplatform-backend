package com.eduplatform.recommendation.util;

import java.util.Map;
import java.util.Set;

public class SimilarityCalculator {

    /**
     * Calculate cosine similarity between two vectors
     */
    public static double cosineSimilarity(java.util.List<Double> vec1, java.util.List<Double> vec2) {
        if (vec1 == null || vec2 == null || vec1.isEmpty() || vec2.isEmpty()) {
            return 0.0;
        }

        if (vec1.size() != vec2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            normA += Math.pow(vec1.get(i), 2);
            normB += Math.pow(vec2.get(i), 2);
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Calculate similarity between two feature vectors (maps)
     */
    public static double featureSimilarity(Map<String, Double> features1, Map<String, Double> features2) {
        if (features1 == null || features2 == null || features1.isEmpty() || features2.isEmpty()) {
            return 0.0;
        }

        Set<String> allKeys = new java.util.HashSet<>();
        allKeys.addAll(features1.keySet());
        allKeys.addAll(features2.keySet());

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (String key : allKeys) {
            double a = features1.getOrDefault(key, 0.0);
            double b = features2.getOrDefault(key, 0.0);

            dotProduct += a * b;
            normA += Math.pow(a, 2);
            normB += Math.pow(b, 2);
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Calculate recency weight (newer = higher weight)
     */
    public static double recencyWeight(long ageInDays) {
        // Decay factor: older items get lower weight
        return Math.max(1.0 - (ageInDays / 60.0), 0.1);
    }

    /**
     * Calculate euclidean distance
     */
    public static double euclideanDistance(java.util.List<Double> vec1, java.util.List<Double> vec2) {
        if (vec1 == null || vec2 == null || vec1.size() != vec2.size()) {
            return Double.MAX_VALUE;
        }

        double sum = 0.0;
        for (int i = 0; i < vec1.size(); i++) {
            sum += Math.pow(vec1.get(i) - vec2.get(i), 2);
        }

        return Math.sqrt(sum);
    }
}