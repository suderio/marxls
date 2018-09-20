package marxls;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.DateUtil;

public final class ConverterFactory {

	private ConverterFactory() {
	}

	@SuppressWarnings("unchecked")
	private static final <T> Function<String, T> converter(Class<?> klazz) {
		if (klazz.isEnum()) {
			return a -> {
				try {
					return (T) klazz.getMethod("valueOf", String.class).invoke(klazz, a);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					throw new IllegalArgumentException("Unable to find enum " + klazz.getName());
				}
			};
		} else /* if (isJavaBean(klazz)) */ {
			// TODO ???
			return a -> (T) a.toString();
		}
	}

	@SuppressWarnings("unchecked")
	private static final <T> Function<String, T> converter(String klazz) {
		if ("integer".equals(klazz)) {
			return a -> (T) Integer.valueOf(a);
		} else if ("date".equals(klazz)) {
			return value -> (T) DateUtil.getJavaDate(Double.valueOf(value), true);
		} else if ("decimal(2)".equals(klazz)) {
			return a -> (T) new BigDecimal(a).setScale(2, RoundingMode.HALF_UP);
			// TODO
		} else if ("decimal(8)".equals(klazz)) {
			return a -> (T) new BigDecimal(a).setScale(8, RoundingMode.HALF_UP);
		} else if ("double".equals(klazz)) {
			return a -> (T) Double.valueOf(a);
		} else {
			return a -> (T) a.toString();
		}
	}

	public static final <T> Function<String, T> converter(Member member) {
		try {
			return converter(Class.forName(member.getConverter()));
		} catch (ClassNotFoundException | NullPointerException e) {
			return converter(member.getConverter());
		}
	}
}
