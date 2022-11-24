package io.github.cidverse.cid.sdk.domain

data class VCSRelease(
    val version: String,
    val ref: VCSTag,
)
