plugins {
    java
}

group = "de.hpi.bpt"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

}

dependencies {
    implementation("nz.ac.waikato.cms.weka:weka-stable:3.8.3")
    implementation("org.apache.commons:commons-lang3:3.9")

    implementation("guru.nidi:graphviz-java:0.8.10")

}

repositories {
    mavenCentral()
}

