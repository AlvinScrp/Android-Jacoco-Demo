plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "2.1.7"
    id("org.jetbrains.kotlin.jvm") version "1.6.21"

}

repositories {
    google()
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val asm_version = "9.1"
dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:7.3.0")
    implementation("com.google.guava:guava:30.1-jre")
    implementation("commons-io:commons-io:2.6")
    implementation("commons-codec:commons-codec:1.15")
    implementation("org.ow2.asm:asm:${asm_version}")
    implementation("org.ow2.asm:asm-commons:${asm_version}")
    implementation("org.ow2.asm:asm-tree:${asm_version}")
}

sourceSets.main {
    java.srcDirs("../plugin/src/main/java")
}
//
//sourceSets{
//    main{
//        java.srcDirs.add(file("../plugin/src/main/java"))
//    }
//}