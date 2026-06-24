lazy val scalajspackRoot = rootProject.autoAggregate.settings(
  autoScalaLibrary := false,
  publish := {},
  publishLocal := {},
)

val circeVersion = "0.14.16"

val unusedWarnings = Seq(
  "-Wunused:imports",
)

val scalaVersions = Seq("2.13.18")

lazy val scalajspack = projectMatrix
  .in(file("core"))
  .defaultAxes()
  .settings(
    name := "scalajspack",
    licenses := Seq("MIT License" -> url("https://opensource.org/licenses/mit-license")),
    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
    ),
    scalacOptions ++= unusedWarnings,
    libraryDependencies ++= Seq(
      "com.github.scalaprops" %% "scalaprops" % "0.11.0" % "test",
      "com.github.xuwei-k" %% "scodec-msgpack" % "0.8.0",
      "io.circe" %% "circe-parser" % circeVersion,
    ),
    Seq(Compile, Test).flatMap(c => (c / console / scalacOptions) --= unusedWarnings),
    scalapropsCoreSettings
  )
  .jvmPlatform(
    scalaVersions,
  )
  .jsPlatform(
    scalaVersions,
    Def.settings(
      TaskKey[Unit]("dist") := Def.uncached {
        val dir = file("dist")
        IO.delete(dir)
        IO.createDirectory(dir)
        val indexHtml = "index.html"
        IO.copyFile(file(indexHtml), dir / indexHtml)
        val v = (Compile / fullLinkJS).value
        val Seq(m) = v.data.publicModules.toSeq
        val src = (Compile / fullLinkJSOutput).value
        val f = src / m.jsFileName
        val srcMap = src / m.sourceMapName.getOrElse(sys.error("source map not found"))
        IO.copyFile(f, dir / m.jsFileName)
        IO.copyFile(srcMap, dir / srcMap.getName)
      },
      scalacOptions += {
        val a = (LocalRootProject / baseDirectory).value.toURI.toString
        val g = "https://raw.githubusercontent.com/xuwei-k/scalajspack/" + sys.process
          .Process("git rev-parse HEAD")
          .lazyLines_!
          .head
        s"-P:scalajs:mapSourceURI:$a->$g/"
      },
    ),
  )
