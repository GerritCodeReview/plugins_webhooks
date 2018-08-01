load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
    maven_jar(
        name = "mockito",
        artifact = "org.mockito:mockito-core:2.9.0",
        sha1 = "f28b9606eca8da77e10df30a7e301f589733143e",
        deps = [
            "@byte-buddy//jar",
            "@objenesis//jar",
        ],
    )

    maven_jar(
        name = "byte-buddy",
        artifact = "net.bytebuddy:byte-buddy:1.7.0",
        sha1 = "48481d20ed4334ee0abfe8212ecb44e0233a97b5",
    )

    maven_jar(
        name = "objenesis",
        artifact = "org.objenesis:objenesis:2.6",
        sha1 = "639033469776fd37c08358c6b92a4761feb2af4b",
    )
