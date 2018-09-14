package marxls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.beanutils.PropertyUtils;
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

	public void read(File xls) throws FileNotFoundException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		try (ExcelFile file = new ExcelFile(xls)) {
			ExcelSheet sheet;
			Class<?> klazz;
			for (Mapping mapping : this.mappings.getMappings()) {
				sheet = file.sheet(mapping.getSheet());
				klazz = Class.forName(mapping.getClassName());
				repository.put(klazz, new ArrayList<>());
				for (int line : getLines()) {
					Object entity = klazz.newInstance();
					for (Property property : mapping.getProperties()) {
						sheet.read(line, column(property), function(property), value -> {
							try {
								PropertyUtils.setProperty(entity, property.getProperty(), value);
							} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						});
					}
					repository.get(klazz).add(entity);
				}
			}
		}
	}

	private <T> Function<String, T> function(Property property) {
		if ("integer".equals(property.getConverter())) {
			return a -> (T) Integer.valueOf(a);
		} else if ("date".equals(property.getConverter())) {
			return value -> {
			    boolean date1904 = true;
			    Date dataReferencia = DateUtil.getJavaDate(Double.valueOf(value), date1904);
			    LocalDate localDataRef = null;
			    if (dataReferencia != null) {
			      localDataRef = dataReferencia.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			    }
			    return (T) localDataRef;
			  };
		} else if ("decimal(2)".equals(property.getConverter())) {
			return a -> (T) new BigDecimal(a).setScale(2);
		} else if ("decimal(8)".equals(property.getConverter())) {
			return a -> (T) new BigDecimal(a).setScale(8);
		} else {
			return a -> (T) a;
		}
	}

	private int column(Property property) {
		// TODO Auto-generated method stub
		return 0;
	}

	private List<Integer> getLines() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> get(Class<T> klazz) {
		return (List<T>) repository.get(klazz);
	}

}
