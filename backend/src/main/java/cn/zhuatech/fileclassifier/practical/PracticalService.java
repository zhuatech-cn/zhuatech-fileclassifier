/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.fileclassifier.practical;

import static cn.zhuatech.fileclassifier.practical.PracticalInput.*;

import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@Service
public class PracticalService {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  public Map<String, Object> execute(Map<String, Object> r) {
    validate(r);
    keys(r, "rules", "files");
    var rules = rows(r, "rules", 1, 100);
    Set<String> ids = new HashSet<>();
    for (var rule : rules) {
      keys(rule, "id", "category", "priority", "extensions", "keywords");
      unique(ids, id(rule, "id"));
      id(rule, "category");
      integer(rule, "priority", 0, 100);
      var extensions = strings(rule, "extensions", 0, 50);
      for (String extension : extensions)
        if (!extension.matches("[A-Za-z0-9]{1,20}"))
          throw new IllegalArgumentException("扩展名仅支持字母数字且不含点");
      strings(rule, "keywords", 0, 20);
    }
    Set<String> fileIds = new HashSet<>();
    List<Map<String, Object>> out = new ArrayList<>();
    for (var file : rows(r, "files", 1, 200)) {
      keys(file, "id", "path");
      String id = id(file, "id");
      unique(fileIds, id);
      String path = text(file, "path", 250);
      String normalized =
          java.text.Normalizer.normalize(path, java.text.Normalizer.Form.NFKC).replace('\\', '/');
      if (normalized.startsWith("/")
          || normalized.indexOf(':') >= 0
          || normalized.codePoints().anyMatch(Character::isISOControl))
        throw new IllegalArgumentException("仅支持无控制字符的相对路径");
      for (String part : normalized.split("/", -1))
        if (part.isEmpty() || part.equals(".") || part.equals(".."))
          throw new IllegalArgumentException("路径不能包含空段或穿越");
      String name = normalized.substring(normalized.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);
      int dot = name.lastIndexOf('.');
      String extension = dot <= 0 ? "" : name.substring(dot + 1);
      int best = -1;
      Set<String> categories = new TreeSet<>();
      List<String> matched = new ArrayList<>();
      for (var rule : rules) {
        var extensions =
            strings(rule, "extensions", 0, 50).stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .toList();
        var keywords =
            strings(rule, "keywords", 0, 20).stream().map(PracticalInput::normalize).toList();
        if ((extensions.isEmpty() || extensions.contains(extension))
            && (keywords.isEmpty() || keywords.stream().anyMatch(name::contains))) {
          int priority = integer(rule, "priority", 0, 100);
          if (priority > best) {
            best = priority;
            categories.clear();
            matched.clear();
          }
          if (priority == best) {
            categories.add(rule.get("category").toString());
            matched.add(rule.get("id").toString());
          }
        }
      }
      out.add(
          Map.of(
              "id",
              id,
              "state",
              categories.isEmpty()
                  ? "UNCLASSIFIED"
                  : categories.size() > 1 ? "AMBIGUOUS" : "CLASSIFIED",
              "categories",
              new ArrayList<>(categories),
              "matchedRules",
              matched,
              "extension",
              extension,
              "executableName",
              Set.of("exe", "sh", "bat", "cmd", "ps1", "com").contains(extension)));
    }
    return Map.of("files", out, "fileContentRead", false, "filesMoved", false);
  }
}
