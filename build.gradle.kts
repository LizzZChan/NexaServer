import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin ("jvm") version "1.9.0"
  kotlin("plugin.serialization") version "2.0.0"
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.hcifuture"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
  maven("https://www.ijiaodui.com:31084/repository/hci-maven/")
}

val vertxVersion = "4.5.7"
val junitJupiterVersion = "5.9.1"

val mainVerticleName = "com.hcifuture.nuixserver.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-web-client")
  implementation("io.vertx:vertx-web-validation")
  implementation("io.vertx:vertx-web-graphql")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-web-openapi")
  implementation("io.vertx:vertx-web-openapi-router")
  implementation("io.vertx:vertx-web-api-contract")
  implementation("io.vertx:vertx-lang-kotlin")
  implementation("io.vertx:vertx-lang-kotlin-coroutines")
  runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.108.Final:osx-aarch_64")
  implementation(kotlin("stdlib-jdk8"))
  implementation("com.aallam.openai:openai-client:3.8.1")
  implementation("org.kodein.di:kodein-di:7.21.1")
  implementation("com.sksamuel.hoplite:hoplite-core:2.7.5")
  implementation("com.sksamuel.hoplite:hoplite-yaml:2.7.5")
  implementation("io.ktor:ktor-client-android:2.3.9")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
  implementation("it.skrape:skrapeit:1.3.0-alpha.1")
  implementation("it.skrape:skrapeit-http-fetcher:1.3.0-alpha.1")
  implementation("it.skrape:skrapeit-browser-fetcher:1.3.0-alpha.1")
  implementation("com.google.code.gson:gson:2.10.1")
  implementation("org.slf4j:slf4j-api:2.0.12")
  implementation("ch.qos.logback:logback-classic:1.4.14")
  implementation("ch.qos.logback:logback-core:1.4.14")
  implementation("app.futureinteraction:vertx-sse-maven:0.0.1")
  implementation("org.json:json:20210307")
  implementation("com.google.cloud:google-cloud-vision:2.0.0")
  implementation("net.sourceforge.tess4j:tess4j:5.4.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
  implementation("io.swagger.parser.v3:swagger-parser:2.1.22")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "17"

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}


tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}
