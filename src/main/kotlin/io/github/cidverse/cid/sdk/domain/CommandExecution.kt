package io.github.cidverse.cid.sdk.domain

data class CommandExecution(
    val workDir: String?,
    val command: String,
	val captureOutput: Boolean,
)
