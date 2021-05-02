# Kotlin Spring boot Native Application
in this tutorial, I have adjusted the Spring boot Native Java tutorial [here](https://spring-boot-cnb-hol-en.apps.pcfone.io/)
into the Kotlin equivalent to showcase the great process made in building
native spring boot applications, as a disclaimer this technology is still experimental
therefore its recommended not to be used in production environments.

## Requirements:
- Java 11
- Maven
- Docker

## Steps:

### Generate Spring boot project
Generate a new spring boot project using [Spring boot Initializer](https://start.spring.io/#!type=maven-project&language=kotlin&platformVersion=2.4.5.RELEASE&packaging=jar&jvmVersion=11&groupId=com.devexplained&artifactId=native-spring-boot&name=native-spring-boot&description=Demo%20project%20for%20Spring%20Boot%20Native%20with%20JDBC&packageName=com.devexplained.native-spring-boot&dependencies=web,jdbc,postgresql,native)
the chosen configuration were:
- Kotlin
- Maven
- Java 11
- Dependencies: web,jdbc,postgresql,native

### Add a controller
```kotlin
@RestController
class AvengerController(val jdbcTemplate: JdbcTemplate) {

    @GetMapping("/avengers")
    fun getAvengers(): List<Avenger> =
        jdbcTemplate.query(
            "SELECT id, name FROM avenger ORDER BY id"
        ) { rs, _ ->
            Avenger(
                rs.getInt("id"),
                rs.getString("name")
            )
        }

    @DeleteMapping("/avengers/{id}")
    fun deleteAvenger(@PathVariable("id") id: Int) {
        jdbcTemplate.update("DELETE FROM avenger WHERE id = ?", id)
    }

    @PostMapping("/avengers")
    fun postAvengers(@RequestBody avenger: Avenger): Avenger {
        val keyHolder: KeyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection: Connection ->
            val statement =
                connection.prepareStatement("INSERT INTO avenger(name) VALUES (?)", arrayOf("id"))
            statement.setString(1, avenger.name)
            statement
        }, keyHolder)
        avenger.copy(id = keyHolder.key!!.toInt())
        return avenger
    }

    data class Avenger(val id: Int, val name: String)

}
```
### Configure the database
create a local postgres db using docker compose by adding a `docker-compose.yml`
```yaml
version: '3'
services:
  database:
    image: "postgres:12.6"
    environment:
      - POSTGRES_DB=avenger
      - POSTGRES_USER=avenger
      - POSTGRES_PASSWORD=avenger
    ports:
      - "5432:5432"
```
the database can now be started by running in the command line:
```shell
docker-compose up
```
create the database scheme by adding the `/src/main/resources/schema.sql`
```sql
CREATE TABLE IF NOT EXISTS avenger
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(16) UNIQUE
);
```
add some records to the table bz adding a data file `/src/main/resources/data.sql`
```sql
INSERT INTO avenger(name) VALUES ('Ironman') ON CONFLICT ON CONSTRAINT avenger_name_key DO NOTHING;
INSERT INTO avenger(name) VALUES ('Capitan America') ON CONFLICT ON CONSTRAINT avenger_name_key DO NOTHING;
INSERT INTO avenger(name) VALUES ('Black Widow') ON CONFLICT ON CONSTRAINT avenger_name_key DO NOTHING;
INSERT INTO avenger(name) VALUES ('Hulk') ON CONFLICT ON CONSTRAINT avenger_name_key DO NOTHING;
INSERT INTO avenger(name) VALUES ('Thor') ON CONFLICT ON CONSTRAINT avenger_name_key DO NOTHING;
INSERT INTO avenger(name) VALUES ('Star Lord') ON CONFLICT ON CONSTRAINT avenger_name_key DO NOTHING;
INSERT INTO avenger(name) VALUES ('Gamora') ON CONFLICT ON CONSTRAINT avenger_name_key DO NOTHING;
INSERT INTO avenger(name) VALUES ('Rocket') ON CONFLICT ON CONSTRAINT avenger_name_key DO NOTHING;
INSERT INTO avenger(name) VALUES ('Drax') ON CONFLICT ON CONSTRAINT avenger_name_key DO NOTHING;
```
### Build and Run the application
first lets build the jar by running:
```shell
./mvnw clean package -Dmaven.test.skip=true
```
Run the application by:
```shell
java -jar  ${artifact-id}-0.0.1-SNAPSHOT.jar
````
in my case the artifact is call `native-spring-boot-0.0.1-SNAPSHOT.jar` but this can be configured in your `pom.xml` file 
```
java -jar target/native-spring-boot-0.0.1-SNAPSHOT.jar
```
application started in 1.967 seconds using the JVM:
```shell
2021-05-02 14:29:19.825  INFO 13350 --- [           main] c.d.n.NativeSpringBootApplicationKt      : Started NativeSpringBootApplicationKt in 1.967 seconds (JVM running for 2.99)
```
now we can test the application:
```shell
curl http://localhost:8080/avengers | jq
```
response:
```shell
[
  {
    "id": 1,
    "name": "Ironman"
  },
  {
    "id": 2,
    "name": "Capitan America"
  },
  {
    "id": 3,
    "name": "Black Widow"
  },
  {
    "id": 4,
    "name": "Hulk"
  },
  {
    "id": 5,
    "name": "Thor"
  },
  {
    "id": 6,
    "name": "Star Lord"
  },
  {
    "id": 7,
    "name": "Gamora"
  },
  {
    "id": 8,
    "name": "Rocket"
  },
  {
    "id": 9,
    "name": "Drax"
  }
]
```
creating a new avenger:
```shell
curl -s http://localhost:8080/avengers -d "{\"name\": \"Spiderman\"}" -H "Content-Type: application/json" | jq
```
response:
```shell
{
  "id": 0,
  "name": "Spiderman"
}
```
all the other endpotins should work similarly.

### Building Docker Container
we can generate the native docker image using spring boot by running the following command:
```shell
./mvnw spring-boot:build-image -Dmaven.test.skip=true
```
after my first attemp the build crashed with the following error:
```shell
Error: Image build request failed with exit status 137
```
I use docker desktop increasing, I increaseed the memory to 8 GB and try again:
````shell
./mvnw spring-boot:build-image -Dmaven.test.skip=true
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------< com.devexplained:native-spring-boot >-----------------
[INFO] Building native-spring-boot 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] >>> spring-boot-maven-plugin:2.4.5:build-image (default-cli) > package @ native-spring-boot >>>
[INFO] 
[INFO] --- maven-resources-plugin:3.2.0:resources (default-resources) @ native-spring-boot ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Using 'UTF-8' encoding to copy filtered properties files.
[INFO] Copying 1 resource
[INFO] Copying 2 resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.8.1:compile (default-compile) @ native-spring-boot ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- kotlin-maven-plugin:1.4.32:compile (compile) @ native-spring-boot ---
[INFO] Applied plugin: 'spring'
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by com.intellij.util.ReflectionUtil (file:/Users/folgerpersonal/.m2/repository/org/jetbrains/kotlin/kotlin-compiler/1.4.32/kotlin-compiler-1.4.32.jar) to method java.util.ResourceBundle.setParent(java.util.ResourceBundle)
WARNING: Please consider reporting this to the maintainers of com.intellij.util.ReflectionUtil
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
[INFO] 
[INFO] --- maven-resources-plugin:3.2.0:testResources (default-testResources) @ native-spring-boot ---
[INFO] Not copying test resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.8.1:testCompile (default-testCompile) @ native-spring-boot ---
[INFO] Not compiling test sources
[INFO] 
[INFO] --- kotlin-maven-plugin:1.4.32:test-compile (test-compile) @ native-spring-boot ---
[INFO] Test compilation is skipped
[INFO] 
[INFO] --- spring-aot-maven-plugin:0.9.2:test-generate (test-generate) @ native-spring-boot ---
[INFO] Spring Native operating mode: native
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.springframework.data.jpa.repository.support.EntityManagerBeanDefinitionRegistrarPostProcessor it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.springframework.security.config.annotation.web.configuration.AutowiredWebSecurityConfigurersIgnoreParents it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: io.netty.channel.socket.nio.NioSocketChannel it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.springframework.messaging.handler.annotation.MessageMapping it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: javax.transaction.Transactional it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL10Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL95Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL94Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL93Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL92Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL91Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL9Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgresPlusDialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL82Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL81Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.springframework.web.reactive.socket.server.upgrade.TomcatRequestUpgradeStrategy it will be skipped
[INFO] Not compiling test sources
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Using 'UTF-8' encoding to copy filtered properties files.
[INFO] Copying 5 resources
[INFO] 
[INFO] --- maven-surefire-plugin:2.22.2:test (default-test) @ native-spring-boot ---
[INFO] Tests are skipped.
[INFO] 
[INFO] --- spring-aot-maven-plugin:0.9.2:generate (generate) @ native-spring-boot ---
[INFO] Spring Native operating mode: native
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.springframework.data.jpa.repository.support.EntityManagerBeanDefinitionRegistrarPostProcessor it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.springframework.security.config.annotation.web.configuration.AutowiredWebSecurityConfigurersIgnoreParents it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: io.netty.channel.socket.nio.NioSocketChannel it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.springframework.messaging.handler.annotation.MessageMapping it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: javax.transaction.Transactional it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.springframework.data.jpa.repository.support.EntityManagerBeanDefinitionRegistrarPostProcessor it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.springframework.security.config.annotation.web.configuration.AutowiredWebSecurityConfigurersIgnoreParents it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: io.netty.channel.socket.nio.NioSocketChannel it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.springframework.messaging.handler.annotation.MessageMapping it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: javax.transaction.Transactional it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL10Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL95Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL94Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL93Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL92Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL91Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL9Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgresPlusDialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL82Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL81Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL10Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL95Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL94Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL93Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL92Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL91Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL9Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgresPlusDialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL82Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.hibernate.dialect.PostgreSQL81Dialect it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.springframework.web.reactive.socket.server.upgrade.TomcatRequestUpgradeStrategy it will be skipped
[WARNING] Failed verification check: this type was requested to be added to configuration but is not resolvable: org.springframework.web.reactive.socket.server.upgrade.TomcatRequestUpgradeStrategy it will be skipped
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 14 source files to /Users/folgerpersonal/developer/native-spring-boot/target/classes
[INFO] /Users/folgerpersonal/developer/native-spring-boot/target/generated-sources/spring-aot/src/main/java/org/springframework/aot/StaticSpringFactories.java: /Users/folgerpersonal/developer/native-spring-boot/target/generated-sources/spring-aot/src/main/java/org/springframework/aot/StaticSpringFactories.java uses or overrides a deprecated API.
[INFO] /Users/folgerpersonal/developer/native-spring-boot/target/generated-sources/spring-aot/src/main/java/org/springframework/aot/StaticSpringFactories.java: Recompile with -Xlint:deprecation for details.
[INFO] /Users/folgerpersonal/developer/native-spring-boot/target/generated-sources/spring-aot/src/main/java/org/springframework/core/io/support/SpringFactoriesLoader.java: Some input files use unchecked or unsafe operations.
[INFO] /Users/folgerpersonal/developer/native-spring-boot/target/generated-sources/spring-aot/src/main/java/org/springframework/core/io/support/SpringFactoriesLoader.java: Recompile with -Xlint:unchecked for details.
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Using 'UTF-8' encoding to copy filtered properties files.
[INFO] Copying 5 resources
[INFO] 
[INFO] --- maven-jar-plugin:3.2.0:jar (default-jar) @ native-spring-boot ---
[INFO] Building jar: /Users/folgerpersonal/developer/native-spring-boot/target/native-spring-boot-0.0.1-SNAPSHOT.jar
[INFO] 
[INFO] --- spring-boot-maven-plugin:2.4.5:repackage (repackage) @ native-spring-boot ---
[INFO] Replacing main artifact with repackaged archive
[INFO] 
[INFO] <<< spring-boot-maven-plugin:2.4.5:build-image (default-cli) < package @ native-spring-boot <<<
[INFO] 
[INFO] 
[INFO] --- spring-boot-maven-plugin:2.4.5:build-image (default-cli) @ native-spring-boot ---
[INFO] Building image 'docker.io/library/native-spring-boot:0.0.1-SNAPSHOT'
[INFO] 
[INFO]  > Pulling builder image 'docker.io/paketobuildpacks/builder:tiny' 100%
[INFO]  > Pulled builder image 'paketobuildpacks/builder@sha256:957b1e3c60c175e9b30585d70a89fdd9c3813456d30907745a7b7a3643a1ac31'
[INFO]  > Pulling run image 'docker.io/paketobuildpacks/run:tiny-cnb' 100%
[INFO]  > Pulled run image 'paketobuildpacks/run@sha256:57768a9ede0bed24b6c176a673e2cc825c65ac319be0baa32661eb4db722dba3'
[INFO]  > Executing lifecycle version v0.11.2
[INFO]  > Using build cache volume 'pack-cache-3cc1aa5deb48.build'
[INFO] 
[INFO]  > Running creator
[INFO]     [creator]     ===> DETECTING
[INFO]     [creator]     4 of 11 buildpacks participating
[INFO]     [creator]     paketo-buildpacks/graalvm        6.0.0
[INFO]     [creator]     paketo-buildpacks/executable-jar 5.0.0
[INFO]     [creator]     paketo-buildpacks/spring-boot    4.2.0
[INFO]     [creator]     paketo-buildpacks/native-image   4.0.0
[INFO]     [creator]     ===> ANALYZING
[INFO]     [creator]     Previous image with name "docker.io/library/native-spring-boot:0.0.1-SNAPSHOT" not found
[INFO]     [creator]     ===> RESTORING
[INFO]     [creator]     ===> BUILDING
[INFO]     [creator]     
[INFO]     [creator]     Paketo GraalVM Buildpack 6.0.0
[INFO]     [creator]       https://github.com/paketo-buildpacks/graalvm
[INFO]     [creator]       Build Configuration:
[INFO]     [creator]         $BP_JVM_VERSION              11.*            the Java version
[INFO]     [creator]       Launch Configuration:
[INFO]     [creator]         $BPL_JVM_HEAD_ROOM           0               the headroom in memory calculation
[INFO]     [creator]         $BPL_JVM_LOADED_CLASS_COUNT  35% of classes  the number of loaded classes in memory calculation
[INFO]     [creator]         $BPL_JVM_THREAD_COUNT        250             the number of threads in memory calculation
[INFO]     [creator]         $JAVA_TOOL_OPTIONS                           the JVM launch flags
[INFO]     [creator]       GraalVM JDK 11.0.10: Contributing to layer
[INFO]     [creator]         Downloading from https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.0.0.2/graalvm-ce-java11-linux-amd64-21.0.0.2.tar.gz
[INFO]     [creator]         Verifying checksum
[INFO]     [creator]         Expanding to /layers/paketo-buildpacks_graalvm/jdk
[INFO]     [creator]         Adding 129 container CA certificates to JVM truststore
[INFO]     [creator]       GraalVM Native Image Substrate VM 11.0.10
[INFO]     [creator]         Downloading from https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.0.0.2/native-image-installable-svm-java11-linux-amd64-21.0.0.2.jar
[INFO]     [creator]         Verifying checksum
[INFO]     [creator]         Installing substrate VM
[INFO]     [creator]     Processing Component archive: /tmp/24e5a08e2714aee343b22c266285090721ff882ab0a31b7e8e4a68585c38f421/native-image-installable-svm-java11-linux-amd64-21.0.0.2.jar
[INFO]     [creator]     Installing new component: Native Image (org.graalvm.native-image, version 21.0.0.2)
[INFO]     [creator]         Writing env.build/JAVA_HOME.override
[INFO]     [creator]         Writing env.build/JDK_HOME.override
[INFO]     [creator]     
[INFO]     [creator]     Paketo Executable JAR Buildpack 5.0.0
[INFO]     [creator]       https://github.com/paketo-buildpacks/executable-jar
[INFO]     [creator]       Class Path: Contributing to layer
[INFO]     [creator]         Writing env.build/CLASSPATH.delim
[INFO]     [creator]         Writing env.build/CLASSPATH.prepend
[INFO]     [creator]     
[INFO]     [creator]     Paketo Spring Boot Buildpack 4.2.0
[INFO]     [creator]       https://github.com/paketo-buildpacks/spring-boot
[INFO]     [creator]       Class Path: Contributing to layer
[INFO]     [creator]         Writing env.build/CLASSPATH.append
[INFO]     [creator]         Writing env.build/CLASSPATH.delim
[INFO]     [creator]       Image labels:
[INFO]     [creator]         org.opencontainers.image.title
[INFO]     [creator]         org.opencontainers.image.version
[INFO]     [creator]         org.springframework.boot.spring-configuration-metadata.json
[INFO]     [creator]         org.springframework.boot.version
[INFO]     [creator]     
[INFO]     [creator]     Paketo Native Image Buildpack 4.0.0
[INFO]     [creator]       https://github.com/paketo-buildpacks/native-image
[INFO]     [creator]       Build Configuration:
[INFO]     [creator]         $BP_NATIVE_IMAGE                  true  enable native image build
[INFO]     [creator]         $BP_NATIVE_IMAGE_BUILD_ARGUMENTS        arguments to pass to the native-image command
[INFO]     [creator]       Native Image: Contributing to layer
[INFO]     [creator]         GraalVM Version 21.0.0.2 (Java Version 11.0.10+8-jvmci-21.0-b06)
[INFO]     [creator]         Executing native-image -H:+StaticExecutableWithDynamicLibC -H:Name=/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt -cp /workspace:/workspace/BOOT-INF/classes:/workspace/BOOT-INF/lib/spring-boot-2.4.5.jar:/workspace/BOOT-INF/lib/spring-boot-autoconfigure-2.4.5.jar:/workspace/BOOT-INF/lib/logback-classic-1.2.3.jar:/workspace/BOOT-INF/lib/logback-core-1.2.3.jar:/workspace/BOOT-INF/lib/log4j-to-slf4j-2.13.3.jar:/workspace/BOOT-INF/lib/log4j-api-2.13.3.jar:/workspace/BOOT-INF/lib/jul-to-slf4j-1.7.30.jar:/workspace/BOOT-INF/lib/jakarta.annotation-api-1.3.5.jar:/workspace/BOOT-INF/lib/snakeyaml-1.27.jar:/workspace/BOOT-INF/lib/HikariCP-3.4.5.jar:/workspace/BOOT-INF/lib/slf4j-api-1.7.30.jar:/workspace/BOOT-INF/lib/spring-jdbc-5.3.6.jar:/workspace/BOOT-INF/lib/spring-beans-5.3.6.jar:/workspace/BOOT-INF/lib/spring-tx-5.3.6.jar:/workspace/BOOT-INF/lib/jackson-datatype-jdk8-2.11.4.jar:/workspace/BOOT-INF/lib/jackson-datatype-jsr310-2.11.4.jar:/workspace/BOOT-INF/lib/jackson-module-parameter-names-2.11.4.jar:/workspace/BOOT-INF/lib/tomcat-embed-core-9.0.45.jar:/workspace/BOOT-INF/lib/jakarta.el-3.0.3.jar:/workspace/BOOT-INF/lib/tomcat-embed-websocket-9.0.45.jar:/workspace/BOOT-INF/lib/spring-web-5.3.6.jar:/workspace/BOOT-INF/lib/spring-webmvc-5.3.6.jar:/workspace/BOOT-INF/lib/spring-aop-5.3.6.jar:/workspace/BOOT-INF/lib/spring-context-5.3.6.jar:/workspace/BOOT-INF/lib/spring-expression-5.3.6.jar:/workspace/BOOT-INF/lib/jackson-module-kotlin-2.11.4.jar:/workspace/BOOT-INF/lib/jackson-databind-2.11.4.jar:/workspace/BOOT-INF/lib/jackson-core-2.11.4.jar:/workspace/BOOT-INF/lib/jackson-annotations-2.11.4.jar:/workspace/BOOT-INF/lib/kotlin-reflect-1.4.32.jar:/workspace/BOOT-INF/lib/kotlin-stdlib-1.4.32.jar:/workspace/BOOT-INF/lib/kotlin-stdlib-common-1.4.32.jar:/workspace/BOOT-INF/lib/annotations-13.0.jar:/workspace/BOOT-INF/lib/kotlin-stdlib-jdk8-1.4.32.jar:/workspace/BOOT-INF/lib/kotlin-stdlib-jdk7-1.4.32.jar:/workspace/BOOT-INF/lib/spring-native-0.9.2.jar:/workspace/BOOT-INF/lib/postgresql-42.2.19.jar:/workspace/BOOT-INF/lib/checker-qual-3.5.0.jar:/workspace/BOOT-INF/lib/spring-core-5.3.6.jar:/workspace/BOOT-INF/lib/spring-jcl-5.3.6.jar:/workspace/BOOT-INF/lib/spring-boot-jarmode-layertools-2.4.5.jar com.devexplained.nativespringboot.NativeSpringBootApplicationKt
[INFO]     [creator]     [/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt:177]    classlist:   3,557.72 ms,  1.19 GB
[INFO]     [creator]     [/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt:177]        (cap):     787.21 ms,  1.19 GB
[INFO]     [creator]     [/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt:177]        setup:   3,538.81 ms,  1.19 GB
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.transaction.ReactiveTransactionManager. Reason: java.lang.NoClassDefFoundError: reactor/core/publisher/Mono.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration$Dbcp2. Reason: java.lang.NoClassDefFoundError: org/apache/commons/dbcp2/BasicDataSource.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.web.multipart.commons.CommonsMultipartResolver. Reason: java.lang.NoClassDefFoundError: org/apache/commons/fileupload/FileItemFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration. Reason: java.lang.NoClassDefFoundError: net/sf/ehcache/CacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/redis/connection/RedisConnectionFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator. Reason: java.lang.NoClassDefFoundError: org/aspectj/util/PartialOrder$PartialComparable.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration. Reason: java.lang.NoClassDefFoundError: com/google/gson/GsonBuilder.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.HazelcastCacheConfiguration. Reason: java.lang.NoClassDefFoundError: com/hazelcast/core/HazelcastInstance.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator. Reason: java.lang.NoClassDefFoundError: org/aspectj/util/PartialOrder$PartialComparable.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration$OracleUcp. Reason: java.lang.NoClassDefFoundError: oracle/ucp/jdbc/PoolDataSourceImpl.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration. Reason: java.lang.NoClassDefFoundError: javax/validation/ValidatorFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/redis/serializer/RedisSerializer.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jsonb.JsonbAutoConfiguration. Reason: java.lang.NoClassDefFoundError: javax/json/bind/Jsonb.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.CouchbaseCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/couchbase/CouchbaseClientFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.transaction.reactive.TransactionalOperator. Reason: java.lang.NoClassDefFoundError: org/reactivestreams/Publisher.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.JCacheCacheConfiguration. Reason: java.lang.NoClassDefFoundError: javax/cache/CacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/neo4j/core/transaction/Neo4jTransactionManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration$Tomcat. Reason: java.lang.NoClassDefFoundError: org/apache/tomcat/jdbc/pool/DataSource.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.InfinispanCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/infinispan/manager/EmbeddedCacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.CaffeineCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/cache/caffeine/CaffeineCacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.transaction.ReactiveTransactionManager. Reason: java.lang.NoClassDefFoundError: reactor/core/publisher/Mono.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration$Dbcp2. Reason: java.lang.NoClassDefFoundError: org/apache/commons/dbcp2/BasicDataSource.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.web.multipart.commons.CommonsMultipartResolver. Reason: java.lang.NoClassDefFoundError: org/apache/commons/fileupload/FileItemFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration. Reason: java.lang.NoClassDefFoundError: net/sf/ehcache/CacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/redis/connection/RedisConnectionFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator. Reason: java.lang.NoClassDefFoundError: org/aspectj/util/PartialOrder$PartialComparable.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration. Reason: java.lang.NoClassDefFoundError: com/google/gson/GsonBuilder.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.HazelcastCacheConfiguration. Reason: java.lang.NoClassDefFoundError: com/hazelcast/core/HazelcastInstance.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator. Reason: java.lang.NoClassDefFoundError: org/aspectj/util/PartialOrder$PartialComparable.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration$OracleUcp. Reason: java.lang.NoClassDefFoundError: oracle/ucp/jdbc/PoolDataSourceImpl.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration. Reason: java.lang.NoClassDefFoundError: javax/validation/ValidatorFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/redis/serializer/RedisSerializer.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jsonb.JsonbAutoConfiguration. Reason: java.lang.NoClassDefFoundError: javax/json/bind/Jsonb.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.CouchbaseCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/couchbase/CouchbaseClientFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.transaction.reactive.TransactionalOperator. Reason: java.lang.NoClassDefFoundError: org/reactivestreams/Publisher.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.JCacheCacheConfiguration. Reason: java.lang.NoClassDefFoundError: javax/cache/CacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/neo4j/core/transaction/Neo4jTransactionManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration$Tomcat. Reason: java.lang.NoClassDefFoundError: org/apache/tomcat/jdbc/pool/DataSource.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.InfinispanCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/infinispan/manager/EmbeddedCacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.CaffeineCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/cache/caffeine/CaffeineCacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.transaction.ReactiveTransactionManager. Reason: java.lang.NoClassDefFoundError: reactor/core/publisher/Mono.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration$Dbcp2. Reason: java.lang.NoClassDefFoundError: org/apache/commons/dbcp2/BasicDataSource.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration. Reason: java.lang.NoClassDefFoundError: net/sf/ehcache/CacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/redis/connection/RedisConnectionFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration. Reason: java.lang.NoClassDefFoundError: com/google/gson/GsonBuilder.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.HazelcastCacheConfiguration. Reason: java.lang.NoClassDefFoundError: com/hazelcast/core/HazelcastInstance.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator. Reason: java.lang.NoClassDefFoundError: org/aspectj/util/PartialOrder$PartialComparable.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/redis/serializer/RedisSerializer.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jsonb.JsonbAutoConfiguration. Reason: java.lang.NoClassDefFoundError: javax/json/bind/Jsonb.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.CouchbaseCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/couchbase/CouchbaseClientFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.transaction.reactive.TransactionalOperator. Reason: java.lang.NoClassDefFoundError: org/reactivestreams/Publisher.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration$Tomcat. Reason: java.lang.NoClassDefFoundError: org/apache/tomcat/jdbc/pool/DataSource.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.CaffeineCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/cache/caffeine/CaffeineCacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.web.multipart.commons.CommonsMultipartResolver. Reason: java.lang.NoClassDefFoundError: org/apache/commons/fileupload/FileItemFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator. Reason: java.lang.NoClassDefFoundError: org/aspectj/util/PartialOrder$PartialComparable.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration$OracleUcp. Reason: java.lang.NoClassDefFoundError: oracle/ucp/jdbc/PoolDataSourceImpl.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration. Reason: java.lang.NoClassDefFoundError: javax/validation/ValidatorFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.JCacheCacheConfiguration. Reason: java.lang.NoClassDefFoundError: javax/cache/CacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/neo4j/core/transaction/Neo4jTransactionManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.InfinispanCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/infinispan/manager/EmbeddedCacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.transaction.ReactiveTransactionManager. Reason: java.lang.NoClassDefFoundError: reactor/core/publisher/Mono.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration$Dbcp2. Reason: java.lang.NoClassDefFoundError: org/apache/commons/dbcp2/BasicDataSource.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration. Reason: java.lang.NoClassDefFoundError: net/sf/ehcache/CacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/redis/connection/RedisConnectionFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration. Reason: java.lang.NoClassDefFoundError: com/google/gson/GsonBuilder.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.HazelcastCacheConfiguration. Reason: java.lang.NoClassDefFoundError: com/hazelcast/core/HazelcastInstance.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator. Reason: java.lang.NoClassDefFoundError: org/aspectj/util/PartialOrder$PartialComparable.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/redis/serializer/RedisSerializer.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jsonb.JsonbAutoConfiguration. Reason: java.lang.NoClassDefFoundError: javax/json/bind/Jsonb.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.CouchbaseCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/couchbase/CouchbaseClientFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.transaction.reactive.TransactionalOperator. Reason: java.lang.NoClassDefFoundError: org/reactivestreams/Publisher.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration$Tomcat. Reason: java.lang.NoClassDefFoundError: org/apache/tomcat/jdbc/pool/DataSource.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.CaffeineCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/cache/caffeine/CaffeineCacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.web.multipart.commons.CommonsMultipartResolver. Reason: java.lang.NoClassDefFoundError: org/apache/commons/fileupload/FileItemFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator. Reason: java.lang.NoClassDefFoundError: org/aspectj/util/PartialOrder$PartialComparable.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration$OracleUcp. Reason: java.lang.NoClassDefFoundError: oracle/ucp/jdbc/PoolDataSourceImpl.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration. Reason: java.lang.NoClassDefFoundError: javax/validation/ValidatorFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.JCacheCacheConfiguration. Reason: java.lang.NoClassDefFoundError: javax/cache/CacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/neo4j/core/transaction/Neo4jTransactionManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.InfinispanCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/infinispan/manager/EmbeddedCacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.transaction.ReactiveTransactionManager. Reason: java.lang.NoClassDefFoundError: reactor/core/publisher/Mono.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration$Dbcp2. Reason: java.lang.NoClassDefFoundError: org/apache/commons/dbcp2/BasicDataSource.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration. Reason: java.lang.NoClassDefFoundError: net/sf/ehcache/CacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/redis/connection/RedisConnectionFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration. Reason: java.lang.NoClassDefFoundError: com/google/gson/GsonBuilder.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.HazelcastCacheConfiguration. Reason: java.lang.NoClassDefFoundError: com/hazelcast/core/HazelcastInstance.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator. Reason: java.lang.NoClassDefFoundError: org/aspectj/util/PartialOrder$PartialComparable.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/redis/serializer/RedisSerializer.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jsonb.JsonbAutoConfiguration. Reason: java.lang.NoClassDefFoundError: javax/json/bind/Jsonb.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.CouchbaseCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/couchbase/CouchbaseClientFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.transaction.reactive.TransactionalOperator. Reason: java.lang.NoClassDefFoundError: org/reactivestreams/Publisher.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration$Tomcat. Reason: java.lang.NoClassDefFoundError: org/apache/tomcat/jdbc/pool/DataSource.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.CaffeineCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/cache/caffeine/CaffeineCacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.web.multipart.commons.CommonsMultipartResolver. Reason: java.lang.NoClassDefFoundError: org/apache/commons/fileupload/FileItemFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator. Reason: java.lang.NoClassDefFoundError: org/aspectj/util/PartialOrder$PartialComparable.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration$OracleUcp. Reason: java.lang.NoClassDefFoundError: oracle/ucp/jdbc/PoolDataSourceImpl.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration. Reason: java.lang.NoClassDefFoundError: javax/validation/ValidatorFactory.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.JCacheCacheConfiguration. Reason: java.lang.NoClassDefFoundError: javax/cache/CacheManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration. Reason: java.lang.NoClassDefFoundError: org/springframework/data/neo4j/core/transaction/Neo4jTransactionManager.
[INFO]     [creator]     WARNING: Could not register reflection metadata for org.springframework.boot.autoconfigure.cache.InfinispanCacheConfiguration. Reason: java.lang.NoClassDefFoundError: org/infinispan/manager/EmbeddedCacheManager.
[INFO]     [creator]     [/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt:177]     (clinit):   1,951.04 ms,  2.98 GB
[INFO]     [creator]     [/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt:177]   (typeflow):  41,473.82 ms,  2.98 GB
[INFO]     [creator]     [/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt:177]    (objects):  31,892.24 ms,  2.98 GB
[INFO]     [creator]     [/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt:177]   (features):   5,760.58 ms,  2.98 GB
[INFO]     [creator]     [/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt:177]     analysis:  83,787.91 ms,  2.98 GB
[INFO]     [creator]     [/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt:177]     universe:   3,374.26 ms,  2.98 GB
[INFO]     [creator]     [/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt:177]      (parse):  12,538.96 ms,  3.62 GB
[INFO]     [creator]     [/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt:177]     (inline):  13,333.76 ms,  4.56 GB
[INFO]     [creator]     [/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt:177]    (compile):  55,329.80 ms,  4.84 GB
[INFO]     [creator]     [/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt:177]      compile:  86,909.29 ms,  4.77 GB
[INFO]     [creator]     [/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt:177]        image:  11,274.10 ms,  4.60 GB
[INFO]     [creator]     [/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt:177]        write:   2,256.93 ms,  4.60 GB
[INFO]     [creator]     [/layers/paketo-buildpacks_native-image/native-image/com.devexplained.nativespringboot.NativeSpringBootApplicationKt:177]      [total]: 196,076.94 ms,  4.60 GB
[INFO]     [creator]       Removing bytecode
[INFO]     [creator]       Process types:
[INFO]     [creator]         native-image: /workspace/com.devexplained.nativespringboot.NativeSpringBootApplicationKt (direct)
[INFO]     [creator]         task:         /workspace/com.devexplained.nativespringboot.NativeSpringBootApplicationKt (direct)
[INFO]     [creator]         web:          /workspace/com.devexplained.nativespringboot.NativeSpringBootApplicationKt (direct)
[INFO]     [creator]     ===> EXPORTING
[INFO]     [creator]     Adding 1/1 app layer(s)
[INFO]     [creator]     Adding layer 'launcher'
[INFO]     [creator]     Adding layer 'config'
[INFO]     [creator]     Adding layer 'process-types'
[INFO]     [creator]     Adding label 'io.buildpacks.lifecycle.metadata'
[INFO]     [creator]     Adding label 'io.buildpacks.build.metadata'
[INFO]     [creator]     Adding label 'io.buildpacks.project.metadata'
[INFO]     [creator]     Adding label 'org.opencontainers.image.title'
[INFO]     [creator]     Adding label 'org.opencontainers.image.version'
[INFO]     [creator]     Adding label 'org.springframework.boot.spring-configuration-metadata.json'
[INFO]     [creator]     Adding label 'org.springframework.boot.version'
[INFO]     [creator]     Setting default process type 'web'
[INFO]     [creator]     Saving docker.io/library/native-spring-boot:0.0.1-SNAPSHOT...
[INFO]     [creator]     *** Images (3326c1a82f73):
[INFO]     [creator]           docker.io/library/native-spring-boot:0.0.1-SNAPSHOT
[INFO]     [creator]     Adding cache layer 'paketo-buildpacks/graalvm:jdk'
[INFO]     [creator]     Adding cache layer 'paketo-buildpacks/native-image:native-image'
[INFO] 
[INFO] Successfully built image 'docker.io/library/native-spring-boot:0.0.1-SNAPSHOT'
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  08:51 min
[INFO] Finished at: 2021-05-02T15:17:06+02:00
[INFO] ------------------------------------------------------------------------
````
at the end the build took almost 8 min and I practically was not able to use my laptop until it was done.
run the docker application
```shell
 docker run --rm \
 -p 8080:8080 \
 -m 150m \
 -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/avenger \
 docker.io/library/native-spring-boot:0.0.1-SNAPSHOT
```
the result:
```shell
2021-05-02 14:39:35.874  INFO 1 --- [           main] o.s.nativex.NativeListener               : This application is bootstrapped with code generated with Spring AOT

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.4.5)

2021-05-02 14:39:35.877  INFO 1 --- [           main] o.s.boot.SpringApplication               : Starting application using Java 11.0.10 on b5aa91a3bf80 with PID 1 (started by cnb in /workspace)
2021-05-02 14:39:35.877  INFO 1 --- [           main] o.s.boot.SpringApplication               : No active profile set, falling back to default profiles: default
2021-05-02 14:39:35.914  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
May 02, 2021 2:39:35 PM org.apache.coyote.AbstractProtocol init
INFO: Initializing ProtocolHandler ["http-nio-8080"]
May 02, 2021 2:39:35 PM org.apache.catalina.core.StandardService startInternal
INFO: Starting service [Tomcat]
May 02, 2021 2:39:35 PM org.apache.catalina.core.StandardEngine startInternal
INFO: Starting Servlet engine: [Apache Tomcat/9.0.45]
2021-05-02 14:39:35.915  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 38 ms
May 02, 2021 2:39:35 PM org.apache.catalina.core.ApplicationContext log
INFO: Initializing Spring embedded WebApplicationContext
2021-05-02 14:39:35.924  INFO 1 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2021-05-02 14:39:35.943  INFO 1 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2021-05-02 14:39:35.970  INFO 1 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
May 02, 2021 2:39:35 PM org.apache.coyote.AbstractProtocol start
INFO: Starting ProtocolHandler ["http-nio-8080"]
2021-05-02 14:39:35.985  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2021-05-02 14:39:35.985  INFO 1 --- [           main] o.s.boot.SpringApplication               : Started application in 0.115 seconds (JVM running for 0.117)
```
the application started in 0.115 sec 
lets test by making a rest call:
```shell
curl http://localhost:8080/avengers | jq
```
response:
```shell
[
  {
    "id": 1,
    "name": "Ironman"
  },
  {
    "id": 2,
    "name": "Capitan America"
  },
  {
    "id": 3,
    "name": "Black Widow"
  },
  {
    "id": 4,
    "name": "Hulk"
  },
  {
    "id": 5,
    "name": "Thor"
  },
  {
    "id": 6,
    "name": "Star Lord"
  },
  {
    "id": 7,
    "name": "Gamora"
  },
  {
    "id": 8,
    "name": "Rocket"
  },
  {
    "id": 9,
    "name": "Drax"
  }
]
```

