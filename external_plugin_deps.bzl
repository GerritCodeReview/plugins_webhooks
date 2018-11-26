load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
    maven_jar(
        name = "mockito",
        artifact = "org.mockito:mockito-core:2.23.4",
        sha1 = "a35b6f8ffcfa786771eac7d7d903429e790fdf3f",
        deps = [
            "@byte-buddy//jar",
            "@byte-buddy-agent//jar",
            "@objenesis//jar",
        ],
    )

    BYTE_BUDDY_VERSION = "1.9.3"

    maven_jar(
        name = "byte-buddy",
        artifact = "net.bytebuddy:byte-buddy:" + BYTE_BUDDY_VERSION,
        sha1 = "f32e510b239620852fc9a2387fac41fd053d6a4d",
    )

    maven_jar(
        name = "byte-buddy-agent",
        artifact = "net.bytebuddy:byte-buddy-agent:" + BYTE_BUDDY_VERSION,
        sha1 = "f5b78c16cf4060664d80b6ca32d80dca4bd3d264",
    )

    maven_jar(
        name = "objenesis",
        artifact = "org.objenesis:objenesis:2.6",
        sha1 = "639033469776fd37c08358c6b92a4761feb2af4b",
    )
