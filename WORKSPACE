workspace(name = "webhooks")
load("//:bazlets.bzl", "load_bazlets")

load_bazlets(
    commit = "28aa2290c7f7742261d69b358f3de30d2e87c13b",
)

#Snapshot Plugin API
#load(
#    "@com_googlesource_gerrit_bazlets//:gerrit_api_maven_local.bzl",
#    "gerrit_api_maven_local",
#)

# Load snapshot Plugin API
#gerrit_api_maven_local()

# Release Plugin API
load(
   "@com_googlesource_gerrit_bazlets//:gerrit_api.bzl",
   "gerrit_api",
)

# Load release Plugin API
gerrit_api()

load("@com_googlesource_gerrit_bazlets//tools:maven_jar.bzl", "maven_jar")

maven_jar(
    name = "mockito",
    artifact = "org.mockito:mockito-core:2.7.21",
    sha1 = "23e9f7bfb9717e849a05b84c29ee3ac723f1a653",
)

maven_jar(
    name = "byte-buddy",
    artifact = "net.bytebuddy:byte-buddy:1.6.11",
    sha1 = "8a8f9409e27f1d62c909c7eef2aa7b3a580b4901",
)

maven_jar(
    name = "objenesis",
    artifact = "org.objenesis:objenesis:2.5",
    sha1 = "612ecb799912ccf77cba9b3ed8c813da086076e9",
)
