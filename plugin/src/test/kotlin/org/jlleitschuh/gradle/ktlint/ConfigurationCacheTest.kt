package org.jlleitschuh.gradle.ktlint

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask
import org.jlleitschuh.gradle.ktlint.testdsl.CommonTest
import org.jlleitschuh.gradle.ktlint.testdsl.GradleTestVersions
import org.jlleitschuh.gradle.ktlint.testdsl.build
import org.jlleitschuh.gradle.ktlint.testdsl.project
import org.junit.jupiter.api.DisplayName

@GradleTestVersions
class ConfigurationCacheTest : AbstractPluginTest() {
    private val configurationCacheFlag = "--configuration-cache"

    @DisplayName("Should support configuration cache without errors on running linting")
    @CommonTest
    internal fun configurationCacheForCheckTask(gradleVersion: GradleVersion) {
        project(gradleVersion) {
            createSourceFile(
                "src/main/kotlin/CleanSource.kt",
                """
                val foo = "bar"

                """.trimIndent()
            )

            build(
                configurationCacheFlag,
                CHECK_PARENT_TASK_NAME
            ) {
                assertThat(task(":$mainSourceSetCheckTaskName")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
            }

            build(
                configurationCacheFlag,
                CHECK_PARENT_TASK_NAME
            ) {
                assertThat(task(":$mainSourceSetCheckTaskName")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
                assertThat(output).contains("Reusing configuration cache.")
            }
        }
    }

    @DisplayName("Should support configuration cache on running format tasks")
    @CommonTest
    fun configurationCacheForFormatTasks(gradleVersion: GradleVersion) {
        project(gradleVersion) {
            val sourceFile = "\nval foo = \"bar\"\n"
            createSourceFile(
                "src/main/kotlin/CleanSource.kt",
                sourceFile
            )
            val formatTaskName = KtLintFormatTask.buildTaskNameForSourceSet("main")
            build(
                configurationCacheFlag,
                FORMAT_PARENT_TASK_NAME
            ) {
                assertThat(task(":$formatTaskName")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
                assertThat(task(":$mainSourceSetFormatTaskName")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
            }
            build(
                configurationCacheFlag,
                FORMAT_PARENT_TASK_NAME,
                "--debug"
            ) {
                assertThat(task(":$formatTaskName")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
                assertThat(task(":$mainSourceSetFormatTaskName")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
                assertThat(output).contains("Reusing configuration cache.")
            }
        }
    }

    @DisplayName("Should support configuration cache on running format tasks with relative paths")
    @CommonTest
    fun configurationCacheForFormatTasksWithRelativePaths(gradleVersion: GradleVersion) {
        project(gradleVersion) {
            buildGradle.appendText(
                //language=Groovy
                """
                repositories {
                    jcenter()
                }

                ktlint {
                    relative = true
                    reporters {
                        reporter "plain"
                        reporter "checkstyle"
                    }
                }
                """.trimIndent()
            )
            val sourceFile = "\nval foo = \"bar\"\n"
            createSourceFile(
                "src/main/kotlin/CleanSource.kt",
                sourceFile
            )
            val formatTaskName = KtLintFormatTask.buildTaskNameForSourceSet("main")
            build(
                configurationCacheFlag,
                FORMAT_PARENT_TASK_NAME
            ) {
                assertThat(task(":$formatTaskName")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
                assertThat(task(":$mainSourceSetFormatTaskName")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
            }
            build(
                configurationCacheFlag,
                FORMAT_PARENT_TASK_NAME
            ) {
                assertThat(task(":$formatTaskName")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
                assertThat(task(":$mainSourceSetFormatTaskName")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
                assertThat(output).contains("Reusing configuration cache.")
            }
        }
    }

    @DisplayName("Should support configuration cache for git hook format install task")
    @CommonTest
    internal fun configurationCacheForGitHookFormatInstallTask(gradleVersion: GradleVersion) {
        project(gradleVersion) {
            projectPath.initGit()

            build(
                configurationCacheFlag,
                INSTALL_GIT_HOOK_FORMAT_TASK
            ) {
                assertThat(task(":$INSTALL_GIT_HOOK_FORMAT_TASK")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
            }

            build(
                configurationCacheFlag,
                INSTALL_GIT_HOOK_FORMAT_TASK
            ) {
                assertThat(task(":$INSTALL_GIT_HOOK_FORMAT_TASK")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
                assertThat(output).contains("Reusing configuration cache.")
            }
        }
    }

    @DisplayName("Should support configuration cache for git hook check install task")
    @CommonTest
    internal fun configurationCacheForGitHookCheckInstallTask(gradleVersion: GradleVersion) {
        project(gradleVersion) {
            projectPath.initGit()

            build(
                configurationCacheFlag,
                INSTALL_GIT_HOOK_CHECK_TASK
            ) {
                assertThat(task(":$INSTALL_GIT_HOOK_CHECK_TASK")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
            }

            build(
                configurationCacheFlag,
                INSTALL_GIT_HOOK_CHECK_TASK
            ) {
                assertThat(task(":$INSTALL_GIT_HOOK_CHECK_TASK")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
                assertThat(output).contains("Reusing configuration cache.")
            }
        }
    }
}
