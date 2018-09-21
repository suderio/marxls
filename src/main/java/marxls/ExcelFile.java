package marxls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.collect.Streams;

public class ExcelFile implements AutoCloseable {
	private XSSFWorkbook workbook;
	private Map<String, Sheet> sheets;

	public ExcelFile(Path path) throws FileNotFoundException, IOException {
		this(path.toFile());
	}

	public ExcelFile(File file) throws FileNotFoundException, IOException {
		this.workbook = new XSSFWorkbook(new FileInputStream(file));
		sheets = new HashMap<>();
		Iterator<Sheet> i = this.workbook.sheetIterator();
		while (i.hasNext()) {
			Sheet sheet = i.next();
			sheets.put(sheet.getSheetName(), sheet);
		}
	}

	public <T> T read(String sheetName, int line, int column, Converter<T> function, Consumer<T> action) {
		T result = read(sheetName, line, column, function);
		action.accept(result);
		return result;
	}

	public <T> T read(String sheetName, int line, int column, Converter<T> function) {
		String result = read(sheetName, line, column);
		try {
			return function.apply(result);
		} catch (NullPointerException | NumberFormatException e) {
			return null;
		}
	}

	public String read(String sheetName, int line, int column, Consumer<String> action) {
		String result = read(sheetName, line, column);
		action.accept(result);
		return result;
	}

	public String read(String sheetName, int line, int column) {
		String result;
		try {
			Sheet sheet = sheets.get(sheetName);
			Row row = sheet.getRow(line);
			Cell cell = row.getCell(column);
			result = getCellValue(cell);
		} catch (IllegalStateException | NullPointerException e) {
			result = null;
		}
		return result;
	}

	public <T> void write(String sheetName, int line, int column, T value) {
		Sheet sheet = sheets.get(sheetName);
		Row row = sheet.getRow(line);
		if (row == null) {
			row = sheet.createRow(line);
		}
		Cell cell = row.getCell(column);
		if (cell == null) {
			cell = row.createCell(column);
		}
		setCellValue(cell, value);
	}

	public SortedSet<Integer> getRows(String sheetName, Predicate<Row> predicate) {
		Sheet sheet = sheets.get(sheetName);
		return Streams.stream(sheet::iterator).filter(predicate).map(row -> row.getRowNum())
				.collect(Collectors.toCollection(TreeSet::new));
	}

	public int getColumn(String sheetName, Predicate<Cell> predicate) {
		Sheet sheet = sheets.get(sheetName);
		Iterator<Row> rows = sheet.iterator();
		Iterator<Cell> cells;
		Row row;
		Cell cell;
		while (rows.hasNext()) {
			row = rows.next();
			cells = row.cellIterator();
			while (cells.hasNext()) {
				cell = cells.next();
				if (predicate.test(cell)) {
					return cell.getColumnIndex();
				}
			}
		}
		return -1;
	}

	private String getCellValue(Cell cell) {
		if (cell == null) {
			return null;
		}
		switch (cell.getCellTypeEnum()) {
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			return StringUtils.removeEnd(Double.toString(cell.getNumericCellValue()), ".0");
		case BOOLEAN:
			return Boolean.toString(cell.getBooleanCellValue());
		case _NONE:
		case BLANK:
		case FORMULA:
		case ERROR:
		default:
			return null;
		}
	}

	private void setCellValue(Cell cell, Object value) {
		if (value.getClass().isAssignableFrom(Double.class)) {
			cell.setCellValue((double) value);
		} else if (value.getClass().isAssignableFrom(Boolean.class)) {
			cell.setCellValue((boolean) value);
		} else if (value.getClass().isAssignableFrom(Integer.class)) {
			cell.setCellValue((int) value);
		} else {
			cell.setCellValue(value.toString());
		}
	}

	public ExcelSheet sheet(String sheetName) {
		return new ExcelSheet(this, sheetName);
	}

	public void save(Path path) {
		save(path.toFile());
	}

	public void save(File file) {
		try {
			workbook.write(new FileOutputStream(file));
		} catch (IOException e) {
			throw new IllegalArgumentException("Arquivo não existe", e);
		}
	}

	@Override
	public void close() {
		try {
			workbook.close();
		} catch (IOException e) {
			;
		}
	}

	class ExcelSheet {
		private final String sheet;
		private final ExcelFile file;

		private ExcelSheet(ExcelFile file, String sheet) {
			this.sheet = sheet;
			this.file = file;
		}

		public <T> T read(int line, int column, Converter<T> function, Consumer<T> action) {
			return file.read(sheet, line, column, function, action);
		}

		public <T> T read(int line, int column, Converter<T> function) {
			return file.read(sheet, line, column, function);
		}

		public String read(int line, int column, Consumer<String> action) {
			return file.read(sheet, line, column, action);
		}

		public String read(int line, int column) {
			return file.read(sheet, line, column);
		}

		public <T> void write(int line, int column, T value) {
			file.write(sheet, line, column, value);
		}

		public SortedSet<Integer> getRows(Predicate<Row> predicate) {
			return file.getRows(sheet, predicate);
		}

		public String toString() {
			return sheet;
		}

		public int getColumn(Predicate<Cell> predicate) {
			return file.getColumn(sheet, predicate);
		}
	}
}
