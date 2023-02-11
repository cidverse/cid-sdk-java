package io.github.cidverse.cid.sdk.domain

data class ArtifactFile(
    val buildId: String,
    val jobId: String,
    val id: String,
    val module: String,
    val type: String,
    val name: String,
    val format: String,
    val formatVersion: String,
)
