package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.service.CorpQueryService
import io.glnt.gpms.model.dto.CorpCriteria
import io.glnt.gpms.model.dto.CorpDTO
import io.glnt.gpms.service.CorpService
import io.glnt.gpms.service.ParkSiteInfoService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class CorpResource (
    private val corpService: CorpService,
    private val corpQueryService: CorpQueryService,
    private val parkSiteInfoService: ParkSiteInfoService
){
    companion object : KLogging()

    @RequestMapping(value = ["/corps"], method = [RequestMethod.GET])
    fun getStores(criteria: CorpCriteria): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(corpQueryService.findByCriteria(criteria)))
    }

    @RequestMapping(value = ["/corps"], method = [RequestMethod.POST])
    fun createStore(corpDTO: CorpDTO): ResponseEntity<CommonResult> {
        if (corpDTO.sn != null) {
            throw CustomException(
                "corp create sn exists",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(corpService.save(corpDTO, "update", parkSiteInfoService.getSiteId())))
    }

    @RequestMapping(value = ["/corps"], method = [RequestMethod.PUT])
    fun updateStore(corpDTO: CorpDTO): ResponseEntity<CommonResult> {
        if (corpDTO.sn == null) {
            throw CustomException(
                "corp update not found sn",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(corpService.save(corpDTO, "create", parkSiteInfoService.getSiteId())))
    }

    @RequestMapping(value = ["/corps/{sn}/{inSn}/able/ticket"], method = [RequestMethod.GET])
    fun getStoreAbleTickets(@PathVariable sn: Long, @PathVariable inSn: String): ResponseEntity<CommonResult> {
        logger.debug { "store fetch able ticket" }
        if (inSn == "ALL")
            return CommonResult.returnResult(CommonResult.data(corpService.getStoreTicketsByStoreSn(sn)))
        return CommonResult.returnResult(CommonResult.data())
    }

}