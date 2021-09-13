package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.model.dto.BarcodeClassDTO
import io.glnt.gpms.model.dto.BarcodeDTO
import io.glnt.gpms.model.dto.request.resParkInList
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.service.BarcodeClassService
import io.glnt.gpms.service.BarcodeService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class BarcodeResource (
    private val barcodeService: BarcodeService,
    private val barcodeClassService: BarcodeClassService
){
    companion object : KLogging()

    @RequestMapping(value = ["/barcode"], method = [RequestMethod.GET])
    fun findAll(): ResponseEntity<CommonResult> {
        logger.debug { "barcode fetch" }
        var result = barcodeService.findAll().filter { it.delYn == DelYn.N }
        if (result.isNotEmpty()) result = listOf(result[0])
        return CommonResult.returnResult(CommonResult.data(result))
    }

    @RequestMapping(value = ["/barcode"], method = [RequestMethod.POST])
    fun createBarcode(@Valid @RequestBody barcodeDTO: BarcodeDTO): ResponseEntity<CommonResult> {
        logger.debug { "barcode save $barcodeDTO" }
        if (barcodeDTO.sn != null) {
            throw CustomException(
                "Barcode create sn exists",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(barcodeService.save(barcodeDTO)))
    }

    @RequestMapping(value = ["/barcode"], method = [RequestMethod.PUT])
    fun updateBarcode(@Valid @RequestBody barcodeDTO: BarcodeDTO): ResponseEntity<CommonResult> {
        logger.debug { "barcode save $barcodeDTO" }
        if (barcodeDTO.sn == null) {
            throw CustomException(
                "Barcode not found sn",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(barcodeService.save(barcodeDTO)))
    }

    @RequestMapping(value = ["/barcode/classes"], method = [RequestMethod.GET])
    fun findAllBarcodeClasses(): ResponseEntity<CommonResult> {
        logger.debug { "barcode-class fetch all" }
        return CommonResult.returnResult(CommonResult.data(barcodeClassService.findAll()))
    }

    @RequestMapping(value = ["/barcode/classes"], method = [RequestMethod.POST])
    fun createBarcodeClasses(@Valid @RequestBody barcodeClassDTO: BarcodeClassDTO): ResponseEntity<CommonResult> {
        logger.debug { "barcode-class save $barcodeClassDTO" }
        if (barcodeClassDTO.sn != null) {
            throw CustomException(
                "Barcode class create sn exists",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(barcodeClassService.save(barcodeClassDTO)))
    }

    @RequestMapping(value = ["/barcode/classes"], method = [RequestMethod.PUT])
    fun updateBarcodeClasses(@Valid @RequestBody barcodeClassDTO: BarcodeClassDTO): ResponseEntity<CommonResult> {
        logger.debug { "barcode-class save $barcodeClassDTO" }
        if (barcodeClassDTO.sn == null) {
            throw CustomException(
                "Barcode class not found sn",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(barcodeClassService.save(barcodeClassDTO)))
    }



}