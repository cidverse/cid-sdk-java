package io.github.cidverse.cid.sdk.domain

import java.time.Instant

data class VCSCommit(
    val hashShort: String,
    val hash: String,
    val message: String,
    val description: String,
    val author: VCSAuthor,
    val committer: VCSAuthor,
    val tags: List<VCSTag>?,
    val authoredAt: Instant,
    val committedAt: Instant,
    val changes: List<VCSChange>?,
)
