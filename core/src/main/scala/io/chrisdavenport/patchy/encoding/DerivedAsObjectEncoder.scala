package io.chrisdavenport.patchy.encoding

import io.circe.{ Encoder, JsonObject }
import shapeless.{LabelledGeneric, Lazy}

abstract class DerivedAsObjectEncoder[A] extends Encoder.AsObject[A]

object DerivedAsObjectEncoder {
  implicit def deriveEncoder[A, R](implicit
    gen: LabelledGeneric.Aux[A, R],
    encodeR: Lazy[ReprAsObjectEncoder[R]]
  ): DerivedAsObjectEncoder[A] = new DerivedAsObjectEncoder[A] {
    private[this] lazy val cachedEncodeR: Encoder.AsObject[R] = encodeR.value
    final def encodeObject(a: A): JsonObject = cachedEncodeR.encodeObject(gen.to(a))
  }
}