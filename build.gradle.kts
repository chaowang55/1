plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.4.0"
}

group = "uk.ac.ncl.csc8019"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot 基础
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    // ✅ 安全依赖（修复所有包找不到）
    implementation 'org.springframework.boot:spring-boot-starter-security'

    // ✅ JJWT 依赖（JWT 工具）
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'

    // 数据库
    runtimeOnly 'com.h2database:h2'
    runtimeOnly 'mysql:mysql-connector-java'

    // 工具
    implementation 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // 测试
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}

spotless {
    java {
        palantirJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    sql {
        target("src/main/resources/db/migration/*.sql")
        dbeaver()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.named("check") {
    dependsOn("spotlessCheck")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
