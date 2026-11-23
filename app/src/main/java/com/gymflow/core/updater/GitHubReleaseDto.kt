package com.gymflow.core.updater

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubReleaseDto(
    val id: Long = 0L,
    @SerialName("tag_name") val tagName: String = "",
    val name: String? = null,
    val body: String? = null,
    val prerelease: Boolean = false,
    val draft: Boolean = false,
    @SerialName("html_url") val htmlUrl: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    val assets: List<GitHubAssetDto> = emptyList()
)

@Serializable
data class GitHubAssetDto(
    val id: Long = 0L,
    val name: String = "",
    @SerialName("browser_download_url") val downloadUrl: String = "",
    val size: Long = 0L,
    @SerialName("content_type") val contentType: String? = null,
    @SerialName("download_count") val downloadCount: Int = 0
)

@Serializable
data class AppVersionManifestDto(
    val version: String = "",
    val versionCode: Long = 0L,
    val tagName: String = "",
    val releaseTitle: String = "",
    val changelog: String = "",
    val downloadUrl: String = "",
    val directCdnUrl: String? = null,
    val fileName: String = "",
    val fileSizeBytes: Long = 0L,
    val sha256: String? = null,
    val releasePageUrl: String = "",
    val publishedAt: String? = null
)

