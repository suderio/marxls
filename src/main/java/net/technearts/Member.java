package net.technearts;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import lombok.Data;

@Data
public class Member {
  private String title;
  private String column;
  private String converter;
  private String property;
  private String mappedBy;
  private boolean mapped = true;

  public boolean isTitleBased() {
    return isNotBlank(title);
  }

  public boolean isReferenceBased() {
    return isNotBlank(title) || isNotBlank(column);
  }
}
