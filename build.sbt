lazy val root = project.in(file(".")).aggregate(
  scalajspackJS,
  scalajspackJVM
).settings(
  autoScalaLibrary := false,
  publish := {},
  publishLocal := {}
)

val circeVersion = "0.14.15"

val unusedWarnings = Seq(
  "-Wunused:imports",
)

lazy val scalajspack = crossProject(JSPlatform, JVMPlatform).in(file(".")).settings(
  name := "scalajspack",
  scalaVersion := "2.13.18",
  licenses := Seq("MIT License" -> url("https://opensource.org/licenses/mit-license")),
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
  ),
  scalacOptions ++= unusedWarnings,
  libraryDependencies ++= Seq(
    "com.github.scalaprops" %%% "scalaprops" % "0.11.0" % "test",
    "com.github.xuwei-k" %%% "scodec-msgpack" % "0.8.0",
    "io.circe" %%% "circe-parser" % circeVersion,
  ),
  Seq(Compile, Test).flatMap(c =>
    (c / console / scalacOptions) --= unusedWarnings
  ),
  scalapropsCoreSettings
).jsSettings(
  scalacOptions += {
    val a = (LocalRootProject / baseDirectory).value.toURI.toString
    val g = "https://raw.githubusercontent.com/xuwei-k/scalajspack/" + sys.process.Process("git rev-parse HEAD").lineStream_!.head
    s"-P:scalajs:mapSourceURI:$a->$g/"
  }
)

lazy val scalajspackJVM = scalajspack.jvm
lazy val scalajspackJS = scalajspack.js
