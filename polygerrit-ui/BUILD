load("//tools/bzl:genrule2.bzl", "genrule2")
load("@build_bazel_rules_nodejs//:index.bzl", "nodejs_test")

package(default_visibility = ["//visibility:public"])

genrule2(
    name = "fonts",
    srcs = [
        "//lib/fonts:material-icons",
        "//lib/fonts:robotofonts",
    ],
    outs = ["fonts.zip"],
    cmd = " && ".join([
        "mkdir -p $$TMP/fonts",
        "cp $(SRCS) $$TMP/fonts/",
        "cd $$TMP",
        "find fonts/ -exec touch -t 198001010000 '{}' ';'",
        "zip -qr $$ROOT/$@ fonts",
    ]),
    output_to_bindir = 1,
)

filegroup(
    name = "web-test-runner_config-sources",
    srcs = glob([
        "package.json",
        "web-test-runner.config.mjs",
    ]),
)

nodejs_test(
    name = "web-test-runner",
    size = "large",
    chdir = package_name(),
    data = [
        ":web-test-runner_config-sources",
        "//polygerrit-ui/app:web-test-runner_app-sources",
        "@ui_dev_npm//:node_modules",
        "@ui_npm//:node_modules",
    ],
    entry_point = "@ui_dev_npm//:node_modules/@web/test-runner/dist/bin.js",
    tags = [
        "local",
        "manual",
    ],
)

# This is a dependency for karma_test rule in js.bzl that is only used by
# plugins.
sh_binary(
    name = "karma_bin",
    srcs = ["@ui_dev_npm//:node_modules/karma/bin/karma"],
    data = [
        "@ui_dev_npm//@open-wc/karma-esm",
        "@ui_dev_npm//karma-chrome-launcher",
        "@ui_dev_npm//karma-mocha",
        "@ui_dev_npm//karma-mocha-reporter",
        "@ui_dev_npm//karma/bin:karma",
        "@ui_dev_npm//mocha",
    ],
)

# This is used by plugins.
exports_files(["karma.conf.js"])
