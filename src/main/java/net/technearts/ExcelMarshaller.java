package net.technearts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.technearts.ExcelFile.ExcelSheet;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Predicate;

import static org.apache.poi.ss.util.CellReference.convertColStringToIndex;

public class ExcelMarshaller {

  private static final Logger LOG = LoggerFactory.getLogger("Marshaller");

  private Mappings mappings;
  private Set<String> sheets;
  private String separator;
  private Predicate<Row> rowFilter;
  private boolean skipTitle;

  private Repository repo;

  private ExcelMarshaller(File yaml, String separator, Predicate<Row> rowFilter, boolean skipTitle)
      throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    this.mappings = mapper.readValue(yaml, Mappings.class);
    this.sheets = new HashSet<>();
    this.separator = separator;
    this.rowFilter = rowFilter;
    this.skipTitle = skipTitle;
    this.repo = new Repository();
  }

  private ExcelMarshaller(Mappings mappings, String separator, Predicate<Row> rowFilter,
      boolean skipTitle) {
    this.mappings = mappings;
    this.sheets = new HashSet<>();
    this.separator = separator;
    this.rowFilter = rowFilter;
    this.skipTitle = skipTitle;
    this.repo = new Repository();
  }

  private ExcelMarshaller(Mappings mappings, Predicate<Row> rowFilter, boolean skipTitle) {
    this(mappings, ";", rowFilter, skipTitle);
  }

  private ExcelMarshaller(File yaml, Predicate<Row> rowFilter, boolean skipTitle)
      throws IOException {
    this(yaml, ";", rowFilter, skipTitle);
  }

  public static ExcelMarshaller create(File file) throws IOException {
    return create(file, any -> true, true);
  }

  public static ExcelMarshaller create(@NotNull Mappings mappings) {
    return create(mappings, any -> true, true);
  }

  public static ExcelMarshaller create(File file, Predicate<Row> rowFilter, boolean skipTitle)
      throws IOException {
    ExcelMarshaller marshaller = new ExcelMarshaller(file, rowFilter, skipTitle);
    for (Mapping mapping : marshaller.mappings.getMappings()) {
      marshaller.sheets.add(mapping.getSheet());
    }
    return marshaller;
  }

  public static ExcelMarshaller create(Mappings mappings, Predicate<Row> rowFilter,
      boolean skipTitle) {
    ExcelMarshaller marshaller = new ExcelMarshaller(mappings, rowFilter, skipTitle);
    for (Mapping mapping : marshaller.mappings.getMappings()) {
      marshaller.sheets.add(mapping.getSheet());
    }
    return marshaller;
  }

  public void read(File xls) {
    ExcelSheet sheet;
    Class<?> klazz = null;
    try (ExcelFile file = new ExcelFile(xls)) {
      for (Mapping mapping : this.mappings.getMappings()) {
        sheet = file.sheet(mapping.getSheet());
        klazz = Class.forName(mapping.getClassName());
        for (int line : getLines(sheet)) {
          Object entity = klazz.newInstance();
          for (Member member : mapping.getMembers()) {
            setMember(sheet, line, entity, member);
          }
          repo.put(mapping, line, entity);
        }
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Arquivo excel não encontrado.", e);
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      throw new IllegalArgumentException("Erro ao instanciar a classe " + klazz.getName(), e);
    }
  }

  private final void setMember(ExcelSheet sheet, int line, Object entity, Member member) {

    try {
      if (member.isMapped()) {
        if (member.isReferenceBased()) {
          sheet.read(line, column(sheet, member), ConverterFactory.converter(member), value -> repo
              .set(entity, member, value, this.separator, this.mappings.getMappings()));
        } else {
          repo.set(entity, member, repo.get(member.getConverterName(), line), this.separator,
              this.mappings.getMappings());
        }
      } else {
        sheet.read(line, column(sheet, member), value -> value,
            value -> repo.set(member, line, value));
      }
    } catch (IllegalArgumentException e) {
      LOG.debug("A coluna referente à " + member.getProperty() + " não foi encontrada.");
    } catch (NullPointerException e) {
      LOG.debug("### A coluna referente à " + member.getProperty() + " não foi encontrada. ###");
    }
  }

  private final int column(ExcelSheet sheet, Member member) {
    try {
      return member.isTitleBased()
          ? sheet.getColumn(cell -> CellType.STRING.equals(cell.getCellTypeEnum())
              && cell.getStringCellValue().equalsIgnoreCase(member.getTitle()))
          : convertColStringToIndex(member.getColumn());
    } catch (Exception e) {
      throw new IllegalArgumentException("Coluna "
          + (member.isTitleBased() ? member.getTitle() : member.getColumn()) + " não encontrada.",
          e);
    }
  }

  private final SortedSet<Integer> getLines(ExcelSheet sheet) {
    SortedSet<Integer> rows =
        sheet.getRows(row -> row.getFirstCellNum() >= 0 && rowFilter.test(row));
    return rows.isEmpty() ? rows : skipTitle ? rows.tailSet(rows.first() + 1) : rows;
  }

  @SuppressWarnings("unchecked")
  public final <T> Map<Integer, T> get(Class<T> klazz) {
    return (Map<Integer, T>) repo.get(klazz);
  }

  public void addBeanFilter(Predicate<Object> predicate) {
    repo.addFilter(predicate);
  }
}
