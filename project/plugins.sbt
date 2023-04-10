addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.13.1")
addSbtPlugin("com.github.scalaprops" % "sbt-scalaprops" % "0.4.4")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.0")

scalacOptions ++= (
  "-deprecation" ::
  "-unchecked" ::
  "-language:existentials" ::
  "-language:higherKinds" ::
  "-language:implicitConversions" ::
  "-Yno-adapted-args" ::
  Nil
)

fullResolvers ~= {_.filterNot(_.name == "jcenter")}
