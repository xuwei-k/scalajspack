import sbtcrossproject.crossProject

lazy val root = project.in(file(".")).aggregate(
  scalajspackJS,
  scalajspackJVM
).settings(
  publish := {},
  publishLocal := {}
)

val circeVersion = "0.13.0"

val unusedWarnings = (
  "-Ywarn-unused" ::
  Nil
)

lazy val scalajspack = crossProject(JSPlatform, JVMPlatform).in(file(".")).settings(
  name := "scalajspack",
  scalaVersion := "2.12.11",
  fullResolvers ~= {_.filterNot(_.name == "jcenter")},
  licenses := Seq("MIT License" -> url("https://opensource.org/licenses/mit-license")),
  scalacOptions ++= (
    "-deprecation" ::
    "-unchecked" ::
    "-Xlint" ::
    "-language:existentials" ::
    "-language:higherKinds" ::
    "-language:implicitConversions" ::
    "-Yno-adapted-args" ::
    Nil
  ) ::: unusedWarnings,
  libraryDependencies ++= (
    ("com.github.scalaprops" %%% "scalaprops" % "0.8.1" % "test") ::
    ("com.github.xuwei-k" %%% "scodec-msgpack" % "0.8.0") ::
    ("io.circe" %%% "circe-parser" % circeVersion) ::
    Nil
  ),
  Seq(Compile, Test).flatMap(c =>
    scalacOptions in (c, console) ~= {_.filterNot(unusedWarnings.toSet)}
  ),
  scalapropsCoreSettings
).jsSettings(
  scalacOptions += {
    val a = (baseDirectory in LocalRootProject).value.toURI.toString
    val g = "https://raw.githubusercontent.com/xuwei-k/scalajspack/" + sys.process.Process("git rev-parse HEAD").lineStream_!.head
    s"-P:scalajs:mapSourceURI:$a->$g/"
  }
)

lazy val scalajspackJVM = scalajspack.jvm
lazy val scalajspackJS = scalajspack.js
