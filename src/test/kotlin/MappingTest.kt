import junit.framework.Assert.fail
import net.technearts.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.net.URISyntaxException

class MappingTest {
    private var marshaller: ExcelMarshaller? = null

    @Before
    fun setup() {
        val mapping = SimpleEntity::class named "simpleEntity" from "Plan1" read listOf(
                "date" column "'D03" converted asDate,
                "integer" column "D04" converted asInteger,
                "decimal" column "D05" converted asDecimal(2),
                "doubleNumber" column "D06" converted asDouble,
                "text" column "D02" converted asString
        )

        val mappings = Mappings()
        mappings.mappings = listOf(mapping)

        try {
            marshaller = ExcelMarshaller.create(mappings)
            marshaller?.read(File(this.javaClass.getResource("/Pasta2.xlsx").toURI()))
        } catch (e: URISyntaxException) {
            fail(e.message)
        } catch (e: IOException) {
            fail(e.message)
        }

    }

    @Test
    fun simpleEntityMappingTest() {

    }
}
