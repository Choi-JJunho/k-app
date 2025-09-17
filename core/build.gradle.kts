plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    // Core module은 외부 의존성 최소화 (POJO/data class만)
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // 테스트
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}
