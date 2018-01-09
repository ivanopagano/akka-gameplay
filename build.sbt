// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `akka-gameplay` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin, GitVersioning)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaCore,
        library.akkaTestkit % Test,
        library.scalaCheck  % Test,
        library.utest       % Test
      )
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val scalaCheck = "1.13.5"
      val utest      = "0.6.3"
      val akka       = "2.5.8"
    }
    val scalaCheck  = "org.scalacheck"    %% "scalacheck"   % Version.scalaCheck
    val utest       = "com.lihaoyi"       %% "utest"        % Version.utest
    val akkaCore    = "com.typesafe.akka" %% "akka-actor"   % Version.akka
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % Version.akka
  }



// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  gitSettings ++
  scalafmtSettings

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    // scalaVersion := "2.12.4",
    organization := "default",
    organizationName := "gameplay",
    startYear := Some(2018),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8"
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    testFrameworks += new TestFramework("utest.runner.Framework")
)

lazy val gitSettings =
  Seq(
    git.useGitDescribe := true
  )

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )
