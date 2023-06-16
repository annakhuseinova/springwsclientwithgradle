plugins {
    java
    id("org.springframework.boot") version "2.7.12"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
}

group = "com.annakhuseinova"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

val jaxb by configurations.creating

repositories {
    mavenCentral()
}

dependencies {
    jaxb("com.sun.xml.bind:jaxb-xjc:2.1.7")
    implementation("org.springframework.boot:spring-boot-starter-web-services")
    implementation("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register("genJaxb") {
    ext["sourcesDir"] = "${buildDir}/generated-sources/jaxb"
    ext["classesDir"] = "${buildDir}/classes/jaxb"
    ext["schema"] = "src/main/schema"

    ext["classesDir"]?.let { outputs.dir(it) }

    doLast {
        ant.withGroovyBuilder {
            "taskdef"(
                "name" to "xjc", "classname" to "com.sun.tools.xjc.XJCTask",
                "classpath" to jaxb.asPath
            )
            ext["sourcesDir"]?.let { mkdir(it) }
            ext["classesDir"]?.let { mkdir(it) }

            "xjc"(
                "destdir" to ext["sourcesDir"],
                "package" to "com.annakhuseinova.springwsclientwithgradle.gen",
            ) {
                "arg"("value" to "-wsdl")
                "schema"("dir" to ext["schema"], "includes" to "**/*.xsd")
                "produces"("dir" to ext["sourcesDir"], "includes" to "**/*.java")
            }

            "javac"(
                "destdir" to ext["classesDir"], "source" to 1.8, "target" to 1.8, "debug" to true,
                "debugLevel" to "lines,vars,source", "classpath" to jaxb.asPath
            ) {
                "src"("path" to ext["sourcesDir"])
                "include"("name" to "**/*.java")
                "include"("name" to "*.java")
            }

            "copy"("todir" to ext["classesDir"]) {
                "fileset"("dir" to ext["sourcesDir"], "erroronmissingdir" to false) {
                    "exclude"("name" to "**/*.java")
                }
            }
        }
    }
}

tasks.build {
    dependsOn("genJaxb")
}
