val zioVersion = "1.0.9"

lazy val frontend = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    inThisBuild(
      List(
        name := "frontend",
        organization := "com.example",
        version := "0.0.1",
        scalaVersion := "3.0.1",
      )
    ),
    libraryDependencies ++= Seq(
      "com.raquo"   %%% "laminar" % "0.13.1",
      "io.laminext" %%% "fetch"   % "0.13.9"
    ),
    scalaJSUseMainModuleInitializer := true
  )
