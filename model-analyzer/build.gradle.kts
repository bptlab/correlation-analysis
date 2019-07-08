plugins {
    java
}

group = "de.hpi.bpt"
version = "1.0-SNAPSHOT"

dependencies {
    compile("org.camunda.bpm.model:camunda-bpmn-model:7.11.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}