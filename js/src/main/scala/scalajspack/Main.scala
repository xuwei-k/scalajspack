package scalajspack

import scala.scalajs.js.annotation._

@JSExportTopLevel("ScalajspackMain")
object Main{
  @JSExport
  def convert(json: String): String = {
    new Scalajspack().jsonString2msgpackBytesString(json).require
  }
}
