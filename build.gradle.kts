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

val jaxb: Configuration = configurations.create("jaxb")

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

val schemaDirectory = "src/main/resources/schema"

tasks.register("genJaxb") {
    ext["sourcesDir"] = "${projectDir}/src/main/java"
    ext["schema"] = schemaDirectory

    doLast {
        ant.withGroovyBuilder {
            "taskdef"(
                "name" to "xjc", "classname" to "com.sun.tools.xjc.XJCTask",
                "classpath" to jaxb.asPath)
            ext["sourcesDir"]?.let { mkdir(it) }

            "xjc"(
                "destdir" to ext["sourcesDir"],
                "package" to "com.annakhuseinova.springwsclientwithgradle.xjc",
            ) {
                "arg"("value" to "-wsdl")
                "schema"("dir" to ext["schema"], "includes" to "**/*.xsd, **/*.wsdl")
                "produces"("dir" to ext[" sourcesDir"], "includes" to "**/*.java")
            }
        }
    }
}

tasks.build {
    dependsOn("genJaxb")
}
