package io.glnt.gpms.handler.vehicle.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.utils.Base64Util
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.handler.tmap.model.reqTmapInVehicle
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.handler.vehicle.model.reqAddParkIn
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.repository.ParkFacilityRepository
import io.glnt.gpms.model.repository.ParkInRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.lang.RuntimeException
import java.time.LocalDate

@Service
class VehicleService {
    companion object : KLogging()

    @Value("\${image.filepath}")
    lateinit var imagePath: String

    @Autowired
    private lateinit var parkinglotService: ParkinglotService

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var tmapSendService: TmapSendService

    @Autowired
    private lateinit var parkFacilityRepository: ParkFacilityRepository

    @Autowired
    private lateinit var parkInRepository: ParkInRepository

    fun parkIn(request: reqAddParkIn) : CommonResult = with(request){
        logger.debug("parkIn service {}", request)
        try {
            //todo gate up(option check)

            // image 파일 저장
            if (base64Str != null) {
                // folder check
                fileFullPath = "$imagePath/"+ LocalDate.now()
                File(fileFullPath!!).apply {
                    if (!exists()) {
                        mkdirs()
                    }
                }
                fileName = parkinglotService.parkSiteId()+"_"+parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid+"_"+ DateUtil.nowTimeDetail.substring(9,12)+vehicleNo+".jpg"
                fileUploadId = DateUtil.stringToNowDateTimeMS()+"_F"
                val imageByteArray = Base64Util.decodeAsBytes(base64Str!!)
                if (imageByteArray != null) {
                    File("$fileFullPath/$fileName").writeBytes(imageByteArray)
                }
            }

            //차량번호 패턴 체크
            if (DataCheckUtil.isValidCarNumber(vehicleNo)) {
                //todo 출차 정보 확인 후 update

                parkingtype = "일반차량"
                //todo 정기권 차량 여부 확인
                productService.getValidProductByVehicleNo(vehicleNo)?.let {
                    parkingtype = "정기차량"
                    validDate = it.validDate
                }
                recognitionResult = "RECOGNITION"
            } else {
                parkingtype = "미인식차량"
                recognitionResult = "NOTRECOGNITION"
            }
            requestId = DataCheckUtil.generateRequestId(parkinglotService.parkSiteId())

            //todo 입차 정보 DB insert
            val newData = ParkIn(
                sn = null,
//                gateId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.gateInfo.gateId,
                gateId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.gateId,
                parkcartype = parkingtype,
                userSn = 0,
                vehicleNo = vehicleNo,
                image = "$fileFullPath/$fileName",
                flag = 1,
                validate = validDate,
                resultcode = resultcode.toInt(),
                requestid = requestId,
                fileuploadid = fileUploadId,
                hour = DateUtil.nowTimeDetail.substring(0,2),
                min = DateUtil.nowTimeDetail.substring(3,5),
                inDate = inDate
            )
            parkInRepository.save(newData)

            //todo tmap 전송
            val data = reqTmapInVehicle(
                gateId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid!!,
                inVehicleType = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.lprType.toString().toLowerCase(),
                vehicleNumber = vehicleNo,
                recognitionType = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.category,
                recognitorResult = recognitionResult!!,
                fileUploadId = fileUploadId!!
            )
            tmapSendService.sendInVehicle(data, requestId!!, fileName)

            CommonResult.created()

        } catch (e: RuntimeException) {
            ParkinglotService.logger.error("addParkinglotFeature error {} ", e.message)
            return CommonResult.error("parkinglot feature db add failed ")
        }


    }
}