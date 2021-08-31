package io.glnt.gpms.io.glnt.gpms.handler.file.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
//import io.glnt.gpms.handler.corp.service.CorpService
import io.glnt.gpms.handler.dashboard.admin.model.reqSearchCorp
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.model.entity.Corp
import io.glnt.gpms.model.entity.ProductTicket
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.TicketType
import io.glnt.gpms.model.enums.VehicleType
import io.glnt.gpms.service.CorpService
import mu.KLogging
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

@Service
class ExcelUploadService {
    companion object : KLogging()

    @Value("\${file.filepath}")
    lateinit var filePath: String

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var corpService: CorpService

    @Throws(CustomException::class)
    @Transactional
    fun loadExcel(file: MultipartFile, type: String): CommonResult {
        try{
            //todo validate
            val workbook = WorkbookFactory.create(file.inputStream)

            if (type == "SEASONTICKET") {
                addProductTicket(workbook)
            }

        return CommonResult.data()

        } catch (e: CustomException) {
            logger.error{ "loadExcel failed file ${file.name}" }
            return CommonResult.error("loadExcel failed file ${file.name}")
        }
    }

    private fun addProductTicket(workbook: Workbook) {
        val sheet = workbook.getSheet("정기권") ?: throw CustomException("file not found", ResultCode.FAILED)
        var continueCount = 0
        for (row in sheet) {
            if (row == null) {
                continue
            }
            var cellString = ""
            continueCount++
            if (continueCount == 1) {
                continue
            }
            if (isRowEmpty(row)) {
                break
            }
            for (cell in row) {
                cell.cellType = CellType.STRING
                cellString += "$cell$"
            }
            val list = cellString.split('$')
            val corpSn = if (list[7].isNotEmpty()) {
                corpService.getStoreByCorpName(list[7])

            } else null

            if (DataCheckUtil.isValidCarNumber(list[0])) {
                val newData = ProductTicket(
                    sn = null,
                    vehicleNo = list[0],
                    ticketType = TicketType.from(list[1]),
                    name = list[2],
                    tel = list[3],
                    effectDate = DateUtil.beginTimeToLocalDateTime(list[4]),
                    expireDate = if (list[5].isNotEmpty()) DateUtil.lastTimeToLocalDateTime(list[5]) else DateUtil.stringToLocalDateTime("9999-12-31 23:59:59", "yyyy-MM-dd HH:mm:ss"),
                    vehiclekind = list[6],
                    corpSn = corpSn as Long?,
                    etc = list[8],
                    etc1 = list[9],
                    delYn = DelYn.N,
                    vehicleType = VehicleType.SMALL
                )
                productService.saveProductTicket(newData)
            }
        }
    }

    fun downloadTemplateOfProductTicket(data: List<ProductTicket>) : File {
        val fileFullPath: String = "$filePath/product"
        File(fileFullPath).apply {
            if (!exists()) {
                mkdirs()
            }
        }

        val workbook = XSSFWorkbook()
        val courseSheet = workbook.createSheet("정기권")
        val courseRow = courseSheet.createRow(0)
        val courseHeader = mutableListOf<String>("차량번호", "상품타입", "이름", "")

        addTableHeader(courseRow, courseHeader)

        val fileName = "정기권"+LocalDate.now()+".csv"
        val os = FileOutputStream("$fileFullPath/$fileName")
        workbook.write(os)
        val file = File("$fileFullPath/$fileName")
        return file
    }

    private fun isRowEmpty(row: Row): Boolean {
        var c = row.firstCellNum
        while (c < row.lastCellNum) {
            val cell = row.getCell(c.toInt())
            if (cell != null && cell.cellType != CellType.BLANK) {
                return false
            }
            c++
        }
        return true
    }

    private fun addTableHeader(row: XSSFRow, header: List<String>) {
        for (i in 0 until header.size) {
            val cell = row.createCell(i)
            cell.setCellValue(header[i])
        }
    }
}