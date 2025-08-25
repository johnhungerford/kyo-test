import org.scalajs.jsenv.nodejs.*

val scala3Version    = "3.7.2"

val kyoVersion = "1.0-RC1"
val zioVersion       = "2.1.17"
val scalaTestVersion = "3.2.19"
val munitVersion = "1.1.1"

ThisBuild / scalaVersion := scala3Version
publish / skip           := true

inThisBuild(List(
    organization := "io.github.johnhungerford",
    organizationName := "johnhungerford",
    organizationHomepage := Some(url("https://johnhungerford.github.io")),
    homepage := Some(url("https://johnhungerford.github.io")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    startYear := Some(2025),
    developers := List(
        Developer(
            id    = "johnhungerford",
            name  = "John Hungerford",
            email = "jiveshungerford@gmail.com",
            url   = url( "https://johnhungerford.github.io" )
        )
    ),
    scmInfo := Some(
        ScmInfo(
        url("https://github.com/johnhungerford/kyo-test"),
        "scm:git@github.com:johnhungerford/kyo-test.git"
        )
    ),
    resolvers += Resolver.sonatypeCentralSnapshots,
    resolvers += Resolver.sonatypeCentralRepo("staging")
))

lazy val `kyo-settings` = Seq(
    fork               := true,
    scalaVersion       := scala3Version,
    scalafmtOnCompile := true,
    scalacOptions ++= Seq(
        "-Wvalue-discard", 
        "-Wnonunit-statement", 
        "-Wconf:msg=(unused.*value|discarded.*value|pure.*statement):error",
        // "-language:strictEquality"
    ),
    // ThisBuild / versionScheme               := Some("early-semver"),
    Test / javaOptions += "--add-opens=java.base/java.lang=ALL-UNNAMED",
    libraryDependencies += "io.getkyo" %%% "kyo-core" % kyoVersion
)

Global / onLoad := {
    val javaVersion  = System.getProperty("java.version")
    val majorVersion = javaVersion.split("\\.")(0).toInt
    if (majorVersion < 21) {
        throw new IllegalStateException(
            s"Java version $javaVersion is not supported. Please use Java 21 or higher."
        )
    }

    (Global / onLoad).value
}

lazy val root = project
    .in(file("."))
    .aggregate(
        `kyo-test`.jvm,
        `kyo-test-scalatest`.jvm,
        `kyo-test-zio`.jvm,
        `kyo-test-munit`.jvm,
        `kyo-test-utest`.jvm,
    )

lazy val JS = project
    .in(file("js"))
    .aggregate(
        `kyo-test`.js,
        `kyo-test-scalatest`.js,
        `kyo-test-zio`.js,
        `kyo-test-munit`.js,
        `kyo-test-utest`.js,
    )

lazy val `kyo-test` =
    crossProject(JVMPlatform, JSPlatform)
        .withoutSuffixFor(JVMPlatform)
        .crossType(CrossType.Full)
        .in(file("kyo-test"))
        .settings(
            `kyo-settings`,
        )
        .jsSettings(
            `js-settings`
        )

lazy val `kyo-test-scalatest` =
    crossProject(JVMPlatform, JSPlatform)
        .withoutSuffixFor(JVMPlatform)
        .crossType(CrossType.Full)
        .in(file("kyo-test-scalatest"))
        .dependsOn(`kyo-test`)
        .settings(
            `kyo-settings`,
            libraryDependencies += "org.scalatest" %%% "scalatest" % scalaTestVersion,
        )
        .jsSettings(
            `js-settings`
        )
        // .jvmSettings(
        //     libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.1.0"
        // )

lazy val `kyo-test-zio` =
    crossProject(JVMPlatform, JSPlatform)
        .withoutSuffixFor(JVMPlatform)
        .crossType(CrossType.Full)
        .in(file("kyo-test-zio"))
        .dependsOn(`kyo-test`)
        .settings(
            `kyo-settings`,
            libraryDependencies += "dev.zio" %%% "zio"          % zioVersion,
            libraryDependencies += "dev.zio" %%% "zio-test"     % zioVersion,
            libraryDependencies += "dev.zio" %%% "zio-test-sbt" % zioVersion % Test,
            libraryDependencies += "io.getkyo" %%% "kyo-zio" % kyoVersion,
        )
        .jsSettings(
            `js-settings`
        )

lazy val `kyo-test-munit` =
    crossProject(JVMPlatform, JSPlatform)
        .withoutSuffixFor(JVMPlatform)
        .crossType(CrossType.Full)
        .in(file("kyo-test-munit"))
        .dependsOn(`kyo-test`)
        .settings(
            `kyo-settings`,
            libraryDependencies += "org.scalameta" %%% "munit" % munitVersion,
        )
        .jsSettings(
            `js-settings`
        )

lazy val `kyo-test-utest` =
    crossProject(JVMPlatform, JSPlatform)
        .withoutSuffixFor(JVMPlatform)
        .crossType(CrossType.Full)
        .in(file("kyo-test-utest"))
        .dependsOn(`kyo-test`)
        .settings(
            `kyo-settings`,
            libraryDependencies += "com.lihaoyi" %%% "utest" % "0.9.1",
            testFrameworks += new TestFramework("utest.runner.Framework"),
        )
        .jsSettings(
            `js-settings`
        )

lazy val `native-settings` = Seq(
    fork                                        := false,
    bspEnabled                                  := false,
    Test / testForkedParallel                   := false,
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.6.0"
)

lazy val `js-settings` = Seq(
    fork                                        := false,
    Test / fork                                 := false,
    Test / parallelExecution                    := false,
    bspEnabled                                  := false,
    jsEnv                                       := new NodeJSEnv(NodeJSEnv.Config().withArgs(List("--max_old_space_size=5120"))),
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.6.0"
)
