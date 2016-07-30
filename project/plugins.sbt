addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.11")
addSbtPlugin("com.github.scalaprops" % "sbt-scalaprops" % "0.1.1")

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
