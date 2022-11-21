package io.github.cidverse.cid.sdk.domain

data class CIDError(
    val status: Int,
    val title: String,
    val details: String
) : RuntimeException() {
    override fun toString(): String {
        return "$status: $title [$details]"
    }
}
