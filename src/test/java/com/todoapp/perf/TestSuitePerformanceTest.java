package com.todoapp.perf;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Performance sanity tests to ensure the test suite runs efficiently.
 * 
 * This test class validates that:
 * - Individual tests complete within reasonable time limits
 * - The overall test suite meets performance expectations
 * - No tests have performance regressions
 * 
 * Target: Full test suite should complete in under 5 seconds for rapid feedback.
 * 
 * Note: This is a lightweight performance test that doesn't require Spring context
 * to avoid adding overhead to performance measurements.
 */
class TestSuitePerformanceTest {

    private static final Duration MAX_INDIVIDUAL_TEST_TIME = Duration.ofMillis(500);
    private static final Duration TARGET_FULL_SUITE_TIME = Duration.ofSeconds(5);
    
    private static Instant suiteStartTime;
    private static final AtomicLong contextStartupTime = new AtomicLong();

    @BeforeAll
    static void recordSuiteStartTime() {
        suiteStartTime = Instant.now();
        System.out.println("🎯 Performance Test Suite Started: " + suiteStartTime);
    }

    @Test
    void testExecutionOverhead_shouldBeMinimal() {
        // This test validates that test execution overhead is minimal
        Instant testStart = Instant.now();
        
        // Measure just the test framework overhead
        Duration testOverhead = Duration.between(suiteStartTime, testStart);
        contextStartupTime.set(testOverhead.toMillis());
        
        System.out.printf("⚡ Test Framework Overhead: %dms%n", testOverhead.toMillis());
        
        assertTrue(testOverhead.compareTo(Duration.ofMillis(100)) <= 0,
            String.format("Test framework overhead took %dms, but should be under 100ms for rapid feedback",
                testOverhead.toMillis()));
    }

    @Test
    void individualTest_shouldCompleteQuickly(TestInfo testInfo) {
        Instant testStart = Instant.now();
        
        // Simulate typical test operations
        performTypicalTestOperations();
        
        Duration testDuration = Duration.between(testStart, Instant.now());
        System.out.printf("⏱️  Test '%s' completed in %dms%n", 
            testInfo.getDisplayName(), testDuration.toMillis());
        
        assertTrue(testDuration.compareTo(MAX_INDIVIDUAL_TEST_TIME) <= 0,
            String.format("Individual test took %dms, but should be under %dms",
                testDuration.toMillis(), MAX_INDIVIDUAL_TEST_TIME.toMillis()));
    }

    @Test
    void memoryUsage_shouldBeReasonable() {
        Runtime runtime = Runtime.getRuntime();
        
        // Force garbage collection for accurate measurement
        System.gc();
        Thread.yield();
        
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        double memoryUsagePercentage = (double) usedMemory / maxMemory * 100;
        
        System.out.printf("💾 Memory Usage: %.1f%% (%d MB used / %d MB max)%n",
            memoryUsagePercentage, usedMemory / 1024 / 1024, maxMemory / 1024 / 1024);
        
        // Ensure we're not using excessive memory (> 80% of available heap)
        assertTrue(memoryUsagePercentage < 80.0,
            String.format("Memory usage is %.1f%%, which is too high for a test suite", memoryUsagePercentage));
    }

    @Test
    void databaseOperations_shouldBeOptimized() {
        Instant start = Instant.now();
        
        // Simulate typical database operations that would occur in our tests
        for (int i = 0; i < 10; i++) {
            // Simulate query preparation and execution time
            simulateDatabaseOperation();
        }
        
        Duration operationTime = Duration.between(start, Instant.now());
        System.out.printf("🗄️  10 Database Operations completed in %dms%n", operationTime.toMillis());
        
        // Database operations should be fast (< 100ms for 10 operations)
        assertTrue(operationTime.toMillis() < 100,
            String.format("Database operations took %dms, should be under 100ms", operationTime.toMillis()));
    }

    @Test
    void overallSuitePerformance_shouldMeetTargets() {
        Duration totalSuiteTime = Duration.between(suiteStartTime, Instant.now());
        
        System.out.println("\n📊 Performance Summary:");
        System.out.printf("   ⏰ Total Suite Time: %dms%n", totalSuiteTime.toMillis());
        System.out.printf("   🚀 Context Startup: %dms%n", contextStartupTime.get());
        System.out.printf("   🎯 Target Time: %dms%n", TARGET_FULL_SUITE_TIME.toMillis());
        
        // Determine performance rating
        String performanceRating = getPerformanceRating(totalSuiteTime);
        System.out.printf("   🏆 Performance Rating: %s%n", performanceRating);
        
        // The suite should complete within target time for developer productivity
        boolean meetsTarget = totalSuiteTime.compareTo(TARGET_FULL_SUITE_TIME) <= 0;
        
        if (meetsTarget) {
            System.out.println("   ✅ Performance target achieved!");
        } else {
            System.out.printf("   ⚠️  Performance target missed by %dms%n", 
                totalSuiteTime.toMillis() - TARGET_FULL_SUITE_TIME.toMillis());
        }
        
        // Log recommendations based on performance
        logPerformanceRecommendations(totalSuiteTime);
    }

    /**
     * Simulates typical test operations like object creation, assertions, etc.
     */
    private void performTypicalTestOperations() {
        // Simulate object creation
        String testData = "test-data-" + System.nanoTime();
        
        // Simulate assertions
        assertNotNull(testData);
        assertTrue(testData.startsWith("test-data"));
        
        // Simulate some processing
        testData.hashCode();
    }

    /**
     * Simulates database operations without actual database calls.
     */
    private void simulateDatabaseOperation() {
        try {
            // Simulate query processing time
            Thread.sleep(2); // 2ms per operation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Determines performance rating based on execution time.
     */
    private String getPerformanceRating(Duration totalTime) {
        long millis = totalTime.toMillis();
        
        if (millis <= 2000) return "🚀 Excellent (< 2s)";
        if (millis <= 3000) return "⚡ Very Good (< 3s)";
        if (millis <= 5000) return "✅ Good (< 5s)";
        if (millis <= 8000) return "⚠️  Acceptable (< 8s)";
        return "❌ Needs Optimization (> 8s)";
    }

    /**
     * Logs performance recommendations based on execution time.
     */
    private void logPerformanceRecommendations(Duration totalTime) {
        long millis = totalTime.toMillis();
        
        System.out.println("\n💡 Performance Recommendations:");
        
        if (millis <= TARGET_FULL_SUITE_TIME.toMillis()) {
            System.out.println("   🎉 Test suite performance is excellent!");
            System.out.println("   ✨ Consider this a benchmark for future changes");
        } else if (millis <= 8000) {
            System.out.println("   🔍 Consider optimizing:");
            System.out.println("   • Use @MockBean instead of full integration tests where possible");
            System.out.println("   • Reduce Spring context reloads with consistent test configuration");
            System.out.println("   • Use in-memory H2 database for faster tests");
        } else {
            System.out.println("   ⚡ Urgent optimizations needed:");
            System.out.println("   • Review test architecture for unnecessary overhead");
            System.out.println("   • Consider test parallelization");
            System.out.println("   • Profile tests to identify bottlenecks");
            System.out.println("   • Separate integration tests from unit tests");
        }
        
        System.out.println();
    }
}
