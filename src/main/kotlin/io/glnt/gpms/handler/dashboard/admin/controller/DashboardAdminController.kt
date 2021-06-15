package io.glnt.gpms.handler.dashboard.admin.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.dashboard.admin.model.*
import io.glnt.gpms.handler.dashboard.admin.service.DashboardAdminService
import io.glnt.gpms.handler.inout.model.reqSearchParkin
import io.glnt.gpms.model.dto.request.*
import io.glnt.gpms.model.entity.TicketClass
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}/dashboard/admin"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class DashboardAdminController {
    @Autowired
    private lateinit var dashboardAdminService: DashboardAdminService

    @RequestMapping(value = ["/list"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun searchAdminUsers(@RequestBody request: reqSearchItem) : ResponseEntity<CommonResult> {
        logger.trace("searchAdminUsers $request")
        return CommonResult.returnResult(dashboardAdminService.searchAdminUsers(request))
    }

    @RequestMapping(value = ["/delete/{sn}"], method = [RequestMethod.DELETE])
    @Throws(CustomException::class)
    fun deleteAdminUser(@PathVariable sn: Long) : ResponseEntity<CommonResult> {
        logger.trace("deleteAdminUser $sn")
        return CommonResult.returnResult(dashboardAdminService.deleteAdminUser(sn))
    }

    @RequestMapping(value = ["/edit"], method = [RequestMethod.PUT])
    @Throws(CustomException::class)
    fun editAdminUser(@RequestBody request: reqUserInfo) : ResponseEntity<CommonResult> {
        logger.trace("editAdminUser $request")
        return CommonResult.returnResult(dashboardAdminService.editAdminUser(request))
    }

    @RequestMapping(value=["/main/gate"], method = [RequestMethod.GET])
    fun getMainGates() : ResponseEntity<CommonResult> {
        logger.trace { "admin dashboard Gates info" }
        return CommonResult.returnResult(dashboardAdminService.getMainGates())
    }

    @RequestMapping(value = ["/inout/list"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun getParkInLists(@RequestBody request: reqSearchParkin) : ResponseEntity<CommonResult> {
        logger.trace { "getParkInLists $request" }
        return CommonResult.returnResult(dashboardAdminService.getParkInLists(request))
    }

    @RequestMapping(value = ["/inout/create"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun createParkInout(@RequestBody request: resParkInList) : ResponseEntity<CommonResult> {
        logger.trace { "getParkInLists $request" }
        return CommonResult.returnResult(dashboardAdminService.createInout(request))
    }


    @RequestMapping(value = ["/inout/edit"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun editParkInout(@RequestBody request: resParkInList) : ResponseEntity<CommonResult> {
        logger.trace { "getParkInLists $request" }
        return CommonResult.returnResult(dashboardAdminService.editParkInout(request))
    }

    @RequestMapping(value = ["/inout/delete/{sn}"], method = [RequestMethod.DELETE])
    @Throws(CustomException::class)
    fun deleteParkInout(@PathVariable sn: Long) : ResponseEntity<CommonResult> {
        logger.trace { "deleteParkInout $sn" }
        return CommonResult.returnResult(dashboardAdminService.deleteParkInout(sn))
    }

    @RequestMapping(value = ["/gate/list"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun getGates(): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(dashboardAdminService.getGates())
    }

    @RequestMapping(value = ["/gate/{action}/{gateId}"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun gateAction(@PathVariable action: String, @PathVariable gateId: String) : ResponseEntity<CommonResult> {
        logger.trace { "gateAction $gateId $action" }
        return CommonResult.returnResult(dashboardAdminService.gateAction(action, gateId))
    }

    @RequestMapping(value = ["/gate/reset/{gateId}/{category}"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun gateResetAction(@PathVariable gateId: String, @PathVariable category: String) : ResponseEntity<CommonResult> {
        logger.trace { "gateResetAction $gateId $category" }
        return CommonResult.returnResult(dashboardAdminService.gateResetAction(gateId, category))
    }

    @RequestMapping(value = ["/gate/change/use"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun changeGateUse(@RequestBody request: reqChangeUseGate): ResponseEntity<CommonResult> {
        logger.trace("parkinglot gate use change : $request")
        return CommonResult.returnResult(dashboardAdminService.changeGateUse(request))
    }

    @RequestMapping(value = ["/facility/create"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun createFacility(@RequestBody request: reqCreateFacility) : ResponseEntity<CommonResult> {
        logger.trace { "createFacility $request " }
        return CommonResult.returnResult(dashboardAdminService.createFacility(request))
    }

    @RequestMapping(value = ["/facility/change/use"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun changeFacilityUse(@RequestBody request: reqChangeUseFacility): ResponseEntity<CommonResult> {
        logger.trace("parkinglot facility use change : $request")
        return CommonResult.returnResult(dashboardAdminService.changeFacilityUse(request))
    }

    @RequestMapping(value = ["/message/create"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun createMessage(@RequestBody request: ReqCreateMessage) : ResponseEntity<CommonResult> {
        logger.trace { "$request" }
        return CommonResult.returnResult(dashboardAdminService.createMessage(request))
    }

    @RequestMapping(value = ["/message/update"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun updateMessage(@RequestBody request: ArrayList<ReqCreateMessage>) : ResponseEntity<CommonResult>{
        logger.trace { "$request" }
        return CommonResult.returnResult(dashboardAdminService.updateMessage(request))
    }

    @RequestMapping(value = ["/message/info"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun getDisplayInfo() : ResponseEntity<CommonResult>{
        logger.trace { "getDisplayInfo" }
        return CommonResult.returnResult(dashboardAdminService.getDisplayInfo())
    }

    @RequestMapping(value = ["/message/info"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun updateDisplayInfo(@RequestBody request: reqDisplayInfo) : ResponseEntity<CommonResult>{
        logger.trace { "updateDisplayInfo $request" }
        return CommonResult.returnResult(dashboardAdminService.updateDisplayInfo(request))
    }

    @RequestMapping(value = ["/product/ticket/list"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun searchProductTicket(@RequestBody request: reqSearchProductTicket) : ResponseEntity<CommonResult> {
        logger.trace("parkinglot product list = $request")
        return CommonResult.returnResult(dashboardAdminService.searchProductTicket(request))
    }

    @RequestMapping(value = ["/product/ticket/download"], method = [RequestMethod.GET], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    @Throws(CustomException::class)
    fun downloadTemplateOfProductTicket() : ResponseEntity<*> {
        logger.trace("parkinglot product list download")
        val result = dashboardAdminService.createTemplateOfProductTicket()
        val headers = HttpHeaders()
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate")
        headers.add("content-disposition", String.format("attachment; filename=product.csv"))
        headers.add("filename", String.format("attachment; filename=product.csv"))
        headers.add("Pragma", "no-cache")
        headers.add("Expires", "0")
        when (result.code) {
            ResultCode.SUCCESS.getCode() -> {

                val file: File = result.data as File
                val filePath = file.absolutePath
                val excelFile = FileSystemResource(filePath)
                return ResponseEntity
                    .ok()
                    .header("Content-type", "application/octet-stream")
                    .header("Content-disposition", "attachment; filename=product.csv")
                    .contentLength(excelFile.contentLength())
//                        .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(excelFile)
            }
        }
        return ResponseEntity
            .badRequest()
            .headers(headers)
            .contentLength(0)
            .contentType(MediaType.parseMediaType("application/octet-stream"))
            .body(null)

//        return CommonResult.returnFile(dashboardAdminService.createTemplateOfProductTicket(), "product")
    }

    @RequestMapping(value = ["/product/ticket/add"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun createProductTicket(@RequestBody request: reqCreateProductTicket) : ResponseEntity<CommonResult> {
        logger.trace("parkinglot product create : $request")
        return CommonResult.returnResult(dashboardAdminService.createProductTicket(request))
    }

    @RequestMapping(value = ["/product/ticket/adds"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun createProductTicket(@RequestBody request: ArrayList<reqCreateProductTicket>) : ResponseEntity<CommonResult> {
        logger.trace("parkinglot product create : $request")
        return CommonResult.returnResult(dashboardAdminService.createProductTickets(request))
    }

    @RequestMapping(value = ["/product/add/file"], method = [RequestMethod.POST], consumes = ["multipart/form-data"])
    @Throws(CustomException::class)
    fun createProductTicketByFiles(@RequestParam("file") file: MultipartFile) : ResponseEntity<CommonResult> {
        logger.trace("parkinglot product create file : ${file.name}")
        return CommonResult.returnResult(dashboardAdminService.createProductTicketByFiles(file))
    }

    @RequestMapping(value = ["/corp/list"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun searchCorpList(@RequestBody request: reqSearchCorp) : ResponseEntity<CommonResult> {
        logger.trace("corp search : $request")
        return CommonResult.returnResult(dashboardAdminService.searchCorpList(request))
    }

    @RequestMapping(value = ["/corp/ticket/create"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun createCorpTicket(@RequestBody request: reqCreateCorpTicket) : ResponseEntity<CommonResult> {
        logger.trace("corp create ticket : $request")
        return CommonResult.returnResult(dashboardAdminService.createCorpTicket(request))
    }

    @RequestMapping(value = ["/corp/ticket/delete/{sn}"], method = [RequestMethod.DELETE])
    @Throws(CustomException::class)
    fun deleteCorpTicket(@PathVariable sn: Long) : ResponseEntity<CommonResult> {
        logger.trace("corp delete ticket : $sn")
        return CommonResult.returnResult(dashboardAdminService.deleteCorpTicket(sn))
    }

    @RequestMapping(value = ["/fare/basic"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun getFareBasic(): ResponseEntity<CommonResult> {
        logger.trace("getFareInfo")
        return CommonResult.returnResult(dashboardAdminService.getFareBasic())
    }

    @RequestMapping(value = ["/fare/info"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun getFareInfo(): ResponseEntity<CommonResult> {
        logger.trace("getFareInfo")
        return CommonResult.returnResult(dashboardAdminService.getFareInfo())
    }

    @RequestMapping(value = ["/fare/policy"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun getFarePolicy(): ResponseEntity<CommonResult> {
        logger.trace("getFarePolicy")
        return CommonResult.returnResult(dashboardAdminService.getFarePolicy())
    }

    @RequestMapping(value = ["/fare/basic"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun updateFareBasic(@RequestBody request: reqFareBasic) : ResponseEntity<CommonResult> {
        logger.trace("updateFareBasic $request")
        return CommonResult.returnResult(dashboardAdminService.updateFareBasic(request))
    }

    @RequestMapping(value = ["/fare/info"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun createFareInfo(@RequestBody request: reqFareInfo) : ResponseEntity<CommonResult> {
        logger.trace("createFareInfo $request")
        return CommonResult.returnResult(dashboardAdminService.createFareInfo(request))
    }

    @RequestMapping(value = ["/fare/policy"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun createFarePolicy(@RequestBody request: reqFarePolicy) : ResponseEntity<CommonResult> {
        logger.trace("createFarePolicy $request")
        return CommonResult.returnResult(dashboardAdminService.createFarePolicy(request))
    }

    @RequestMapping(value = ["/fare/policy/{sn}"], method = [RequestMethod.DELETE])
    @Throws(CustomException::class)
    fun deleteFarePolicy(@PathVariable sn: Long) : ResponseEntity<CommonResult> {
        logger.trace("deleteFarePolicy $sn")
        return CommonResult.returnResult(dashboardAdminService.deleteFarePolicy(sn))
    }

    @RequestMapping(value = ["/discount/class"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun createDiscountTicket(@RequestBody request: reqDiscountTicket) : ResponseEntity<CommonResult> {
        logger.trace("createDiscountTicket $request")
        return CommonResult.returnResult(dashboardAdminService.createDiscountTicket(request))
    }

    @RequestMapping(value = ["/discount/class/{sn}"], method = [RequestMethod.DELETE])
    @Throws(CustomException::class)
    fun deleteDiscountTicket(@PathVariable sn: Long) : ResponseEntity<CommonResult> {
        logger.trace("deleteDiscountTicket $sn")
        return CommonResult.returnResult(dashboardAdminService.deleteDiscountTicket(sn))
    }

    @RequestMapping(value = ["/external/async/parkinglot"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun externalAsyncParkinglot(): ResponseEntity<CommonResult> {
        logger.trace("externalAsyncParkinglot")
        return CommonResult.returnResult(dashboardAdminService.externalAsyncParkinglot())
    }

    @RequestMapping(value = ["/ticket/list"])
    @Throws(CustomException::class)
    fun getTicketList(): ResponseEntity<CommonResult> {
        logger.trace("getTicketList")
        return CommonResult.returnResult(dashboardAdminService.getTicketList())
    }

    @RequestMapping(value = ["/ticket"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun createTicketClass(@RequestBody request: TicketClass): ResponseEntity<CommonResult> {
        logger.trace("createTicketClass")
        return CommonResult.returnResult(dashboardAdminService.createTicketClass(request))
    }

    companion object : KLogging()
}
