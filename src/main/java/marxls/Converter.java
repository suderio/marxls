package marxls;

import java.util.function.Function;

@FunctionalInterface
public interface Converter<T> extends Function<String, T> {

}
