plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    // Core와 Infra 모듈 의존
    implementation(project(":core"))
    runtimeOnly(project(":infra"))

    // API 레벨 의존성
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // 데이터베이스
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

    // JWT
    implementation("com.auth0:java-jwt:4.5.0")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")

    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val externalConfigDir = rootProject.layout.projectDirectory.dir("kapp-config/kapp-backend")
val generatedConfigDir = layout.buildDirectory.dir("generated-resources/config")

val copyExternalConfig by tasks.registering(Copy::class) {
    onlyIf { externalConfigDir.asFile.exists() }
    from(externalConfigDir)
    include("application*.yml")
    include("*.p8")
    into(generatedConfigDir)
}

tasks.named<org.gradle.language.jvm.tasks.ProcessResources>("processResources") {
    dependsOn(copyExternalConfig)
    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
    from(generatedConfigDir)
}
