val zioVersion   = "2.0.0-RC6"
val zhttpVersion = "2.0.0-RC9"
val zioConfig    = "3.0.0-RC9"

githubTokenSource := TokenSource.GitConfig("github.token")

lazy val backend = project
  .in(file("./backend"))
  .dependsOn(common.jvm)
  .settings(
    resolvers += Resolver.githubPackages("shevv920", "kuzminki-zio-2"),
    ThisBuild / scalaVersion := "2.13.8",
    githubTokenSource        := TokenSource.GitConfig("github.token"),
    libraryDependencies ++= Seq(
      "org.postgresql"        % "postgresql"          % "42.3.6",
      "shevv920"             %% "kuzminki-zio-2-fork" % "0.9.2",
      "org.slf4j"             % "slf4j-nop"           % "1.7.36",
      "com.github.jwt-scala" %% "jwt-core"            % "9.0.5",
      "io.github.nremond"     % "pbkdf2-scala_2.13"   % "0.6.5",
      "io.d11"               %% "zhttp"               % zhttpVersion,
      "dev.zio"              %% "zio-config"          % zioConfig,
      "dev.zio"              %% "zio-config-magnolia" % zioConfig,
      "dev.zio"              %% "zio-prelude"         % "1.0.0-RC14",
      "dev.zio"              %% "zio"                 % zioVersion,
      "dev.zio"              %% "zio-test"            % zioVersion   % Test,
      "dev.zio"              %% "zio-test-sbt"        % zioVersion   % Test,
      "dev.zio"              %% "zio-test-junit"      % zioVersion   % Test,
      "dev.zio"              %% "zio-test-magnolia"   % zioVersion   % Test,
      "io.d11"               %% "zhttp-test"          % zhttpVersion % Test,
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  )
  .enablePlugins(PackPlugin)

lazy val frontend = project
  .in(file("./frontend"))
  .dependsOn(common.js)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    githubTokenSource := TokenSource.GitConfig("github.token"),
    libraryDependencies ++= Seq(
      "com.raquo"                    %%% "laminar"  % "0.14.2",
      "com.raquo"                    %%% "waypoint" % "0.5.0",
      "com.lihaoyi"                  %%% "upickle"  % "1.4.2",
      "dev.zio"                      %%% "zio-json" % "0.3.0-RC7",
      "io.laminext"                  %%% "fetch"    % "0.14.3",
      "com.github.japgolly.scalacss" %%% "core"     % "1.0.0",
    ),
    scalaJSUseMainModuleInitializer := true,
  )

lazy val common = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("./common"))
  .settings(
    githubTokenSource                := TokenSource.GitConfig("github.token"),
    libraryDependencies += "dev.zio" %% "zio-json" % "0.3.0-RC7",
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
    source.getParentFile / "with-html" / s"script-dev-$hash.js.map",
  )
}
