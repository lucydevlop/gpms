package io.glnt.gpms.handler.tmap.controller

import io.glnt.gpms.handler.tmap.model.reqApiTmapCommon
import io.glnt.gpms.handler.tmap.service.TmapCommandService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/v1/manage/devices")
class TmapController {
    @Autowired
    private lateinit var tmapCommandService: TmapCommandService
    //todo type == 'printerCheck' 인 경우 contents 맵핑

    //설비 목록 요청
//    @
    //설비 제어 요청
    @RequestMapping("/command")
    fun getTmapCommand(@RequestBody request: reqApiTmapCommon) {
        when(request.type) {
            "facilitiesCommand" -> tmapCommandService.commandFacilities(request)
            else -> {}
        }
    }
}