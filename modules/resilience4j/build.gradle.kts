plugins {
    `java-library`
}

dependencies {
    // Resilience4j
    api("org.springframework.boot:spring-boot-starter-aop")
    api("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")

    compileOnly("org.springframework.boot:spring-boot-starter-web")
}
