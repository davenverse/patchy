package io.chrisdavenport.patchy

import io.chrisdavenport.patchy.decoding._
import shapeless.{LabelledGeneric, HList}
import io.circe.{Encoder, Decoder}
import io.chrisdavenport.patchy.encoding._

object Patchy{
  final def deriveFor[A]: DerivationHelper[A] = new DerivationHelper[A]

  final class DerivationHelper[A] {

    final def encoder(implicit encode: DerivedAsObjectEncoder[A]): Encoder.AsObject[A] = encode

    final def patch[R <: HList, O <: HList](implicit
        gen: LabelledGeneric.Aux[A, R],
        patch: PatchWithOptions.Aux[R, O],
        decode: ReprDecoder[O]
      ): Decoder[A => A] = DerivedDecoder.decodeCaseClassPatch[A, R, O]
  }
}