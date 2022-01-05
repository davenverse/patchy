package io.chrisdavenport.patchy.decoding

import shapeless.{ HList, LabelledGeneric }
import io.circe.{ Decoder, HCursor }

trait IncompleteDerivedDecoders {

  implicit final def decodeCaseClassPatch[A, R <: HList, O <: HList](implicit
    gen: LabelledGeneric.Aux[A, R],
    patch: PatchWithOptions.Aux[R, O],
    decode: ReprDecoder[O]
  ): DerivedDecoder[A => A] = new DerivedDecoder[A => A] {
    final def apply(c: HCursor): Decoder.Result[A => A] = decode(c) match {
      case Right(o)    => Right(a => gen.from(patch(gen.to(a), o)))
      case l @ Left(_) => l.asInstanceOf[Decoder.Result[A => A]]
    }

    override final def decodeAccumulating(c: HCursor): Decoder.AccumulatingResult[A => A] =
      decode.decodeAccumulating(c).map(o => a => gen.from(patch(gen.to(a), o)))
  }

}