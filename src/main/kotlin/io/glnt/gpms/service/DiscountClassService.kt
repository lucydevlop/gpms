package io.glnt.gpms.service

import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.dto.entity.DiscountClassDTO
import io.glnt.gpms.model.entity.DiscountClass
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.model.mapper.DiscountClassMapper
import io.glnt.gpms.model.repository.DiscountClassRepository
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct

@Service
class DiscountClassService (
    private val discountClassRepository: DiscountClassRepository,
    private val discountClassMapper: DiscountClassMapper
){
    companion object : KLogging()

    @PostConstruct
    fun initalizeData() {
        val discountClasses = findAll()
        if (discountClasses.none { DiscountClassDTO -> DiscountClassDTO.discountNm == "전액" }) {
            discountClassRepository.save(
                DiscountClass(sn = null, discountType = DiscountType.DISCOUNT, discountNm = "전액", discountApplyType = DiscountApplyType.PERCENT, discountApplyRate = DiscountApplyRateType.VARIABLE,
                              timeTarget = TimeTarget.NOW, dayRange = DiscountRangeType.ALL, unit = 100, disUse = SaleType.FREE,
                              effectDate = DateUtil.stringToLocalDateTime("2021-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss"), expireDate = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59", "yyyy-MM-dd HH:mm:ss"),
                              rcsUse = true, orderNo = 1, delYn = YN.N)
            )
        }
        if (discountClasses.none { DiscountClassDTO -> DiscountClassDTO.discountNm == "500원" }) {
            discountClassRepository.save(
                DiscountClass(sn = null, discountType = DiscountType.DISCOUNT, discountNm = "500원", discountApplyType = DiscountApplyType.WON, discountApplyRate = DiscountApplyRateType.VARIABLE,
                    timeTarget = TimeTarget.NOW, dayRange = DiscountRangeType.ALL, unit = 500, disUse = SaleType.FREE,
                    effectDate = DateUtil.stringToLocalDateTime("2021-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss"), expireDate = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59", "yyyy-MM-dd HH:mm:ss"),
                    rcsUse = true, orderNo = 2, delYn = YN.N)
            )
        }
        if (discountClasses.none { DiscountClassDTO -> DiscountClassDTO.discountNm == "1000원" }) {
            discountClassRepository.save(
                DiscountClass(sn = null, discountType = DiscountType.DISCOUNT, discountNm = "1000원", discountApplyType = DiscountApplyType.WON, discountApplyRate = DiscountApplyRateType.VARIABLE,
                    timeTarget = TimeTarget.NOW, dayRange = DiscountRangeType.ALL, unit = 1000, disUse = SaleType.FREE,
                    effectDate = DateUtil.stringToLocalDateTime("2021-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss"), expireDate = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59", "yyyy-MM-dd HH:mm:ss"),
                    rcsUse = true, orderNo = 3, delYn = YN.N)
            )
        }
        if (discountClasses.none { DiscountClassDTO -> DiscountClassDTO.discountNm == "5000원" }) {
            discountClassRepository.save(
                DiscountClass(sn = null, discountType = DiscountType.DISCOUNT, discountNm = "5000원", discountApplyType = DiscountApplyType.WON, discountApplyRate = DiscountApplyRateType.VARIABLE,
                    timeTarget = TimeTarget.NOW, dayRange = DiscountRangeType.ALL, unit = 5000, disUse = SaleType.FREE,
                    effectDate = DateUtil.stringToLocalDateTime("2021-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss"), expireDate = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59", "yyyy-MM-dd HH:mm:ss"),
                    rcsUse = true, orderNo = 4, delYn = YN.N)
            )
        }
        if (discountClasses.none { DiscountClassDTO -> DiscountClassDTO.discountNm == "10000원" }) {
            discountClassRepository.save(
                DiscountClass(sn = null, discountType = DiscountType.DISCOUNT, discountNm = "10000원", discountApplyType = DiscountApplyType.WON, discountApplyRate = DiscountApplyRateType.VARIABLE,
                    timeTarget = TimeTarget.NOW, dayRange = DiscountRangeType.ALL, unit = 10000, disUse = SaleType.FREE,
                    effectDate = DateUtil.stringToLocalDateTime("2021-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss"), expireDate = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59", "yyyy-MM-dd HH:mm:ss"),
                    rcsUse = true, orderNo = 5, delYn = YN.N)
            )
        }
    }

    @Transactional(readOnly = true)
    fun findAll(): List<DiscountClassDTO> {
        return discountClassRepository.findAll().map(discountClassMapper::toDto)
    }

    fun findBySn(sn: Long) : DiscountClassDTO {
        return discountClassMapper.toDto(discountClassRepository.findBySn(sn))
    }

    fun save(discountClassDTO: DiscountClassDTO): DiscountClassDTO {
        var discountClass = discountClassMapper.toEntity(discountClassDTO)
        discountClass = discountClassRepository.save(discountClass!!)
        return discountClassMapper.toDto(discountClass)
    }
}