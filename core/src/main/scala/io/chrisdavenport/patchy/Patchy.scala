package io.chrisdavenport.patchy

import io.chrisdavenport.patchy.decoding._
import shapeless.{LabelledGeneric, HList}
import io.circe.Decoder

object Patchy{
  final def deriveFor[A]: DerivationHelper[A] = new DerivationHelper[A]

  final class DerivationHelper[A] {
    final def patch[R <: HList, O <: HList](implicit
        gen: LabelledGeneric.Aux[A, R],
        patch: PatchWithOptions.Aux[R, O],
        decode: ReprDecoder[O]
      ): Decoder[A => A] = DerivedDecoder.decodeCaseClassPatch[A, R, O]
  }
}