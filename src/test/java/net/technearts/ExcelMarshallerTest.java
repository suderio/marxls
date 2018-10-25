package net.technearts;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.technearts.ExcelMarshaller;

public class ExcelMarshallerTest {

  private ExcelMarshaller marshaller;

  @Before
  public void setup() {
    try {
      marshaller =
          ExcelMarshaller.create(new File(this.getClass().getResource("exemplo.yml").toURI()));
      marshaller.read(new File(this.getClass().getResource("Pasta2.xlsx").toURI()));
    } catch (URISyntaxException | IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testSimpleMembers() {
    Map<Integer, SimpleEntity> p = marshaller.get(SimpleEntity.class);
    assertThat(p.size(), equalTo(3));
    assertThat(p.get(4).getInteger(), equalTo(45));
  }

  @Test
  public void testEnum() {
    Map<Integer, Address> p = marshaller.get(Address.class);
    assertThat(p.size(), equalTo(2));
    assertThat(p.get(2).getText(), equalTo("Texto 1"));
  }

  @Test
  public void testBeanMemberWithoutReference() {
    Map<Integer, Place> p = marshaller.get(Place.class);
    assertThat(p.size(), equalTo(2));
    assertThat(p.get(2).getDescription(), equalTo("teste 1"));
  }

  @Test
  public void testMemberWithMultiValue() {
    Map<Integer, PlaceTags> p = marshaller.get(PlaceTags.class);
    assertThat(p.size(), equalTo(3));
    assertThat(p.get(4).getTags().size(), equalTo(2));
    assertThat(p.get(5).getTags().size(), equalTo(1));
    assertThat(p.get(6).getTags().size(), equalTo(0));
    assertThat(p.get(4).getPlaces().size(), equalTo(2));
    assertThat(p.get(5).getPlaces().size(), equalTo(1));
    assertThat(p.get(6).getPlaces().size(), equalTo(0));
  }

  @Test
  public void testMemberWithSimpleValue() {
    Map<Integer, MainEntity> p = marshaller.get(MainEntity.class);
    assertThat(p.size(), equalTo(3));
  }
}
