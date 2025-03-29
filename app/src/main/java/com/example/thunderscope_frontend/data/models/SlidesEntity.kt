package com.example.thunderscope_frontend.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Entity(tableName = "slides")
@Parcelize
data class SlidesEntity(
    @PrimaryKey val id: Long,
    val caseId: Long,
    val mainImage: String? = null,
    val qrCode: String? = null,
    val reportId: String? = null,
    val diagnosis: String? = null,
    val specimenType: String? = null,
    val aiInsights: String? = null,
    val collectionSite: String? = null,
    val clinicalData: String? = null,
    var isActive: Boolean = false,
    var isCurrentlySelected: Boolean = false
) : Parcelable