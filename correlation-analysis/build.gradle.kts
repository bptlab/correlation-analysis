plugins {
    java
    application
}

group = "de.hpi.bpt"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClassName = "de.hpi.bpt.correlationanalysis.Demo"
}

dependencies {
    compile("nz.ac.waikato.cms.weka:weka-stable:3.8.3")
    implementation("org.apache.commons:commons-lang3:3.9")
    compile("javax.xml.bind:jaxb-api:2.3.1")
    compile("org.slf4j:slf4j-simple:1.7.28")


    implementation("guru.nidi:graphviz-java:0.8.10")

    compile("ro.pippo:pippo-core:1.12.0")
    compile("ro.pippo:pippo-freemarker:1.12.0")
    compile("ro.pippo:pippo-jetty:1.12.0")
}

repositories {
    mavenCentral()
}

