package io.glnt.gpms.handler.file.service.excel

import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.exception.CustomException
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet

object ExcelConstants {
    // sheets
    const val nodeInfoSheet = "정기권"
    const val pipeInfoSheet = "capacities"
    const val demandsInfoSheet = "demands"

    // columns
    val nodeName = "이름"
    val nodeVehicleNo = "차량번호"
    val nodeStart = "시작일"
    val nodeEnd = "종료일"
    val nodeType = "타입"
    val nodeInfoHeaders = listOf(nodeName, nodeVehicleNo, nodeStart, nodeEnd, nodeType)
}

//fun Sheet.getColumnToIndexMap(): MutableMap<String, Int> {
//    val headerToIndexMap = mutableMapOf<String, Int>()
//
//    val headerRow = this.getRow(0)
//    headerRow.forEachIndexed { index, cell ->
//        if (ExcelConstants.nodeInfoHeaders.contains(cell.stringCellValue.trim())) {
//            headerToIndexMap[cell.stringCellValue.trim()] = index
//        }
//    }
//
//    Assert.isTrue(ExcelConstants.nodeInfoHeaders.size == headerToIndexMap.size, ErrorCode.INCORRECT_VALUE, "Некорректный формат листа ${ExcelConstants.nodeInfoSheet}")
//    return headerToIndexMap
//}
//
//fun String.getPosition(message: String): Double {
//    val result = this.substringBefore(' ').toDoubleOrNull()
//    if(result!=null)
//        return result
//    else
//        throw GtvRuntimeException(message, ErrorCode.INCORRECT_VALUE)
//
//}
//
//fun Cell?.safeStringValue() = when (this?.cellTypeEnum) {
//    CellType.ERROR -> ""
//    else -> this?.stringCellValue?.trim()
//}
//
//inline fun <reified T> T?.notNull(message: String = "Некорректный формат загруженной страницы"): T {
//    if (this != null) throw CustomException(message, ResultCode.FAILED)
//    return this!!
//}