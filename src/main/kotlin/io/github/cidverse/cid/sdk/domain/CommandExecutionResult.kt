package io.github.cidverse.cid.sdk.domain

data class CommandExecutionResult(
    val dir: String,
    val command: String,
    val code: Int,
    val stdout: String,
    val stderr: String,
    val error: String,
)
