package marxls;

import static org.apache.poi.ss.util.CellReference.convertColStringToIndex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import marxls.ExcelFile.ExcelSheet;

public class ExcelMarshaller {

	private Mappings mappings;
	private Set<String> sheets;
	private Map<Class<?>, Map<Integer, Object>> repository;
	private String separator;

	private ExcelMarshaller(File yaml, String separator) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		this.mappings = mapper.readValue(yaml, Mappings.class);
		this.sheets = new HashSet<>();
		this.repository = new HashMap<>();
		this.separator = separator;
	}

	private ExcelMarshaller(File yaml) throws JsonParseException, JsonMappingException, IOException {
		this(yaml, ";");
	}

	public static ExcelMarshaller create(File file) throws JsonParseException, JsonMappingException, IOException {
		ExcelMarshaller marshaller = new ExcelMarshaller(file);
		for (Mapping mapping : marshaller.mappings.getMappings()) {
			marshaller.sheets.add(mapping.getSheet());
		}
		return marshaller;
	}

	public void read(File xls) throws FileNotFoundException, IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		try (ExcelFile file = new ExcelFile(xls)) {
			ExcelSheet sheet;
			Class<?> klazz;
			for (Mapping mapping : this.mappings.getMappings()) {
				sheet = file.sheet(mapping.getSheet());
				klazz = Class.forName(mapping.getClassName());
				repository.put(klazz, new HashMap<>());
				for (int line : getLines(sheet)) {
					Object entity = klazz.newInstance();
					for (Member member : mapping.getMembers()) {
						setMember(sheet, line, entity, member);
					}
					repository.get(klazz).put(line, entity);
				}
			}
		}
	}

	private final void setMember(ExcelSheet sheet, int line, Object entity, Member member) {
		try {
			System.out.println("Lendo " + line + ", " + column(sheet, member) + ": " + member.getTitle() + ", "
					+ member.getProperty());
			if (member.isReferenceBased()) {
				sheet.read(line, column(sheet, member), converter(member),
						value -> setProperty(entity, member.getProperty(), value));
			} else {
				Class<?> key = getClass(member.getConverter());
				setProperty(entity, member.getProperty(), repository.get(key).get(line));
			}
		} catch (IllegalArgumentException | NullPointerException e) {
			System.out.println("A coluna referente à " + member.getProperty() + " não foi encontrada.");
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T newCollectionInstance(Class<T> klass) {
		T t = null;
		try {
			t = (T) klass.newInstance();
		} catch (NullPointerException | InstantiationException | IllegalAccessException e) {
			// BeanContext, BeanContextServices, BlockingDeque<E>, BlockingQueue<E>,
			// Deque<E>, List<E>, NavigableSet<E>, Queue<E>, Set<E>, SortedSet<E>,
			// TransferQueue<E>
			if (Arrays.asList(klass.getInterfaces()).contains(List.class)) {
				t = (T) newCollectionInstance(ArrayList.class);
			}
		}
		return t;
	}

	@SuppressWarnings("unchecked")
	private final <T, V> void setProperty(T bean, String name, V value) {
		try {
			Class<T> klass = (Class<T>) PropertyUtils.getPropertyType(bean, name);
			if (Arrays.asList(klass.getInterfaces()).contains(Collection.class)) {
				Collection<V> c = (Collection<V>) PropertyUtils.getProperty(bean, name);
				if (c == null) {
					c = (Collection<V>) newCollectionInstance(klass);
					PropertyUtils.setProperty(bean, name, c);
				}
				for (String o : value.toString().split(this.separator)) {
					PropertyUtils.setIndexedProperty(bean, name, c.size(), o);
				}
			} else {
				PropertyUtils.setProperty(bean, name, value);
			}
		} catch (ReflectiveOperationException e) {
			System.out.println("A propriedade " + name + " não foi encontrada em " + bean.getClass().getSimpleName());
		}
	}

	private final Class<?> getClass(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException | NullPointerException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private final <T> Function<String, T> converter(Member member) {
		Class<?> klazz;
		if ((klazz = getClass(member.getConverter())) != null) {
			if (klazz.isEnum()) {
				return a -> {
					try {
						return (T) klazz.getMethod("valueOf", String.class).invoke(klazz, a);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| NoSuchMethodException | SecurityException e) {
						return null;
					}
				};
			} else {
				// TODO ???
				return null;
			}
		} else if ("integer".equals(member.getConverter())) {
			return a -> (T) Integer.valueOf(a);
		} else if ("date".equals(member.getConverter())) {
			return value -> (T) DateUtil.getJavaDate(Double.valueOf(value), true);
		} else if ("decimal(2)".equals(member.getConverter())) {
			return a -> (T) new BigDecimal(a).setScale(2, RoundingMode.HALF_UP);
		} else if ("decimal(8)".equals(member.getConverter())) {
			return a -> (T) new BigDecimal(a).setScale(8, RoundingMode.HALF_UP);
		} else if ("double".equals(member.getConverter())) {
			return a -> (T) Double.valueOf(a);
		} else {
			return a -> (T) a.toString();
		}
	}

	private final int column(ExcelSheet sheet, Member member) {
		try {
			return member.isTitleBased()
					? sheet.getColumn(cell -> CellType.STRING.equals(cell.getCellTypeEnum())
							&& cell.getStringCellValue().equalsIgnoreCase(member.getTitle()))
					: convertColStringToIndex(member.getColumn());
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Coluna " + (member.isTitleBased() ? member.getTitle() : member.getColumn()) + " não encontrada.",
					e);
		}
	}

	private final SortedSet<Integer> getLines(ExcelSheet sheet) {
		SortedSet<Integer> rows = sheet.getRows(row -> row.getFirstCellNum() >= 0);
		return rows.isEmpty() ? rows : rows.tailSet(rows.first() + 1);
	}

	@SuppressWarnings("unchecked")
	public final <T> Map<Integer, T> get(Class<T> klazz) {
		return (Map<Integer, T>) repository.get(klazz);
	}

}
