package scalajspack

import scala.scalajs.js.annotation._

@JSExportTopLevel("scalajspack.Main")
object Main{
  @JSExport
  def convert(json: String): String = {
    new Scalajspack().jsonString2msgpackBytesString(json).require
  }
}
