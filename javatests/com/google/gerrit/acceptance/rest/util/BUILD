load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "util",
    testonly = True,
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//java/com/google/gerrit/acceptance:lib",
        "//lib/commons:lang3",
    ],
)
