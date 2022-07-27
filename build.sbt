val zioVersion      = "2.0.0"
val zhttpVersion    = "2.0.0-RC10"
val zioConfig       = "3.0.1"
val zioJsonVersion  = "0.3.0-RC10"
val laminextVersion = "0.14.3"

ThisBuild / scalaVersion := "3.1.3"
ThisBuild / scalacOptions ++= Seq(
  "-feature",
  "-Xfatal-warnings",
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
)

lazy val backend = project
  .in(file("./backend"))
  .dependsOn(common.jvm)
  .settings(
    resolvers +=
      "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies ++= Seq(
      "org.postgresql"        % "postgresql"          % "42.3.6",
      "org.slf4j"             % "slf4j-nop"           % "1.7.36",
      "com.github.jwt-scala" %% "jwt-core"            % "9.0.6",
      "io.github.nremond"    %% "pbkdf2-scala"        % "0.7.0",
      "io.d11"               %% "zhttp"               % zhttpVersion,
      "io.getquill"          %% "quill-jdbc-zio"      % "4.1.0-V2",
      "dev.zio"              %% "zio-config"          % zioConfig,
      "dev.zio"              %% "zio-config-magnolia" % zioConfig,
      "dev.zio"              %% "zio-prelude"         % "1.0.0-RC15",
      "dev.zio"              %% "zio"                 % zioVersion,
      "dev.zio"              %% "zio-test"            % zioVersion % Test,
      "dev.zio"              %% "zio-test-sbt"        % zioVersion % Test,
      "dev.zio"              %% "zio-test-magnolia"   % zioVersion % Test,
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  )
  .enablePlugins(PackPlugin)

lazy val frontend = project
  .in(file("./frontend"))
  .dependsOn(common.js)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    ThisBuild / scalaVersion := "3.1.3",
    libraryDependencies ++= Seq(
      "com.raquo"                    %%% "laminar"         % "0.14.2",
      "com.raquo"                    %%% "waypoint"        % "0.5.0",
      "dev.zio"                      %%% "zio-json"        % zioJsonVersion,
      "io.laminext"                  %%% "fetch"           % laminextVersion,
      "io.laminext"                  %%% "validation-core" % laminextVersion,
      "io.laminext"                  %%% "ui"              % laminextVersion,
      "com.github.japgolly.scalacss" %%% "core"            % "1.0.0",
    ),
    scalaJSUseMainModuleInitializer := true,
  )

lazy val common = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("./common"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-json" % zioJsonVersion
    )
  )

lazy val fastOptCompileCopy = taskKey[Unit]("")

lazy val jsPath   = "frontend/resources"
lazy val htmlPath = "frontend/resources/index.html"

fastOptCompileCopy := {
  val source    = (frontend / Compile / fastOptJS).value.data
  val sourceMap = source.getParentFile / (source.getName + ".map")
  val hash      = Hash.toHex(Hash(source))
  val htmlFile  = baseDirectory.value / htmlPath
  val srcHtml   = IO.readBytes(htmlFile)
  val htmlWithScript =
    new String(srcHtml).replaceAll("script-.*\\.js", s"script-dev-$hash.js").getBytes
  IO.write(source.getParentFile / "with-html" / "index.html", htmlWithScript)

  IO.copyFile(
    source,
    source.getParentFile / "with-html" / s"script-dev-$hash.js",
  )
  IO.copyFile(
    sourceMap,
    source.getParentFile / "with-html" / s"frontend-fastopt.js.map",
  )
}
