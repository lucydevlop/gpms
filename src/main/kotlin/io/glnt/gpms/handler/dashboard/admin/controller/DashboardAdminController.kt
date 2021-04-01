package io.glnt.gpms.handler.dashboard.admin.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.dashboard.admin.model.*
import io.glnt.gpms.handler.dashboard.admin.service.DashboardAdminService
import io.glnt.gpms.handler.inout.model.reqSearchParkin
import io.glnt.gpms.handler.inout.model.resParkInList
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

    companion object : KLogging()
}