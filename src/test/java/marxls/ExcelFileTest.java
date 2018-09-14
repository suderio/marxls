package marxls;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test for simple ExcelFile.
 */
public class ExcelFileTest {

  @Test
  public void testExcelFile() {

    try (ExcelFile xls =
        new ExcelFile(new File(this.getClass().getResource("Pasta1.xlsx").toURI()))) {
      xls.write("Plan1", 0, 0, "A");
      xls.write("Plan1", 0, 1, "B");
      xls.write("Plan1", 0, 2, "C");
      xls.write("Plan1", 1, 0, 1);
      xls.write("Plan1", 1, 1, 2);
      xls.write("Plan1", 1, 2, 3);
      assertEquals(xls.read("Plan1", 0, 0, ExcelFileTest::stringTest), "A");
      assertEquals(xls.read("Plan1", 0, 1, ExcelFileTest::stringTest), "B");
      assertEquals(xls.read("Plan1", 0, 2, ExcelFileTest::stringTest), "C");
      assertEquals(xls.read("Plan1", 1, 0, Double::valueOf, ExcelFileTest::doubleTest), 1.0d,
          0.001d);
      assertEquals(xls.read("Plan1", 1, 1, Double::valueOf, ExcelFileTest::doubleTest), 2.0d,
          0.001d);
      assertEquals(xls.read("Plan1", 1, 2, Double::valueOf, ExcelFileTest::doubleTest), 3.0d,
          0.001d);
    } catch (URISyntaxException | IOException e) {
      fail(e.getMessage());
    }
    assertTrue(true);
  }

  @Test
  public void testExcelSheet() {
    URL url = this.getClass().getResource("Pasta1.xlsx");
    try {
      URI uri = url.toURI();
      try (ExcelFile xls = new ExcelFile(new File(uri))) {
        ExcelFile.ExcelSheet sheet = xls.sheet("Plan2");
        sheet.write(0, 0, "A");
        sheet.write(0, 1, "B");
        sheet.write(0, 2, "C");
        sheet.write(1, 0, 1);
        sheet.write(1, 1, 2);
        sheet.write(1, 2, 3);
        assertEquals(sheet.read(0, 0, ExcelFileTest::stringTest), "A");
        assertEquals(sheet.read(0, 1, ExcelFileTest::stringTest), "B");
        assertEquals(sheet.read(0, 2, ExcelFileTest::stringTest), "C");
        assertEquals(sheet.read(1, 0, Double::valueOf, ExcelFileTest::doubleTest), 1.0d, 0.001d);
        assertEquals(sheet.read(1, 1, Double::valueOf, ExcelFileTest::doubleTest), 2.0d, 0.001d);
        assertEquals(sheet.read(1, 2, Double::valueOf, ExcelFileTest::doubleTest), 3.0d, 0.001d);
      } catch (IOException e) {
        fail(e.getMessage());
      }
    } catch (URISyntaxException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    assertTrue(true);
  }

  static Double doubleTest(Double a) {
    return a;
  }

  static String stringTest(String a) {
    return a;
  }

  static boolean booleanTest(boolean a) {
    return a;
  }
}
