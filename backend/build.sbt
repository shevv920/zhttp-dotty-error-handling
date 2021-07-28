val zioVersion = "1.0.9"

lazy val backend = project
  .in(file("."))
  .settings(
    inThisBuild(
      List(
        name := "backend",
        organization := "com.example",
        version := "0.0.1",
        scalaVersion := "3.0.1",
      )
    ),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"               % zioVersion,
      "io.d11"  %% "zhttp"             % "1.0.0.0-RC17",
      "dev.zio" %% "zio-test"          % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt"      % zioVersion % Test,
      "dev.zio" %% "zio-test-junit"    % zioVersion % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
