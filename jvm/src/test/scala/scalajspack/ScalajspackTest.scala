package scalajspack

import io.circe.{Json, JsonObject}
import scodec.Attempt
import scodec.msgpack.Serialize
import scalaprops.{Scalaprops, Property, Gen}

object ScalajspackTest extends Scalaprops {

  private[this] implicit val scalaDoubleGen: Gen[Double] =
    Gen[Long].map { n =>
      java.lang.Double.longBitsToDouble(n) match {
        case x if x.isNaN => n
        case x => x
      }
    }

  private[this] implicit val bigDecimalGen: Gen[BigDecimal] =
    Gen[Double].map(BigDecimal(_))

  private[this] implicit val stringGen = Gen.alphaNumString

  private[this] val jsonNumberGen: Gen[Json] =
    Gen.oneOf(
      bigDecimalGen.map(Json.bigDecimal),
      Gen[Long].map(Json.long),
      Gen[Double].map(Json.numberOrNull)
    )

  private[this] val jsValuePrimitivesArb: Gen[Json] =
    Gen.oneOf(
      Gen.value(Json.Empty),
      Gen.value(Json.True),
      Gen.value(Json.False),
      jsonNumberGen,
      Gen[String].map(Json.string)
    )

  private[this] val jsObjectArb1: Gen[JsonObject] =
    Gen.listOfN(
      5,
      Gen.tuple2(
        Gen[String], jsValuePrimitivesArb
      )
    ).map(list => JsonObject.fromIndexedSeq(list.toVector))

  private[this] val jsArrayArb1: Gen[List[Json]] =
    Gen.listOfN(5, jsValuePrimitivesArb)

  implicit val jsValueArb: Gen[Json] =
    Gen.oneOf(
      jsValuePrimitivesArb,
      jsObjectArb1.map(Json.fromJsonObject),
      jsArrayArb1.map(Json.array)
    )

  implicit val jsObjectArb: Gen[JsonObject] =
    Gen.listOfN(
      5,
      Gen.tuple2(Gen[String], jsValueArb)
    ).map(list => JsonObject.fromIndexedSeq(list.toVector))

  implicit val jsArrayArb: Gen[List[Json]] =
    Gen.listOfN(5, jsValueArb)

  val test = Property.forAll{ json: Json =>
    val S = Scalajspack.serialize(UnpackOptions.default)
    val actual = S.pack(json).flatMap(S.unpack)
    val r = actual == Attempt.successful(json)
    assert(r, s"$actual $json")
    r
  }

  override val param = super.param.copy(minSuccessful = 10000)
}
