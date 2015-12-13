package scalajspack

import scodec.{Attempt, Err}
import io.circe.Json
import scodec.bits.ByteVector

final case class UnpackOptions(
  extension: (ByteVector, ByteVector) => Attempt[Json],
  binary: ByteVector => Attempt[Json],
  positiveInf: Attempt[Json],
  negativeInf: Attempt[Json],
  nan: Attempt[Json],
  nonStringKey: Json => Attempt[String]
)

object UnpackOptions{
  val default: UnpackOptions = UnpackOptions(
    extension = (_, _) => Attempt.failure(Err("error extension")),
    binary = _  => Attempt.failure(Err("error binary")),
    positiveInf = Attempt.failure(Err("error pos inf")),
    negativeInf = Attempt.failure(Err("error neg inf")),
    nan = Attempt.failure(Err("error nan")),
    nonStringKey = v => Attempt.failure(Err("error non string key " + v))
  )
}
