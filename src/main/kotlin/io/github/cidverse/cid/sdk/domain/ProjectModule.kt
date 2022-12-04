package io.github.cidverse.cid.sdk.domain

data class ProjectModule(
    val projectDir: String,
    val moduleDir: String,
    val discovery: List<ProjectModuleDiscovery>,
    val name: String,
    val slug: String,
    val buildSystem: String,
    val buildSystemSyntax: String,
    val language: Map<String, String>?,
    val dependencies: List<ProjectDependency>?,
    val submodules: List<ProjectModule>?,
    val files: List<String>,
)
