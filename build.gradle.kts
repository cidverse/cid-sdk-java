// Plugins
plugins {
    id("java-library")
    id("me.philippheuer.configuration") version "0.10.5"
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
    // http client
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.kohlschutter.junixsocket:junixsocket-core:2.6.2")

    // json
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.1")

    // testing
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
}
