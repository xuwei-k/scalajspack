package scalajspack

import io.circe.{Json, JsonObject}
import scodec.Attempt
import scalaprops.{Scalaprops, Property, Gen}

object ScalajspackTest extends Scalaprops {

  private[this] implicit val scalaDoubleGen: Gen[Double] = {
    val minusZero = java.lang.Double.doubleToLongBits(-0.0)
    Gen[Long].map { n =>
      java.lang.Double.longBitsToDouble(n) match {
        case x if x.isNaN => n
        case _ if n == minusZero => 0.0
        case x => x
      }
    }
  }

  private[this] implicit val bigDecimalGen: Gen[BigDecimal] =
    Gen[Double].map(BigDecimal(_))

  private[this] implicit val stringGen = Gen.alphaNumString

  private[this] val jsonNumberGen: Gen[Json] =
    Gen.oneOf(
      bigDecimalGen.map(Json.fromBigDecimal),
      Gen[Long].map(Json.fromLong),
      Gen[Double].map(Json.fromDoubleOrNull)
    )

  private[this] val jsValuePrimitivesArb: Gen[Json] =
    Gen.oneOf(
      Gen.value(Json.Null),
      Gen.value(Json.True),
      Gen.value(Json.False),
      jsonNumberGen,
      Gen[String].map(Json.fromString)
    )

  private[this] val jsObjectArb1: Gen[JsonObject] =
    Gen.listOfN(
      5,
      Gen.tuple2(
        Gen[String], jsValuePrimitivesArb
      )
    ).map(list => JsonObject.fromIterable(list))

  private[this] val jsArrayArb1: Gen[List[Json]] =
    Gen.listOfN(5, jsValuePrimitivesArb)

  implicit val jsValueArb: Gen[Json] =
    Gen.oneOf(
      jsValuePrimitivesArb,
      jsObjectArb1.map(Json.fromJsonObject),
      jsArrayArb1.map(Json.arr)
    )

  implicit val jsObjectArb: Gen[JsonObject] =
    Gen.listOfN(
      5,
      Gen.tuple2(Gen[String], jsValueArb)
    ).map(list => JsonObject.fromIterable(list))

  implicit val jsArrayArb: Gen[List[Json]] =
    Gen.listOfN(5, jsValueArb)

  val test = Property.forAll{ json: Json =>
    val S = Scalajspack.serialize(UnpackOptions.default)
    val actual = S.pack(json).flatMap(S.unpack)
    val r = actual == Attempt.successful(json)
    assert(r, s"$actual $json")
    r
  }

  override val param = super.param.copy(minSuccessful = 1000)
}
