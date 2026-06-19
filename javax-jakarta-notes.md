# javax vs jakarta: DataSource & Naming Notes

## `jakarta.sql.DataSource`

- Exists. It's the renamed counterpart of `javax.sql.DataSource`, part of the Jakarta EE namespace migration (Jakarta EE 9+, after Eclipse Foundation took over from Oracle and had to drop the `javax` package prefix for trademark reasons).
- Technically a JDBC interface, but it's bundled under the Jakarta EE umbrella because EE platform specs (like JTA) depend on it.
- **Java SE / JDBC itself never migrated** — so most JDBC drivers and connection pools still implement `javax.sql.DataSource`, not the jakarta version.
- You only really encounter `jakarta.sql.DataSource` inside a Jakarta EE 9+ container context (e.g. implementing EE-specific SPIs like `jakarta.resource.spi.ManagedConnectionFactory`).

### Gradle setup

**Full Jakarta EE platform API (compile-only, container supplies impl at runtime):**
```gradle
dependencies {
    compileOnly 'jakarta.platform:jakarta.jakartaee-api:10.0.0'
}
```

**Lighter — Web Profile only:**
```gradle
dependencies {
    compileOnly 'jakarta.platform:jakarta.jakartaee-web-api:10.0.0'
}
```

**Standalone / non-container apps (Spring Boot, plain Java):**
You almost never need `jakarta.sql` here. Just pull in a connection pool + driver — `javax.sql.DataSource` comes from the JDK itself:
```gradle
dependencies {
    implementation 'com.zaxxer:HikariCP:5.1.0'
    runtimeOnly 'org.postgresql:postgresql:42.7.3'
}
```

> **Gotcha:** Confirm which namespace your pool/driver actually targets before assuming you need the jakarta artifact. Most of the JDBC ecosystem is still `javax.sql`.

---

## `javax.naming` (JNDI)

- Part of Java SE standard library (`java.naming` module) — **not** a Jakarta EE artifact.
- No Gradle dependency needed in the normal case; it ships with the JDK.

### Modular builds (JPMS)
If using `module-info.java`, declare it there, not in Gradle:
```java
module com.example.myapp {
    requires java.naming;
}
```

### Custom runtime images (jlink)
If building a stripped-down JRE with `jlink`, make sure `java.naming` is explicitly included:
```gradle
jlink {
    options = ['--add-modules', 'java.naming']
}
```
(exact syntax depends on the jlink Gradle plugin in use, e.g. Badass JLink)

### Need a working JNDI implementation outside a real container?
The API classes are always present, but if you want actual JNDI lookups to work (e.g. in tests, without an app server), you need a separate implementation:
```gradle
dependencies {
    testImplementation 'org.osjava:osjava-jndi:1.2.3' // check current version
}
```

---

## TL;DR

| Package | Where it comes from | Gradle dependency needed? |
|---|---|---|
| `javax.sql.DataSource` | JDK / JDBC | No — built in |
| `jakarta.sql.DataSource` | Jakarta EE 9+ | Yes — `jakarta.platform:jakarta.jakartaee-api` (or web-api) |
| `javax.naming` | JDK (`java.naming` module) | No — built in (declare in `module-info.java` if modular) |
