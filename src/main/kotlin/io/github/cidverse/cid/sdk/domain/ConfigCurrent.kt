package io.github.cidverse.cid.sdk.domain

data class ConfigCurrent(
    val debug: Boolean,
    val log: Map<String, String>,
	val projectDir: String,
	val artifactDir: String,
	val tempDir: String,
	val hostName: String,
	val hostUserId: String,
	val hostUserName: String,
	val hostGroupId: String,
    val config: Any,
)
