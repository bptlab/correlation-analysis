plugins {
    java
}

group = "de.hpi.bpt"
version = "1.0-SNAPSHOT"

dependencies {
    compile("org.camunda.bpm.model:camunda-bpmn-model:7.11.0")
    compile("org.apache.commons:commons-lang3:3.9")

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation("org.assertj:assertj-core:3.11.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}