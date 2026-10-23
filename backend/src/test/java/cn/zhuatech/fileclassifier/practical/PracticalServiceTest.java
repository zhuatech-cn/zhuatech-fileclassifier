/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.fileclassifier.practical;

import static org.junit.jupiter.api.Assertions.*;

import java.math.*;
import java.util.*;
import org.junit.jupiter.api.Test;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
class PracticalServiceTest {
  private final PracticalService service = new PracticalService();

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  private Map<String, Object> sample() {
    return Map.<String, Object>ofEntries(
        Map.entry(
            "rules",
            List.of(
                Map.<String, Object>ofEntries(
                    Map.entry("id", "R1"),
                    Map.entry("category", "FINANCE"),
                    Map.entry("priority", 10),
                    Map.entry("extensions", List.of("pdf")),
                    Map.entry("keywords", List.of("invoice"))),
                Map.<String, Object>ofEntries(
                    Map.entry("id", "R2"),
                    Map.entry("category", "ARCHIVE"),
                    Map.entry("priority", 1),
                    Map.entry("extensions", List.of("pdf")),
                    Map.entry("keywords", List.of())))),
        Map.entry(
            "files",
            List.of(
                Map.<String, Object>ofEntries(
                    Map.entry("id", "F1"), Map.entry("path", "docs/Invoice-2026.PDF")),
                Map.<String, Object>ofEntries(
                    Map.entry("id", "F2"), Map.entry("path", "photo.jpg")))));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void computesActualFeatureResult() {
    var result = service.execute(sample());
    assertEquals("CLASSIFIED", ((Map<?, ?>) ((List<?>) result.get("files")).get(0)).get("state"));
    assertEquals(
        List.of("FINANCE"), ((Map<?, ?>) ((List<?>) result.get("files")).get(0)).get("categories"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsFeatureBoundary() {
    var invalid = new HashMap<>(sample());
    invalid.put("files", List.of());
    assertThrows(IllegalArgumentException.class, () -> service.execute(invalid));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void featureBoundary1() {
    var r = new HashMap<>(sample());
    r.put("files", List.of(Map.of("id", "X", "path", "../secret.pdf")));
    assertThrows(IllegalArgumentException.class, () -> service.execute(r));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void featureBoundary2() {
    var r = new HashMap<>(sample());
    var a = (Map<String, Object>) ((List<?>) r.get("rules")).get(0);
    var b = new HashMap<>(a);
    b.put("id", "R3");
    b.put("category", "OTHER");
    r.put("rules", List.of(a, b));
    assertEquals(
        "AMBIGUOUS", ((Map<?, ?>) ((List<?>) service.execute(r).get("files")).get(0)).get("state"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void featureBoundary3() {
    assertEquals(
        "UNCLASSIFIED",
        ((Map<?, ?>) ((List<?>) service.execute(sample()).get("files")).get(1)).get("state"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void featureBoundary4() {
    var r = new HashMap<>(sample());
    r.put("files", List.of(Map.of("id", "X", "path", "archive.tar.gz")));
    assertEquals(
        "gz", ((Map<?, ?>) ((List<?>) service.execute(r).get("files")).get(0)).get("extension"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void deterministicWithoutMutatingInput() {
    var input = sample();
    String before = input.toString();
    assertEquals(service.execute(input), service.execute(input));
    assertEquals(before, input.toString());
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsNull() {
    assertThrows(IllegalArgumentException.class, () -> service.execute(null));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsMissingFields() {
    assertThrows(IllegalArgumentException.class, () -> service.execute(Map.of()));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsUnknownFields() {
    var input = new HashMap<>(sample());
    input.put("__unknown", true);
    assertThrows(IllegalArgumentException.class, () -> service.execute(input));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsOversizedText() {
    assertThrows(
        IllegalArgumentException.class, () -> service.execute(Map.of("text", "x".repeat(64001))));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsDeepInput() {
    Map<String, Object> input = Map.of("v", 1);
    for (int i = 0; i < 14; i++) input = Map.of("v", input);
    final var value = input;
    assertThrows(IllegalArgumentException.class, () -> service.execute(value));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsExtremeNumberScale() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PracticalInput.decimal(Map.of("v", new BigDecimal("1e-1000000")), "v", 0, 1));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            PracticalInput.decimal(
                Map.of("v", new BigDecimal("0.1234567890123456789012345678901")), "v", 0, 1));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsUnsupportedType() {
    assertThrows(
        IllegalArgumentException.class, () -> PracticalInput.validate(Map.of("v", new Object())));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsOversizedArray() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PracticalInput.validate(Map.of("v", Collections.nCopies(501, 1))));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void disabledWithoutConfiguredToken() {
    assertEquals(
        503, new PracticalController(service, "").compute("{}", null).getStatusCode().value());
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void shortConfigurationCannotEnableAccess() {
    assertEquals(
        503,
        new PracticalController(service, "short")
            .compute("{}", "Bearer short")
            .getStatusCode()
            .value());
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsNormalizedBlank() {
    assertThrows(IllegalArgumentException.class, () -> PracticalInput.normalize("\u00a0"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void parserPreservesDecimalPrecision() {
    var parsed = PracticalInput.parseJson("{\"v\":0.123456789012}");
    assertEquals(new BigDecimal("0.123456789012"), parsed.get("v"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void parserRejectsDuplicateFields() {
    assertThrows(
        IllegalArgumentException.class, () -> PracticalInput.parseJson("{\"v\":1,\"v\":2}"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void decimalResponsesAreLosslessStrings() {
    var safe =
        (Map<?, ?>)
            PracticalInput.jsonSafe(Map.of("amount", new BigDecimal("9007199254740993.01")));
    assertEquals("9007199254740993.01", safe.get("amount"));
  }
}
