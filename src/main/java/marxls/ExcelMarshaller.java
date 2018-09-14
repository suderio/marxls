package marxls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import marxls.ExcelFile.ExcelSheet;

public class ExcelMarshaller {

	private Mappings mappings;
	private Set<String> sheets;

	private ExcelMarshaller(File yaml) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		this.mappings = mapper.readValue(yaml, Mappings.class);
		this.sheets = new HashSet<>();
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

	public void read(File xls) throws FileNotFoundException, IOException, ClassNotFoundException {
		try (ExcelFile file = new ExcelFile(xls)) {
			ExcelSheet sheet;
			for (Mapping mapping : this.mappings.getMappings()) {
				sheet = file.sheet(mapping.getSheet());
				
				for (int line : getLines()) {
					for (Property property : mapping.getProperties()) {
						sheet.read(line, column(property), function(property), consumer(property));
					}
				}
			}
		}
	}

	private <T> Consumer<T> consumer(Property property) {
		// TODO Auto-generated method stub
		return null;
	}

	private <T> Function<String, T> function(Property property) {
		// TODO Auto-generated method stub
		return null;
	}

	private int column(Property property) {
		// TODO Auto-generated method stub
		return 0;
	}

	private List<Integer> getLines() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> List<T> get(Class<T> klazz) {
		// TODO Auto-generated method stub
		return null;
	}

}
