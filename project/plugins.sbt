addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.7")
addSbtPlugin("com.github.scalaprops" % "sbt-scalaprops" % "0.1.0")

scalacOptions ++= (
  "-deprecation" ::
  "-unchecked" ::
  "-Xlint" ::
  "-language:existentials" ::
  "-language:higherKinds" ::
  "-language:implicitConversions" ::
  "-Yno-adapted-args" ::
  Nil
)

fullResolvers ~= {_.filterNot(_.name == "jcenter")}
