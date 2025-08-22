plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    // Resilience4j
    api("org.springframework.boot:spring-boot-starter-aop")
    api("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")

    compileOnly("org.springframework.boot:spring-boot-starter-web")

    // Lombok
    testFixturesCompileOnly("org.projectlombok:lombok")
    testFixturesAnnotationProcessor("org.projectlombok:lombok")

    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
}
