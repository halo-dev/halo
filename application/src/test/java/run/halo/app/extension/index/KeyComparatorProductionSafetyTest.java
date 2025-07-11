package run.halo.app.extension.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * Production Safety Test - Check various edge cases and potential risks of  {@link KeyComparator}.
 */
@Slf4j
public class KeyComparatorProductionSafetyTest {

    private final KeyComparator comparator = KeyComparator.INSTANCE;

    @Test
    void testCriticalEqualityIssues() {
        // Testing the critical equality issues that could lead to data loss

        String[][] testCases = {
            // 原始问题
            {"/upload/1.4.3.png", "/upload/1.4.6.png"},
            {"/upload/1.4.10.png", "/upload/1.4.2.png"},

            // 版本号模式
            {"app-1.2.3.jar", "app-1.2.4.jar"},
            {"lib-1.0.0.jar", "lib-1.0.1.jar"},
            {"plugin-2.1.0.zip", "plugin-2.1.1.zip"},

            // 时间戳模式
            {"backup-20241201-123456.tar.gz", "backup-20241201-123457.tar.gz"},
            {"log-2024-12-01-10-30-15.txt", "log-2024-12-01-10-30-16.txt"},

            // ID模式
            {"user-12345", "user-12346"},
            {"order-000001", "order-000002"},
            {"invoice-2024001", "invoice-2024002"},

            // 混合模式
            {"data-v1.2.3-build123.json", "data-v1.2.3-build124.json"},
            {"config-1.0.0-alpha.1.yml", "config-1.0.0-alpha.2.yml"}
        };

        for (String[] testCase : testCases) {
            String a = testCase[0];
            String b = testCase[1];
            int result = comparator.compare(a, b);

            // 最关键的检查：绝对不能相等
            if (result == 0) {
                log.debug("'{}' vs '{}' = {}", a, b, result);
                throw new AssertionError(String.format(
                    "CRITICAL: '%s' and '%s' should NEVER be equal! This will cause data loss!",
                    a, b));
            }
        }
    }

    @Test
    void testIndexConsistency() {
        // Test the consistency of the index keys using the comparator

        // 模拟 indexKeyObjectNamesMap 中的真实数据
        List<String> indexKeys = Arrays.asList(
            "user-1", "user-2", "user-10", "user-100",
            "file-1.0.0.jar", "file-1.0.1.jar", "file-1.1.0.jar",
            "backup-001", "backup-002", "backup-010", "backup-100",
            "log-2024-01-01.txt", "log-2024-01-02.txt", "log-2024-01-10.txt",
            "config-v1.2.3.yml", "config-v1.2.4.yml", "config-v1.10.0.yml"
        );

        // 测试排序一致性
        List<String> sorted1 = new ArrayList<>(indexKeys);
        List<String> sorted2 = new ArrayList<>(indexKeys);

        sorted1.sort(comparator);
        sorted2.sort(comparator);

        if (!sorted1.equals(sorted2)) {
            throw new AssertionError("排序结果不一致！这会导致索引不稳定！");
        }

        // 测试TreeMap的一致性（模拟真实的索引存储）
        TreeMap<String, String> indexMap = new TreeMap<>(comparator);
        for (String key : indexKeys) {
            indexMap.put(key, "value-" + key);
        }

        // 验证所有键都能被找到
        for (String key : indexKeys) {
            if (!indexMap.containsKey(key)) {
                throw new AssertionError(String.format(
                    "CRITICAL: Key '%s' lost in TreeMap! This will cause data loss!", key));
            }
        }
    }

    @Test
    void testEdgeCasesAndBoundaries() {
        // Test various edge cases and boundaries that could lead to unexpected behavior

        String[][] edgeCases = {
            // 空字符串和null
            {"", "a"},
            {"a", ""},

            // 前导零
            {"001", "1"},
            {"0001", "01"},
            {"file-001.txt", "file-1.txt"},

            // 长数字
            {"file-999999999999999999999999999999.txt", "file-1.txt"},
            {"id-123456789012345678901234567890", "id-123456789012345678901234567891"},

            // 特殊字符
            {"file-1.2.3.txt", "file-1.2.3.txt.bak"},
            {"config.1.2.3", "config.1.2.3.old"},

            // 混合字符和数字
            {"abc123def456", "abc123def457"},
            {"test-1a2b3c", "test-1a2b3d"},

            // Unicode字符
            {"文件-1.txt", "文件-2.txt"},
            {"测试-123", "测试-124"}
        };

        for (String[] testCase : edgeCases) {
            String a = testCase[0];
            String b = testCase[1];
            int result = comparator.compare(a, b);

            log.debug("'{}' vs '{}' = {}", a, b, result);

            // 对称性检查
            int reverseResult = comparator.compare(b, a);
            if (result != 0 && Integer.signum(result) == Integer.signum(reverseResult)) {
                throw new AssertionError(String.format(
                    "对称性违反: compare('%s', '%s') = %d, but compare('%s', '%s') = %d",
                    a, b, result, b, a, reverseResult));
            }

            // 一致性检查
            int result2 = comparator.compare(a, b);
            if (result != result2) {
                throw new AssertionError(String.format(
                    "一致性违反: 同样的比较产生了不同的结果: %d vs %d", result, result2));
            }
        }
    }

    @Test
    void testNullHandling() {
        // Test how the comparator handles null values

        // null 处理在索引系统中很关键
        assert comparator.compare(null, null) == 0;
        assert comparator.compare(null, "test") > 0;  // null 被认为更大
        assert comparator.compare("test", null) < 0;
    }

    @Test
    void testLargeDatasetPerformance() {
        // Test the performance of the comparator with a large dataset
        // 模拟生产环境中的大量数据
        List<String> largeDataset = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            largeDataset.add(String.format("item-%d.%d.%d", i / 100, i % 100, i % 10));
        }

        long startTime = System.currentTimeMillis();
        largeDataset.sort(comparator);
        long endTime = System.currentTimeMillis();

        log.debug("排序 {} 个元素耗时: {} ms", largeDataset.size(), endTime - startTime);

        // 验证排序结果的正确性
        for (int i = 1; i < largeDataset.size(); i++) {
            if (comparator.compare(largeDataset.get(i - 1), largeDataset.get(i)) > 0) {
                throw new AssertionError("排序结果不正确！");
            }
        }
    }
}
