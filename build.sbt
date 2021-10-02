val zioVersion   = "1.0.12"
val slickVersion = "3.3.3"

lazy val backend = project
  .in(file("./backend"))
  .dependsOn(common.jvm)
  .settings(
    inThisBuild(
      List(
        name := "backend",
        organization := "com.example",
        version := "0.0.1",
        scalaVersion := "2.13.6",
      ),
    ),
    resolvers +=
      "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick"             % slickVersion,
      "com.typesafe.slick" %% "slick-codegen"     % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp"    % slickVersion,
      "org.postgresql"      % "postgresql"        % "42.2.24",
      "org.slf4j"           % "slf4j-nop"         % "1.7.32",
      "io.d11"             %% "zhttp"             % "1.0.0.0-RC17+47-0ea2e2b7-SNAPSHOT",
      "dev.zio"            %% "zio-config"        % "1.0.10",
      "dev.zio"            %% "zio-json"          % "0.2.0-M1",
      "dev.zio"            %% "zio"               % zioVersion,
      "dev.zio"            %% "zio-logging"       % "0.5.12",
      "dev.zio"            %% "zio-test"          % zioVersion % Test,
      "dev.zio"            %% "zio-test-sbt"      % zioVersion % Test,
      "dev.zio"            %% "zio-test-junit"    % zioVersion % Test,
      "dev.zio"            %% "zio-test-magnolia" % zioVersion % Test,
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  )
  .enablePlugins(SlickGenerator)

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
        scalaVersion := "2.13.6",
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
        scalaVersion := "2.13.6",
      ),
    ),
  )

lazy val fastOptCompileCopy = taskKey[Unit]("")

val jsPath = "frontend/resources"

fastOptCompileCopy := {
  val source    = (frontend / Compile / fastOptJS).value.data
  val sourceMap = source.getParentFile / (source.getName + ".map")

  IO.copyFile(
    source,
    baseDirectory.value / jsPath / "dev.js",
  )
  IO.copyFile(
    sourceMap,
    baseDirectory.value / jsPath / "dev.js.map",
  )
}
