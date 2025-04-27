package com.example.thunderscope_frontend.data.utils

import com.example.thunderscope_frontend.data.models.CaseRecordResponse
import com.example.thunderscope_frontend.data.models.SlidesEntity
import com.example.thunderscope_frontend.data.models.SlidesItem

object SlidesMapper {
    fun toEntity(slidesItem: SlidesItem) = SlidesEntity(
        id = slidesItem.id ?: 0L,
        caseId = slidesItem.caseRecordResponse?.id?.toLong() ?: 0L,
        mainImage = slidesItem.mainImage,
        qrCode = slidesItem.qrCode,
        reportId = slidesItem.reportId,
        diagnosis = slidesItem.diagnosis,
        specimenType = slidesItem.specimenType,
        aiInsights = slidesItem.aiInsights,
        collectionSite = slidesItem.collectionSite,
        clinicalData = slidesItem.clinicalData,
        isActive = slidesItem.isActive,
        isCurrentlySelected = slidesItem.isCurrentlySelected
    )

    fun toDomain(slidesEntity: SlidesEntity) = SlidesItem(
        id = slidesEntity.id,
        caseRecordResponse = CaseRecordResponse(id = slidesEntity.caseId.toInt()),
        mainImage = slidesEntity.mainImage,
        qrCode = slidesEntity.qrCode,
        reportId = slidesEntity.reportId,
        diagnosis = slidesEntity.diagnosis,
        specimenType = slidesEntity.specimenType,
        aiInsights = slidesEntity.aiInsights,
        collectionSite = slidesEntity.collectionSite,
        clinicalData = slidesEntity.clinicalData,
        isActive = slidesEntity.isActive,
        isCurrentlySelected = slidesEntity.isCurrentlySelected
    )
}
