load("//tools/bzl:junit.bzl", "junit_tests")

junit_tests(
    name = "server_tests",
    srcs = glob(["*.java"]),
    tags = ["no_windows"],
    deps = [
        "//java/com/google/gerrit/common:server",
        "//java/com/google/gerrit/common:version",
        "//java/com/google/gerrit/launcher",
        "//lib:guava",
        "//lib/auto:auto-value",
        "//lib/auto:auto-value-annotations",
        "//lib/log:log4j",
        "//lib/truth",
    ],
)
