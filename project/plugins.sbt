addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.1")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.22.0")
addSbtPlugin("com.github.scalaprops" % "sbt-scalaprops" % "0.5.3")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
)
