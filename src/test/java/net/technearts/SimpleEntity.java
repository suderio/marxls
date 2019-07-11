package net.technearts;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SimpleEntity {
  private Date date;
  private Integer integer;
  private BigDecimal decimal;
  private Double doubleNumber;
  private String text;
}
