# httplib
HTTP library for PvPCafe Client.

## example usage
### including the library in your project using gradle
```kts
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.pvpcafe-client:httplib:1.0.1")
}
```

### using the library
```java
try (final HttpRequest request = HttpRequest.builder()
        .url("https://this-is.an-api.xyz")
        .setHeader("Authorization", "Bearer " + bearerToken)
        .setHeader("Accept", "application/json")
        .method("GET")
        .build()) {
    final HttpResponse response = request.execute();
    final String responseBody = response.as(StringResponseBody.class).toString();
} catch (Exception exception) {
    // handle the exception
}
```

## License
Licensed under Mozilla Public License 2.0 ([LICENSE](LICENSE)).
