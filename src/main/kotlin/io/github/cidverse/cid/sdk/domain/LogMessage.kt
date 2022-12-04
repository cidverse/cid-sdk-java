package io.github.cidverse.cid.sdk.domain

data class LogMessage(
	val level: String,
	val message: String,
	val context: Map<String, Any>,
)
