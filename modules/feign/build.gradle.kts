plugins {
    `java-library`
}

dependencies {
    // Feign Client
    api("org.springframework.cloud:spring-cloud-starter-openfeign")
    runtimeOnly("io.github.openfeign:feign-hc5")
}
