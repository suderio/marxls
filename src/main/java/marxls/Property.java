package marxls;

import lombok.Data;

@Data
public class Property {
	private String title;
	private String column;
	private boolean titleBased;
	private String converter;
	private String property;
	private boolean referenceBased;
}