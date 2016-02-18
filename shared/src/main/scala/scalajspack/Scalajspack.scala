package scalajspack

import scodec.{Attempt, Err}
import io.circe.Json
import scodec.msgpack._
import scodec.bits.{BitVector, ByteVector}
import cats.data.Xor

final class Scalajspack(serialize: Serialize[Json] = Scalajspack.default) {

  def json2msgpackBitVector(json: Json): Attempt[BitVector] =
    serialize.pack(json).flatMap{ b =>
      scodec.msgpack.codecs.MessagePackCodec.encode(b)
    }

  def jsonString2msgpackBitVector(json: String): Attempt[BitVector] =
    io.circe.parser.parse(json) match {
      case Xor.Left(a) => throw a
      case Xor.Right(a) =>
        json2msgpackBitVector(a)
    }

  def jsonString2msgpackBytes(json: String): Attempt[Array[Byte]] =
    jsonString2msgpackBitVector(json).map(_.toByteArray)

  def jsonString2msgpackBytesString(json: String): Attempt[String] =
    jsonString2msgpackBytes(json).map(Scalajspack.byte2string)

}

object Scalajspack {
  val default: Serialize[Json] = serialize(UnpackOptions.default)

  val byte2string: Array[Byte] => String = { bytes =>
    val builder = new StringBuilder
    var i = 0
    while(i < bytes.length){
      builder.append("%02x".format(bytes(i) & 0xff))
      i += 1
    }
    builder.toString
  }

  def serialize(options: UnpackOptions): Serialize[Json] = new Serialize[Json]{
    private[this] val MNilSuccess = Attempt.successful(MNil)
    private[this] val MTrueSuccess = Attempt.successful(MTrue)
    private[this] val MFalseSuccess = Attempt.successful(MFalse)

    private[this] val NilSuccess = Attempt.successful(Json.empty)
    private[this] val TrueSuccess = Attempt.successful(Json.True)
    private[this] val FalseSuccess = Attempt.successful(Json.False)

    private[this] val arraySerialize: Serialize[Vector[Json]] = Serialize.array(this)
    private[this] val objectSerialize: Serialize[Map[String, Json]] = Serialize.map(Serialize[String], this)

    def pack(json: Json): Attempt[MessagePack] = json.fold(
      jsonNull = MNilSuccess,
      jsonBoolean = value => if(value) MTrueSuccess else MFalseSuccess,
      jsonNumber = value => value.toLong match {
        case Some(n) =>
          Serialize[Long].pack(n)
        case None =>
          Serialize[Double].pack(value.toDouble)
      },
      jsonString = value => Serialize[String].pack(value),
      jsonArray = value => arraySerialize.pack(value.toVector),
      jsonObject = value => objectSerialize.pack(value.toMap)
    )

    def unpack(value: MessagePack): Attempt[Json] = {
      def long(i: Long) = Attempt.successful(Json.long(i))
      def string(i: String) = Attempt.successful(Json.string(i))
      def array(a: Vector[MessagePack]) = a match {
        case h +: t =>
          t.foldLeft(unpack(h).map(Vector(_))){
            (r, m) => for{
              x <- r
              y <- unpack(m)
            } yield x :+ y
          }.map(Json.fromValues(_))
        case _ =>
          Attempt.successful(Json.array())
      }
      def map(m: Map[MessagePack, MessagePack]) = Attempt.successful(
        Json.fromFields(
          m.map {
            case (k, v) =>
              unpack(k).flatMap { x =>
                x.asString match {
                  case Some(z) =>
                    Attempt.successful(z)
                  case None =>
                    options.nonStringKey(x)
                }
              }.require -> unpack(v).require
          }(collection.breakOut)
        ) // TODO should not use require inside map
      )

      value match {
        case MNil => NilSuccess
        case MFalse => FalseSuccess
        case MTrue => TrueSuccess
        case MPositiveFixInt(n) => long(n)
        case MFixMap(n) => map(n)
        case MFixArray(n) => array(n)
        case MFixString(n) => string(n)
        case MBinary8(n) => options.binary(n)
        case MBinary16(n) => options.binary(n)
        case MBinary32(n) => options.binary(n)
        case MFloat32(f) => Attempt.fromOption(Json.number(f), Err(f.toString)) // TODO use UnpackOptions
        case MFloat64(f) => Attempt.fromOption(Json.number(f), Err(f.toString)) // TODO use UnpackOptions
        case MUInt8(i) => long(i)
        case MUInt16(i) => long(i)
        case MUInt32(i) => long(i)
        case MUInt64(i) => long(i)
        case MInt8(i) => long(i)
        case MInt16(i) => long(i)
        case MInt32(i) => long(i)
        case MInt64(i) => long(i)
        case MExtension8(size, code, data) => options.extension(code, data)
        case MExtension16(size, code, data) => options.extension(code, data)
        case MExtension32(size, code, data) => options.extension(code, data)
        case MFixExtension1(code, data) => options.extension(code, data)
        case MFixExtension2(code, data) => options.extension(code, data)
        case MFixExtension4(code, data) => options.extension(code, data)
        case MFixExtension8(code, data) => options.extension(code, data)
        case MFixExtension16(code, data) => options.extension(code, data)
        case MString8(s) => string(s)
        case MString16(s) => string(s)
        case MString32(s) => string(s)
        case MArray16(a) => array(a)
        case MArray32(a) => array(a)
        case MMap16(m) => map(m)
        case MMap32(m) => map(m)
        case MNegativeFixInt(i) => long(i)
      }
    }
  }
}
