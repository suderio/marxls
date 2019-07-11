package net.technearts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class Repository {
  private static final Logger LOG = LoggerFactory.getLogger("Marshaller");
  private Table<Mapping, Integer, Object> table = HashBasedTable.create();
  private Table<Member, Integer, String> nonMappedMembers = HashBasedTable.create();
  private List<Predicate<Object>> entityPredicates = new ArrayList<>();

  public void put(Mapping mapping, int line, Object value) {
    if (entityPredicates.stream().allMatch(predicate -> predicate.test(value))) {
      table.put(mapping, line, value);
    }
  }

  public Object get(String name, int line) {
    if (name == null) {
      throw new IllegalArgumentException("Nome não pode ser nulo");
    }
    return table.column(line).entrySet().stream()
        .filter(entry -> name.equals(entry.getKey().getName())).findFirst().orElse(null);
  }

  public Map<Integer, Object> get(String name) {
    for (Entry<Mapping, Map<Integer, Object>> entry : table.rowMap().entrySet()) {
      if (name.equals(entry.getKey().getName())) {
        return entry.getValue();
      }
    }
    return null;
  }

  public Map<Integer, Object> get(Class<?> klazz) {
    for (Entry<Mapping, Map<Integer, Object>> entry : table.rowMap().entrySet()) {
      if (klazz.getName().equals(entry.getKey().getClassName())) {
        return entry.getValue();
      }
    }
    return null;
  }

  public void addFilter(Predicate<Object> predicate) {
    entityPredicates.add(predicate);
  }

  @SuppressWarnings("unchecked")
  public final <T, V> void set(final T bean, final Member member, final V value, String separator,
      List<Mapping> mappings) {
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
        // embedded
        if (!member.isReferenceBased()) {
          c.add(value);
          return;
        }
        // multivalued reference
        for (String o : value.toString().split(separator)) {
          if (StringUtils.isBlank(mappedBy)) {
            c.add((V) o);
          } else {
            for (Object v : this.get(converter).values()) {
              try {
                String property = mappedBy;
                Optional<Mapping> optionalMapping = mappings.stream()
                    .filter(mapping -> mapping.getName().equals(converter)).findFirst();
                if (optionalMapping.isPresent()) {
                  Member m = optionalMapping.get().getMember(mappedBy);
                  if (m != null) {
                    property = m.getProperty();
                  } else {
                    // TODO ?
                  }
                } else {
                  // TODO ?
                }
                String element = PropertyUtils.getProperty(v, property).toString().trim();
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

  @SuppressWarnings("unchecked")
  private <T> T newCollectionInstance(Class<T> klass) {
    T t = null;
    try {
      t = klass.newInstance();
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

  public <T, V> void set(Member member, int line, V value) {
    nonMappedMembers.put(member, line, value.toString());
  }
}
