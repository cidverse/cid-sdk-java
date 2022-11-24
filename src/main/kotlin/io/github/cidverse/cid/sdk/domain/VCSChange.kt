package io.github.cidverse.cid.sdk.domain

data class VCSChange(
    val type: String,
    val fileFrom: VCSFile,
    val fileTo: VCSFile,
    val patch: String,
)
