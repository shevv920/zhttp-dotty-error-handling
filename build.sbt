val zioVersion   = "1.0.12"
val slickVersion = "3.3.3"

lazy val slick = taskKey[Seq[File]]("Generate Tables.scala")
slick := {
  val dir       = (Compile / sourceManaged).value
  val outputDir = dir / "slick"
  val url =
    "jdbc:postgresql://postgres/test?user=postgres&password=dbpassword" // connection info
  val jdbcDriver  = "org.postgresql.Driver"
  val slickDriver = "slick.jdbc.PostgresProfile"
  val pkg         = "db"

  val cp = (Compile / dependencyClasspath).value
  val s  = streams.value

  runner.value
    .run(
      "slick.codegen.SourceCodeGenerator",
      cp.files,
      Array(slickDriver, jdbcDriver, url, outputDir.getPath, pkg),
      s.log,
    )
    .failed foreach (sys error _.getMessage)

  val file = outputDir / pkg / "Tables.scala"

  Seq(file)
}

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
      "dev.zio"            %% "zio-test"          % zioVersion % Test,
      "dev.zio"            %% "zio-test-sbt"      % zioVersion % Test,
      "dev.zio"            %% "zio-test-junit"    % zioVersion % Test,
      "dev.zio"            %% "zio-test-magnolia" % zioVersion % Test,
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  )

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick-codegen" % slickVersion,
  "com.typesafe.slick" %% "slick"         % slickVersion,
  "org.slf4j"           % "slf4j-nop"     % "1.7.32",
  "org.postgresql"      % "postgresql"    % "42.2.24",
)
backend / Compile / sourceGenerators += slick.taskValue

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
