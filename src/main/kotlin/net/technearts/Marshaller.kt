package net.technearts

import lombok.extern.log4j.Log4j2
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.util.CellReference.convertColStringToIndex
import java.io.File
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.collections.HashSet

@Log4j2
data class Marshaller(val mappings: Mappings, val separator: String = ";", val rowFilter: (Row) -> Boolean = { _ -> true }, val skipTitle: Boolean = false) {
    private val sheets: Set<String> = HashSet()
    private val repo: Repository = Repository()

    fun read(xls: File) {
        var sheet: ExcelFile.ExcelSheet
        var klazz: Class<*>? = null
        try {
            ExcelFile(xls).use { file ->
                for (mapping in this.mappings.mappings!!) {
                    sheet = file.sheet(mapping.sheetName)
                    klazz = Class.forName(mapping.className)
                    for (line in getLines(sheet)) {
                        val entity = klazz!!.newInstance()
                        for (member in mapping.members!!) {
                            setMember(sheet, line, entity, member)
                        }
                        repo.put(mapping, line, entity)
                    }
                }
            }
        } catch (e: IOException) {
            throw IllegalArgumentException("Arquivo excel não encontrado.", e)
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Erro ao instanciar a classe " + klazz!!.name, e)
        } catch (e: IllegalAccessException) {
            throw IllegalArgumentException("Erro ao instanciar a classe " + klazz!!.name, e)
        } catch (e: InstantiationException) {
            throw IllegalArgumentException("Erro ao instanciar a classe " + klazz!!.name, e)
        }

    }

    private fun setMember(sheet: ExcelFile.ExcelSheet, line: Int, entity: Any, member: Member) {
        try {
            if (member.isMapped) {
                if (member.isReferenceBased) {
                    sheet.read(line, column(sheet, member), ConverterFactory.converter<Any>(member),
                            Consumer { value: Any ->
                                repo.set(entity, member, value, this.separator, this.mappings.mappings)
                            })
                } else {
                    repo.set(entity, member, repo.get(member.converterName, line), this.separator,
                            this.mappings.mappings)
                }
            } else {
                sheet.read(line, column(sheet, member), { value -> value },
                        { value -> repo.set<Any, String>(member, line, value) })
            }
        } catch (e: IllegalArgumentException) {
            //log.debug("A coluna referente à " + member.property + " não foi encontrada.")
        } catch (e: NullPointerException) {
            //log.debug("### A coluna referente à " + member.property + " não foi encontrada. ###")
        }

    }

    private fun column(sheet: ExcelFile.ExcelSheet, member: Member): Int {
        try {
            return if (member.isTitleBased)
                sheet.getColumn { cell -> CellType.STRING == cell.cellTypeEnum && cell.stringCellValue.equals(member.title!!, ignoreCase = true) }
            else
                convertColStringToIndex(member.column!!)
        } catch (e: Exception) {
            throw IllegalArgumentException("Coluna "
                    + (if (member.isTitleBased) member.title else member.column) + " não encontrada.",
                    e)
        }

    }

    private fun getLines(sheet: ExcelFile.ExcelSheet): SortedSet<Int> {
        val rows = sheet.getRows { row -> row.firstCellNum >= 0 && rowFilter(row) }
        return if (rows.isEmpty()) rows else if (skipTitle) rows.tailSet(rows.first() + 1) else rows
    }

    operator fun <T> get(klazz: Class<T>): Map<Int, T> {
        return repo.get(klazz) as Map<Int, T>
    }

    fun addBeanFilter(predicate: Predicate<Any>) {
        repo.addFilter(predicate)
    }
}
