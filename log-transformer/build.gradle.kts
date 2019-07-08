plugins {
    java
}

group = "de.hpi.bpt"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    compile("net.sf.supercsv:super-csv:2.4.0")
    compile("org.apache.commons:commons-lang3:3.9")
    compile("com.h2database:h2:1.4.199")

    implementation("de.hpi.bpt:model-analyzer:1.0-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation("org.assertj:assertj-core:3.11.1")
}
