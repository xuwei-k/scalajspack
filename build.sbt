lazy val root = project.in(file(".")).aggregate(
  scalajspackJS,
  scalajspackJVM
).settings(
  publish := {},
  publishLocal := {}
)

val circeVersion = "0.8.0"

val unusedWarnings = (
  "-Ywarn-unused" ::
  "-Ywarn-unused-import" ::
  Nil
)

lazy val scalajspack = crossProject.in(file(".")).settings(
  name := "scalajspack",
  scalaVersion := "2.12.2",
  fullResolvers ~= {_.filterNot(_.name == "jcenter")},
  licenses := Seq("MIT License" -> url("http://opensource.org/licenses/mit-license")),
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
    ("com.github.scalaprops" %%% "scalaprops" % "0.5.0" % "test") ::
    ("com.github.pocketberserker" %%% "scodec-msgpack" % "0.6.0") ::
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
    val g = "https://raw.githubusercontent.com/xuwei-k/scalajspack/" + sys.process.Process("git rev-parse HEAD").lines_!.head
    s"-P:scalajs:mapSourceURI:$a->$g/"
  }
)

lazy val scalajspackJVM = scalajspack.jvm
lazy val scalajspackJS = scalajspack.js
