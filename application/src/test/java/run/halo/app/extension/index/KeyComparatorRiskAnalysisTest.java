package run.halo.app.extension.index;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 风险分析测试 - 专门测试可能导致生产事故的边界情况
 */
@Slf4j
public class KeyComparatorRiskAnalysisTest {

    private final KeyComparator comparator = KeyComparator.INSTANCE;

    @Test
    void testPotentialRisks() {
        // Printing the header for clarity

        // 风险1: 前导零导致的排序问题
        log.debug("风险1: 前导零可能导致的问题");
        testLeadingZeroRisk();

        // 风险2: 长数字溢出问题
        log.debug("风险2: 长数字处理");
        testLongNumberRisk();

        // 风险3: 特殊字符组合
        log.debug("风险3: 特殊字符组合可能导致的问题");
        testSpecialCharacterRisk();

        // 风险4: Unicode 字符处理
        log.debug("风险4: Unicode 字符处理");
        testUnicodeRisk();

        // 风险5: 空字符串和边界条件
        log.debug("风险5: 空字符串和边界条件");
        testBoundaryConditionRisk();
    }

    private void testLeadingZeroRisk() {
        // 这种情况可能在文件编号、订单号等场景中出现
        String[][] cases = {
            {"file-001.txt", "file-1.txt"},
            {"order-0001", "order-1"},
            {"id-000123", "id-123"},
            {"backup-007", "backup-7"},
            {"version-01.02.03", "version-1.2.3"}
        };

        for (String[] testCase : cases) {
            int result = comparator.compare(testCase[0], testCase[1]);
            log.debug("  '{}' vs '{}' = {}", testCase[0], testCase[1], result);

            // 重要：前导零的字符串应该与无前导零的字符串区分开
            // 这样可以避免在索引中丢失数据
            if (result == 0) {
                log.debug("  ⚠️  警告：前导零字符串被认为相等！");
            }
            Assertions.assertThat(result).isNotEqualTo(0);
        }
    }

    private void testLongNumberRisk() {
        // 测试超长数字，可能在ID、时间戳等场景出现
        String[][] cases = {
            {"id-123456789012345678901234567890", "id-123456789012345678901234567891"},
            {"timestamp-1701234567890123456", "timestamp-1701234567890123457"},
            {"hash-999999999999999999999999999999", "hash-999999999999999999999999999998"}
        };

        for (String[] testCase : cases) {
            int result = comparator.compare(testCase[0], testCase[1]);
            log.debug("  '{}' vs '{}' = {}", testCase[0], testCase[1], result);
            if (result == 0) {
                log.debug("  🚨 严重：长数字被错误地认为相等！");
            }
            Assertions.assertThat(result).isNotEqualTo(0);
        }
    }

    private void testSpecialCharacterRisk() {
        // 测试特殊字符组合，可能在文件路径、配置键等场景出现
        String[][] cases = {
            {"config.1.2.3", "config.1.2.3.bak"},
            {"file-1.2.3.txt", "file-1.2.3.txt.old"},
            {"key.1.2.3.value", "key.1.2.3.value.tmp"},
            {"path/to/file-1.2.3", "path/to/file-1.2.3.backup"}
        };

        for (String[] testCase : cases) {
            int result = comparator.compare(testCase[0], testCase[1]);
            log.debug("  '{}' vs '{}' = {}", testCase[0], testCase[1], result);
            if (result == 0) {
                log.debug("  🚨 严重：特殊字符组合被错误地认为相等！");
            }
            Assertions.assertThat(result).isNotEqualTo(0);
        }
    }

    private void testUnicodeRisk() {
        // 测试Unicode字符，可能在国际化场景中出现
        String[][] cases = {
            {"文件-1.txt", "文件-2.txt"},
            {"用户-123", "用户-124"},
            {"配置-1.2.3", "配置-1.2.4"},
            {"测试-001", "测试-002"}
        };

        for (String[] testCase : cases) {
            int result = comparator.compare(testCase[0], testCase[1]);
            log.debug("  '{}' vs '{}' = {}", testCase[0], testCase[1], result);

            if (result == 0) {
                log.debug("  🚨 严重：Unicode字符被错误地认为相等！");
            }
            Assertions.assertThat(result).isNotEqualTo(0);
        }
    }

    private void testBoundaryConditionRisk() {
        // 测试边界条件，这些是最容易出问题的地方
        String[][] cases = {
            {"", "a"},  // 空字符串
            {"a", ""},  // 空字符串
            {"1", "1a"}, // 数字后跟字母
            {"a1", "a1b"}, // 字母数字后跟字母
            {"1.0", "1.0.0"}, // 版本号层级
            {"test", "test1"}, // 前缀相同
            {"test1", "test"}, // 前缀相同反向
        };

        for (String[] testCase : cases) {
            int result = comparator.compare(testCase[0], testCase[1]);
            log.debug("  '{}' vs '{}' = {}", testCase[0], testCase[1], result);

            if (result == 0) {
                log.debug("  🚨 严重：边界条件被错误地认为相等！");
            }
            Assertions.assertThat(result).isNotEqualTo(0);
        }
    }

    @Test
    void testIndexMapBehavior() {
        // Test the behavior of the index map with the comparator

        // 模拟真实的索引场景
        TreeMap<String, List<String>> indexMap = new TreeMap<>(comparator);

        // 添加一些可能有问题的键
        String[] problematicKeys = {
            "file-001.txt", "file-1.txt",
            "user-0123", "user-123",
            "config-1.2.3", "config-1.2.3.bak",
            "backup-007", "backup-7"
        };

        for (String key : problematicKeys) {
            indexMap.put(key, Arrays.asList("object1", "object2"));
        }

        log.debug("索引Map大小: " + indexMap.size());
        log.debug("期望大小: " + problematicKeys.length);

        if (indexMap.size() != problematicKeys.length) {
            log.debug("实际键: " + indexMap.keySet());
            log.debug("期望键: " + Arrays.asList(problematicKeys));
        }
        Assertions.assertThat(indexMap.size()).isEqualTo(problematicKeys.length);
        // 验证所有键都能被检索到
        for (String key : problematicKeys) {
            if (!indexMap.containsKey(key)) {
                log.debug("🚨 严重：键 '" + key + "' 无法被检索到！");
            }
            Assertions.assertThat(indexMap.containsKey(key)).isTrue();
        }
    }
}