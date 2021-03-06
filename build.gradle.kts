@file:Suppress("InvalidPackageDeclaration")

import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin as AndroidApplicationPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.CompileOptions
import com.android.build.gradle.internal.dsl.DefaultConfig
import com.android.build.gradle.internal.dsl.LintOptions
import com.android.build.gradle.internal.dsl.TestOptions
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin as AndroidLibraryPlugin

import com.github.benmanes.gradle.versions.VersionsPlugin
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

import de.mannodermaus.gradle.plugins.junit5.AndroidJUnitPlatformPlugin
import de.mannodermaus.gradle.plugins.junit5.junitPlatform

import io.gitlab.arturbosch.detekt.detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

import org.gradle.api.Project

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin as KotlinKaptPlugin
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper as KotlinAndroidPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper as KotlinPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

import java.io.FileInputStream
import java.util.*

/* BUILD SCRIPT */

buildscript {
    repositories {
        google()
        jcenter()
        maven { url = uri(Reps.Url.GRADLE) }
    }
    dependencies {
        classpath(Deps.Plugin.ANDROID)
        classpath(Deps.Plugin.KOTLIN)
        classpath(Deps.Plugin.DETEKT)
        classpath(Deps.Plugin.ANDROID_J_UNIT_5)
        classpath(Deps.Plugin.VERSIONS)
    }
}

/* PROJECTS CONFIGURATION */

allprojects {
    repositories {
        google()
        jcenter()
        maven { url = uri(Reps.Url.GRADLE) }
    }
}

/* PROJECT PLUGINS CONFIGURATION */

subprojects {
    subprojectsPlugins()
    subprojectsTasks()
}

fun Project.subprojectsPlugins() {
    plugins.withType(KotlinPlugin::class) {
        logPlugin(PluginIds.Kotlin.KOTLIN)
        java { java() }
        sourceSets()
        testOptions()
    }
    plugins.withType(KotlinKaptPlugin::class) {
        logPlugin(PluginIds.Kotlin.KAPT)
        kapt { kapt() }
    }
    plugins.withType(KotlinAndroidPlugin::class) {
        logPlugin(PluginIds.Kotlin.Android.ANDROID)
    }
    plugins.withType(AndroidLibraryPlugin::class) {
        logPlugin(PluginIds.Android.LIBRARY)
        androidLibrary { androidLibrary() }
    }
    plugins.withType(AndroidApplicationPlugin::class) {
        logPlugin(PluginIds.Android.APPLICATION)
        androidApplication { androidApplication() }
    }
    plugins.withType(DetektPlugin::class) {
        logPlugin(PluginIds.Quality.DETEKT)
        detekt { detekt() }
    }
    plugins.withType(AndroidJUnitPlatformPlugin::class) {
        logPlugin(PluginIds.Test.Android.J_UNIT_5)
        androidBase { androidBase() }
    }
    plugins.withType(VersionsPlugin::class) {
        logPlugin(PluginIds.Dependency.VERSIONS)
    }
}

fun Project.subprojectsTasks() {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions { kotlinOptions() }
    }
    tasks.withType<DependencyUpdatesTask> {
        versionsOptions()
    }
    tasks.withType<Zip> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

/* *********************************************************************************************************************** */

/* PLUGINS EXTENSION FUNCTIONS */

fun logPlugin(pluginId: String) {
    println("<<< CONFIGURE WITH $pluginId PLUGIN >>>")
}

fun Project.java(configure: JavaPluginExtension.() -> Unit) =
    extensions.configure(JavaPluginExtension::class.java, configure)

fun Project.sourceSets() {
    configure<SourceSetContainer> {
        mainSourceSetContainer()
        testSourceSetContainer()
    }
}

fun Project.testOptions() {
    tasks.getByName<Test>(Tasks.TEST) {
        testLogging()
        testJUnit5()
    }
}

fun Project.kapt(configure: KaptExtension.() -> Unit) =
    extensions.configure(KaptExtension::class.java, configure)

fun Project.androidBase(configure: BaseExtension.() -> Unit) =
    extensions.configure(BaseExtension::class.java, configure)

fun Project.androidLibrary(configure: LibraryExtension.() -> Unit) =
    extensions.configure(LibraryExtension::class.java, configure)

fun Project.androidApplication(configure: AppExtension.() -> Unit) =
    extensions.configure(AppExtension::class.java, configure)

/* CONFIGURATION EXTENSION FUNCTIONS */

fun JavaPluginExtension.java() {
    sourceCompatibility = Java.version
    targetCompatibility = Java.version
}

@Suppress("SpreadOperator")
fun Test.testLogging() {
    testLogging {
        events(*Logs.events)
        setExceptionFormat(Logs.EXCEPTION_FORMAT)
        debug {
            events(*Logs.debugEvents)
            setExceptionFormat(Logs.DEBUG_EXCEPTION_FORMAT)
        }
        showExceptions = true
        showCauses = true
        showStackTraces = true
        info.events = debug.events
        info.exceptionFormat = debug.exceptionFormat
        addTestListener(object : TestListener {
            override fun beforeSuite(desc: TestDescriptor) = Unit
            override fun beforeTest(desc: TestDescriptor) = Unit
            override fun afterTest(desc: TestDescriptor, result: TestResult) = Unit
            override fun afterSuite(desc: TestDescriptor, result: TestResult) {
                if (desc.parent != null) println(Logs.testConsoleOutput(result))
            }
        })
    }
}

fun Test.testJUnit5() {
    useJUnitPlatform { includeEngines(Tests.Engine.JUnit.VINTAGE, Tests.Engine.Spek.SPEK) }
}

fun KaptExtension.kapt() {
    useBuildCache = true
}

fun BaseExtension.androidBase() {
    testOptions { testOptionsJUnit5() }
}

fun LibraryExtension.androidLibrary() {
    compileSdkVersion(Android.Sdk.COMPILE)
    testOptions.unitTests.isIncludeAndroidResources = true
    defaultConfig { defaultConfig() }
    compileOptions { compileOptions() }
    sourceSets { sourceSets() }
    testOptions { testOptions() }
    lintOptions { lintOptions() }
    buildFeatures()
    variantOptions()
}

fun AppExtension.androidApplication() {
    compileSdkVersion(Android.Sdk.COMPILE)
    testOptions.unitTests.isIncludeAndroidResources = true
    defaultConfig { defaultConfig() }
    compileOptions { compileOptions() }
    sourceSets { sourceSets() }
    testOptions { testOptions() }
    lintOptions { lintOptions() }
    buildFeatures()
    variantOptions()
}

fun DetektExtension.detekt() {
    toolVersion = Versions.Plugin.DETEKT
    parallel = false
    config = files(Config.Detekt.CONFIG_FILE_PATH)
    buildUponDefaultConfig = false
    disableDefaultRuleSets = false
    debug = false
    ignoreFailures = false
    reports {
        html.enabled = true
        xml.enabled = true
        txt.enabled = true
    }
}

/* CONFIGURATION EXTENSION FUNCTIONS - ANDROID */

fun DefaultConfig.defaultConfig() {
    minSdkVersion(Android.Sdk.MIN)
    targetSdkVersion(Android.Sdk.TARGET)
}

fun CompileOptions.compileOptions() {
    sourceCompatibility = Java.version
    targetCompatibility = Java.version
}

fun NamedDomainObjectContainer<AndroidSourceSet>.sourceSets() {
    androidMainSourceSetContainer()
    androidTestSourceSetContainer()
    androidAndroidTestSourceSetContainer()
}

fun TestOptions.testOptions() {
    unitTests.all { test: Test -> test.testLogging() }
}

fun TestOptions.testOptionsJUnit5() {
    junitPlatform { filters { includeEngines(Tests.Engine.JUnit.VINTAGE, Tests.Engine.Spek.SPEK) } }
}

@Suppress("SpreadOperator")
fun LintOptions.lintOptions() {
    isAbortOnError = true
    isCheckAllWarnings = true
    isIgnoreWarnings = false
    isCheckReleaseBuilds = true
    isWarningsAsErrors = true
    lintConfig = file(Config.Lint.CONFIG_FILE_PATH)
    htmlReport = true
    xmlReport = true
    disable(*Config.Lint.disabledIssues)
}

fun BaseExtension.buildFeatures() {
    buildFeatures.viewBinding = true
}

fun LibraryExtension.variantOptions() {
    ignoredVariants {
        onVariants.withName(it) {
            enabled = false
            androidTest {
                enabled = false
            }
            unitTest {
                enabled = false
            }
        }
    }
}

fun AppExtension.variantOptions() {
    ignoredVariants {
        variantFilter {
            if (name == it) ignore = true
        }
    }
}

fun ignoredVariants(variantOptions: (String) -> Unit) {
    val localProperties = Properties()
    localProperties.load(FileInputStream(file(Files.Properties.LOCAL)))
    val ignoredVariants = localProperties[Properties.Local.Property.IGNORED_VARIANTS]?.toString()?.split(Utils.COMMA)
    ignoredVariants?.forEach {
        println(" << CONFIGURE WITHOUT $it VARIANT >>")
        variantOptions(it)
    }
}

/* *********************************************************************************************************************** */

/* TASKS EXTENSION FUNCTIONS */

/* CONFIGURATION EXTENSION FUNCTIONS - KOTLIN */

fun KotlinJvmOptions.kotlinOptions() {
    jvmTarget = Java.version.toString()
    allWarningsAsErrors = true
}

/* CONFIGURATION EXTENSION FUNCTIONS - VERSIONS */

fun DependencyUpdatesTask.versionsOptions() {
    rejectVersionIf { isNonStable(candidate.version) }
    gradleReleaseChannel = Config.Versions.GRADLE_RELEASE_CHANNEL
    checkConstraints = false
    checkForGradleUpdate = true
    outputFormatter = Config.Versions.OUTPUT_FORMATTER
    outputDir = Config.Versions.OUTPUT_DIR
    reportfileName = Config.Versions.REPORT_FILE_NAME
}

fun isNonStable(version: String): Boolean {
    val regex = Config.Versions.REGEX.toRegex()
    val stableKeyword = Config.Versions.stableKeyword.any { version.toUpperCase().contains(it) }
    val nonStableKeyword = Config.Versions.nonStableKeyword.any { version.toUpperCase().contains(it) }
    val isStable = !nonStableKeyword && (stableKeyword || regex.matches(version))
    return isStable.not()
}

/* *********************************************************************************************************************** */

/* MODULE SOURCE SETS */

fun SourceSetContainer.mainSourceSetContainer() {
    named(Sources.MAIN) {
        java.setSrcDirs(arrayListOf(Sources.Main.KOTLIN))
        resources.setSrcDirs(arrayListOf(Sources.Main.RESOURCES))
    }
}

fun NamedDomainObjectContainer<AndroidSourceSet>.androidMainSourceSetContainer() {
    named(Sources.MAIN) {
        java.setSrcDirs(arrayListOf(Sources.Main.KOTLIN))
        resources.setSrcDirs(arrayListOf(Sources.Main.RESOURCES))
    }
}

fun SourceSetContainer.testSourceSetContainer() {
    named(Sources.TEST) {
        java.setSrcDirs(arrayListOf(Sources.Test.KOTLIN, Sources.Spek.KOTLIN, Sources.Robolectric.KOTLIN))
        resources.setSrcDirs(arrayListOf(Sources.Test.RESOURCES, Sources.Spek.RESOURCES, Sources.Robolectric.RESOURCES))
    }
}

fun NamedDomainObjectContainer<AndroidSourceSet>.androidTestSourceSetContainer() {
    named(Sources.TEST) {
        java.setSrcDirs(arrayListOf(Sources.Test.KOTLIN, Sources.Spek.KOTLIN, Sources.Robolectric.KOTLIN))
        resources.setSrcDirs(arrayListOf(Sources.Test.RESOURCES, Sources.Spek.RESOURCES, Sources.Robolectric.RESOURCES))
    }
}

@Suppress("FunctionMaxLength")
fun NamedDomainObjectContainer<AndroidSourceSet>.androidAndroidTestSourceSetContainer() {
    named(Sources.ANDROID_TEST) {
        java.setSrcDirs(arrayListOf(Sources.Android.Test.KOTLIN))
        resources.setSrcDirs(arrayListOf(Sources.Android.Test.RESOURCES))
    }
}
