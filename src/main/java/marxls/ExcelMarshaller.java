package marxls;

import static org.apache.poi.ss.util.CellReference.convertColStringToIndex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
	private Map<Class<?>, List<Object>> repository;

	private ExcelMarshaller(File yaml) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		this.mappings = mapper.readValue(yaml, Mappings.class);
		this.sheets = new HashSet<>();
		this.repository = new HashMap<>();
	}

	public static ExcelMarshaller create(File file) throws JsonParseException, JsonMappingException, IOException {
		ExcelMarshaller marshaller = new ExcelMarshaller(file);
		for (Mapping mapping : marshaller.mappings.getMappings()) {
			mapping.getClassName();
			mapping.getName();
			marshaller.sheets.add(mapping.getSheet());
			for (Property property : mapping.getProperties()) {
				property.getColumn();
				property.getConverter();
				property.getProperty();
				property.getTitle();
			}
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
				repository.put(klazz, new ArrayList<>());
				for (int line : getLines(sheet)) {
					Object entity = klazz.newInstance();
					for (Property property : mapping.getProperties()) {
						try {
							System.out.println("Lendo " + line + ", " + column(sheet, property) + ": "
									+ property.getTitle() + ", " + property.getProperty());
							sheet.read(line, column(sheet, property), function(property), value -> {
								try {
									PropertyUtils.setProperty(entity, property.getProperty(), value);
								} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
									System.out.println("A propriedade " + property.getProperty()
											+ " não foi encontrada em " + mapping.getClassName());
								}
							});
						} catch (IllegalArgumentException | NullPointerException e) {
							System.out
									.println("A coluna referente à " + property.getProperty() + " não foi encontrada.");
						}
					}
					repository.get(klazz).add(entity);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Function<String, T> function(Property property) {
		if ("integer".equals(property.getConverter())) {
			return a -> (T) Integer.valueOf(a);
		} else if ("date".equals(property.getConverter())) {
			return value -> (T) DateUtil.getJavaDate(Double.valueOf(value), true);
		} else if ("decimal(2)".equals(property.getConverter())) {
			return a -> (T) new BigDecimal(a).setScale(2, RoundingMode.HALF_UP);
		} else if ("decimal(8)".equals(property.getConverter())) {
			return a -> (T) new BigDecimal(a).setScale(8, RoundingMode.HALF_UP);
		} else if ("double".equals(property.getConverter())) {
			return a -> (T) Double.valueOf(a);
		} else {
			return a -> (T) a;
		}
	}

	private int column(ExcelSheet sheet, Property property) {
		try {
			return property.isTitleBased()
					? sheet.getColumn(cell -> CellType.STRING.equals(cell.getCellTypeEnum())
							&& cell.getStringCellValue().equalsIgnoreCase(property.getTitle()))
					: convertColStringToIndex(property.getColumn());
		} catch (Exception e) {
			throw new IllegalArgumentException("Coluna "
					+ (property.isTitleBased() ? property.getTitle() : property.getColumn()) + " não encontrada.", e);
		}
	}

	private SortedSet<Integer> getLines(ExcelSheet sheet) {
		SortedSet<Integer> rows = sheet.getRows(row -> row.getFirstCellNum() >= 0);
		return rows.isEmpty() ? rows : rows.tailSet(rows.first() + 1);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> get(Class<T> klazz) {
		return (List<T>) repository.get(klazz);
	}

}
