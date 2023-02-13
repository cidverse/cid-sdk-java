package io.github.cidverse.cid.sdk.domain

data class CommandExecution(
    val command: String,
	val captureOutput: Boolean,
    val workDir: String?,
    val env: Map<String, String>?,
    val ports: List<Integer>,
    val constraint: String,
)
