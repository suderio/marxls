package marxls;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class SimpleEntity {
	private Date date;
	private Integer integer;
	private BigDecimal decimal;
	private Double doubleNumber;
	private String text;
}
