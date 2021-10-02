import sbt.Keys._
import sbt._

object SlickGenerator extends AutoPlugin {
  override def trigger = noTrigger

  override def projectSettings: Seq[Def.Setting[_]] = {
    Seq(
      Compile / sourceGenerators += Def.task {
        val dir       = (Compile / sourceManaged).value
        val outputDir = dir
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
      },
    )
  }
}
