package marxls;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.poi.ss.util.CellReference.convertColStringToIndex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import marxls.ExcelFile.ExcelSheet;

public class ExcelMarshaller {

  private static final Logger LOG = LoggerFactory.getLogger(ExcelMarshaller.class);

  private Mappings mappings;
  private Set<String> sheets;
  private Map<Class<?>, Map<Integer, Object>> repository;
  private String separator;
  private Predicate<Row> rowFilter;
  private boolean skipTitle;
  private List<Predicate<Object>> entityPredicates = new ArrayList<>();

  private ExcelMarshaller(File yaml, String separator, Predicate<Row> rowFilter, boolean skipTitle)
      throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    this.mappings = mapper.readValue(yaml, Mappings.class);
    this.sheets = new HashSet<>();
    this.repository = new HashMap<>();
    this.separator = separator;
    this.rowFilter = rowFilter;
    this.skipTitle = skipTitle;
  }

  private ExcelMarshaller(File yaml, Predicate<Row> rowFilter, boolean skipTitle)
      throws JsonParseException, JsonMappingException, IOException {
    this(yaml, ";", rowFilter, skipTitle);
  }

  public static ExcelMarshaller create(File file)
      throws JsonParseException, JsonMappingException, IOException {
    return create(file, any -> true, true);
  }

  public static ExcelMarshaller create(File file, Predicate<Row> rowFilter, boolean skipTitle)
      throws JsonParseException, JsonMappingException, IOException {
    ExcelMarshaller marshaller = new ExcelMarshaller(file, rowFilter, skipTitle);
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
        repository.put(klazz, new HashMap<>());
        for (int line : getLines(sheet)) {
          Object entity = klazz.newInstance();
          for (Member member : mapping.getMembers()) {
            setMember(sheet, line, entity, member);
          }
          if (entityPredicates.stream().allMatch(predicate -> predicate.test(entity)))
            repository.get(klazz).put(line, entity);
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
      if (member.isReferenceBased()) {
        sheet.read(line, column(sheet, member), ConverterFactory.converter(member),
            value -> setProperty(entity, member, value));
      } else {
        Class<?> key = getClass(member.getConverter());
        setProperty(entity, member, repository.get(key).get(line));
      }
    } catch (IllegalArgumentException e) {
      LOG.debug("A coluna referente à " + member.getProperty() + " não foi encontrada.");
    } catch (NullPointerException e) {
      LOG.debug("### A coluna referente à " + member.getProperty() + " não foi encontrada. ###");
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T newCollectionInstance(Class<T> klass) {
    T t = null;
    try {
      t = (T) klass.newInstance();
    } catch (NullPointerException | InstantiationException | IllegalAccessException e) {
      if (NavigableSet.class.equals(klass) || Set.class.equals(klass)
          || SortedSet.class.equals(klass)
          || Arrays.asList(klass.getInterfaces()).contains(NavigableSet.class)
          || Arrays.asList(klass.getInterfaces()).contains(Set.class)
          || Arrays.asList(klass.getInterfaces()).contains(SortedSet.class)) {
        t = (T) newCollectionInstance(TreeSet.class);
      } else if (List.class.equals(klass) || Queue.class.equals(klass) || Deque.class.equals(klass)
          || Arrays.asList(klass.getInterfaces()).contains(List.class)
          || Arrays.asList(klass.getInterfaces()).contains(Queue.class)
          || Arrays.asList(klass.getInterfaces()).contains(Deque.class)) {
        t = (T) newCollectionInstance(LinkedList.class);
      }
    }
    return t;
  }

  @SuppressWarnings("unchecked")
  private final <T, V> void setProperty(final T bean, final Member member, final V value) {
    final String name = member.getProperty();
    final String mappedBy = member.getMappedBy();
    final String converter = member.getConverter();
    try {
      Class<T> klass = (Class<T>) PropertyUtils.getPropertyType(bean, name);
      if (Arrays.asList(klass.getInterfaces()).contains(Collection.class)) {
        Collection<V> c = (Collection<V>) PropertyUtils.getProperty(bean, name);
        if (c == null) {
          c = (Collection<V>) newCollectionInstance(klass);
          PropertyUtils.setProperty(bean, name, c);
        }
        if (value == null) {
          return;
        }
        for (String o : value.toString().split(this.separator)) {
          if (isBlank(mappedBy)) {
            c.add((V) o);
          } else {
            Class<?> key = getClass(converter);
            if (key == null) {
              throw new IllegalArgumentException("Classe " + converter + " não é um Bean");
            }
            for (Object v : repository.get(key).values()) {
              try {
                String element = PropertyUtils.getProperty(v, mappedBy).toString().trim();
                if (o.trim().equals(element)) {
                  c.add((V) v);
                }
              } catch (NullPointerException e) {
                throw new IllegalArgumentException(
                    "Mapeamento " + mappedBy + " da classe " + converter + " não encontrado.");
              }
            }
          }
        }
      } else {
        PropertyUtils.setProperty(bean, name, value);
      }
    } catch (ReflectiveOperationException e) {
      LOG.error(
          "A propriedade " + name + " não foi encontrada em " + bean.getClass().getSimpleName());
    }
  }

  private final Class<?> getClass(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException | NullPointerException e) {
      LOG.debug("A classe " + className + " não foi encontrada.");
      return null;
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
    return (Map<Integer, T>) repository.get(klazz);
  }

  public void addBeanFilter(Predicate<Object> predicate) {
    entityPredicates.add(predicate);
  }

}
