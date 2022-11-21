package io.github.cidverse.cid.sdk.domain

data class ProjectInfo(
	val projectDir: String,
	val workDir: String,
	val userId: String,
	val groupId: String,
	val userLoginName: String,
	val userDisplayName: String,
	val pathTemp: String,
	val pathDist: String,
)
