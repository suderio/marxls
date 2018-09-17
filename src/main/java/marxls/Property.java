package marxls;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import lombok.Data;

@Data
public class Property {
	private String title;
	private String column;
	private String converter;
	private String property;

	public boolean isTitleBased() {
		return isNotBlank(title);
	}

	public boolean isReferenceBased() {
		return isBlank(title) && isBlank(column);
	}
}