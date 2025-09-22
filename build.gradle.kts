plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.assesment"
version = "0.0.1-SNAPSHOT"
description = "Customer service app (Kotlin, Spring Boot, HTMX, Postgres)"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

kotlin {
    jvmToolchain(25)
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    // Kotlin 2.0.x can't emit class files for Java 25 yet; target 22 to match Java (see JavaCompile below)
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_22)
    }
}

// Use JDK 25 toolchain to compile, but emit Java 22 bytecode for compatibility with Kotlin target
// This sets --release=22 so compileJava doesn't produce class files at 25 while Kotlin is at 22
tasks.withType<org.gradle.api.tasks.compile.JavaCompile>().configureEach {
    options.release.set(22)
}

repositories {
    mavenCentral()
}

// Ensure devtools is available on the runtime classpath during development
configurations {
    developmentOnly
    runtimeClasspath {
        extendsFrom(configurations.developmentOnly.get())
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("org.postgresql:postgresql")

    // JSON (for scheduled import)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Devtools for hot reload (active only in development)
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Testcontainers for Postgres integration tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter:1.20.3")
    testImplementation("org.testcontainers:postgresql:1.20.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
