package io.github.cidverse.cid.sdk

import io.github.cidverse.cid.sdk.domain.ProjectModule
import io.github.cidverse.cid.sdk.domain.ProjectModuleDiscovery
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class SDKMockExample {

    /**
     * example to show how the sdk can be mocked in test cases
     */
    @Test
    fun mockSDK() {
        val sdk = mock<CIDSDK> {
            on { module() } doReturn ProjectModule(
                projectDir = "/my-project",
                moduleDir = "",
                discovery = listOf(ProjectModuleDiscovery("/my-project")),
                name = "",
                slug = "",
                buildSystem = "",
                buildSystemSyntax = "",
                language = null,
                dependencies = null,
                submodules = null,
                files = listOf(),
            )
        }
        sdk.module()
    }
}
