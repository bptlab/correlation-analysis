plugins {
    java
    idea
}

group = "de.hpi.bpt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("de.hpi.bpt:model-analyzer:1.0-SNAPSHOT")
    implementation("de.hpi.bpt:log-transformer:1.0-SNAPSHOT")
    implementation("de.hpi.bpt:feature-evaluator:1.0-SNAPSHOT")

    implementation("guru.nidi:graphviz-java:0.8.10")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}