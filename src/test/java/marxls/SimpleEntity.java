package marxls;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class SimpleEntity {
	private Date date;
	private int integer;
	private BigDecimal decimal;
	private double doubleNumber;
	private String text;
}
