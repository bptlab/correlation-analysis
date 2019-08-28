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
    compile("nz.ac.waikato.cms.weka:weka-stable:3.8.3")
    implementation("org.apache.commons:commons-lang3:3.9")
    compile("javax.xml.bind:jaxb-api:2.3.1")


    implementation("guru.nidi:graphviz-java:0.8.10")

}

repositories {
    mavenCentral()
}

