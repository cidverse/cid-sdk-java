// Plugins
plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("me.philippheuer.configuration") version "0.10.8"
}

// setup
projectConfiguration {
    language.set(me.philippheuer.projectcfg.domain.ProjectLanguage.KOTLIN)
    javaVersion.set(JavaVersion.VERSION_17)
    artifactGroupId.set("io.github.cidverse")
    artifactDescription.set("java sdk to create cid actions and workflows")

    pom = { pom ->
        pom.url.set("https://github.com/cidverse/cid-sdk-java")
        pom.issueManagement {
            system.set("GitHub")
            url.set("https://github.com/cidverse/cid-sdk-java/issues")
        }
        pom.inceptionYear.set("2022")
        pom.developers {
            developer {
                id.set("PhilippHeuer")
                name.set("Philipp Heuer")
                email.set("git@philippheuer.me")
                roles.addAll("maintainer")
            }
        }
        pom.licenses {
            license {
                name.set("MIT Licence")
                distribution.set("repo")
                url.set("https://github.com/cidverse/cid-sdk-java/blob/main/LICENSE")
            }
        }
        pom.scm {
            connection.set("scm:git:https://github.com/cidverse/cid-sdk-java.git")
            developerConnection.set("scm:git:git@https://github.com/cidverse/cid-sdk-java.git")
            url.set("https://github.com/cidverse/cid-sdk-java")
        }
    }
}

dependencies {
    // bom
    api(platform("com.fasterxml.jackson:jackson-bom:2.16.0"))

    // http client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.kohlschutter.junixsocket:junixsocket-core:2.8.3")

    // json
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // testing
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.0")
}
