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
    implementation(project(":infra"))

    // API 레벨 의존성
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // 데이터베이스
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

    // JWT
    implementation("com.auth0:java-jwt:4.5.0")

    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.register<Copy>("copySecret") {
    from("${rootDir}/kapp-config/kapp-backend")
    include("application*.yml")
    include("*.p8")
    exclude("application-local.yml")
    into("./src/main/resources")
}

tasks.named("processResources") {
    dependsOn("copySecret")
}
