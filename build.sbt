val zioVersion = "1.0.9"

lazy val backend = project
  .in(file("./backend"))
  .dependsOn(common.jvm)
  .settings(
    inThisBuild(
      List(
        name := "backend",
        organization := "com.example",
        version := "0.0.1",
        scalaVersion := "3.0.1",
      ),
    ),
    libraryDependencies ++= Seq(
      "io.d11"                        %% "zhttp"                  % "1.0.0.0-RC17",
      "com.softwaremill.sttp.client3" %% "core"                   % "3.3.13",
      "com.softwaremill.sttp.client3" %% "httpclient-backend-zio" % "3.3.13",
      "dev.zio"                       %% "zio"                    % zioVersion,
      "dev.zio"                       %% "zio-test"               % zioVersion % Test,
      "dev.zio"                       %% "zio-test-sbt"           % zioVersion % Test,
      "dev.zio"                       %% "zio-test-junit"         % zioVersion % Test,
      "dev.zio"                       %% "zio-test-magnolia"      % zioVersion % Test,
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  )

lazy val frontend = project
  .in(file("./frontend"))
  .dependsOn(common.js)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    inThisBuild(
      List(
        name := "frontend",
        organization := "com.example",
        version := "0.0.1",
        scalaVersion := "3.0.1",
      ),
    ),
    libraryDependencies ++= Seq(
      "com.raquo"   %%% "laminar" % "0.13.1",
      "io.laminext" %%% "fetch"   % "0.13.9",
    ),
    scalaJSUseMainModuleInitializer := true,
  )

lazy val common = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("./common"))
  .settings(
    inThisBuild(
      List(
        name := "common",
        scalaVersion := "3.0.1",
      ),
    ),
  )

lazy val fastOptCompileCopy = taskKey[Unit]("")

val jsPath = "frontend/resources"

fastOptCompileCopy := {
  val source = (frontend / Compile / fastOptJS).value.data
  IO.copyFile(
    source,
    baseDirectory.value / jsPath / "dev.js",
  )
}
