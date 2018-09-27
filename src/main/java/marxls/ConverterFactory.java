package marxls;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.DateUtil;

public final class ConverterFactory {

	private static final Pattern DECIMAL = Pattern.compile("decimal\\((\\d+)\\)");
	private ConverterFactory() {
	}

	@SuppressWarnings("unchecked")
	private static final <T> Converter<T> converter(Class<?> klazz) {
		if (klazz.isEnum()) {
			return a -> {
				try {
					return (T) klazz.getMethod("valueOf", String.class).invoke(klazz, a);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					throw new IllegalArgumentException("Unable to find enum " + klazz.getName());
				}
			};
		} else if (ArrayUtils.contains(klazz.getInterfaces(), Converter.class)) {
			return Converter.class.cast(klazz);
		} else {
			return a -> (T) a.toString();
		}
	}

	@SuppressWarnings("unchecked")
	private static final <T> Converter<T> converter(String type) {
		Matcher m;
		if ("integer".equals(type)) {
			return a -> (T) Integer.valueOf(a);
		} else if ("date".equals(type)) {
			return value -> (T) DateUtil.getJavaDate(Double.valueOf(value), true);
		} else if (type != null && (m = DECIMAL.matcher(type)).matches()) {
			int scale = Integer.valueOf(m.group(1));
			return a -> (T) new BigDecimal(a).setScale(scale, RoundingMode.HALF_UP);
		} else if ("double".equals(type)) {
			return a -> (T) Double.valueOf(a);
		} else {
			return a -> (T) a.toString();
		}
	}

	public static final <T> Converter<T> converter(Member member) {
		try {
			return converter(Class.forName(member.getConverter()));
		} catch (ClassNotFoundException | NullPointerException e) {
			return converter(member.getConverter());
		}
	}
}
