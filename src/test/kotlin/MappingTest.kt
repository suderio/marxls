import net.technearts.SimpleEntity
import net.technearts.named
import net.technearts.from
import net.technearts.read
import net.technearts.*
import org.apache.poi.ss.usermodel.DateUtil
import org.junit.Test

class MappingTest {
    /*
        # tipos diferentes
    name: simpleEntity
    className: net.technearts.SimpleEntity
    sheet: Plan1
    members:
    - {title: D03, converter: date, property: date}
    - {title: D04, converter: integer, property: integer}
    - {title: D05, converter: decimal(2), property: decimal}
    - {title: D06, converter: double, property: doubleNumber}
    - {title: D02, property: text}
     */
    @Test
    fun simpleEntityMappingTest() {
        SimpleEntity::class named "simpleEntity" from "Plan1" read (listOf(
                "date" column "'D03" converted { a -> DateUtil.getJavaDate(java.lang.Double.valueOf(a), true) },
                "integer" column "D04" converted { a: String -> Integer.valueOf(a) })
                )
    }
}

