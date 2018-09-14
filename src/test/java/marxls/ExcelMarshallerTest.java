package marxls;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ExcelMarshallerTest {

	private ExcelMarshaller marshaller;

	@Before
	public void setup() {
		try {
			marshaller = ExcelMarshaller.create(new File(this.getClass().getResource("exemplo.yml").toURI()));
			marshaller.read(new File(this.getClass().getResource("Pasta2.xlsx").toURI()));
		} catch (URISyntaxException | IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void test() {
		List<SimpleEntity> p = marshaller.get(SimpleEntity.class);
		assertThat(p.size(), equalTo(3));
	}

}
