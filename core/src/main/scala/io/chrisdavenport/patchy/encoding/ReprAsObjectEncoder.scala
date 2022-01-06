package io.chrisdavenport.patchy.encoding


import io.circe.{ Encoder, JsonObject }
import shapeless.{ :+:, ::, CNil, Coproduct, HList, HNil, Inl, Inr, Lazy, Witness }
import shapeless.labelled.FieldType
import io.chrisdavenport.patchy.Patched

/**
 * An encoder for a generic representation of a case class or ADT.
 *
 * Note that users typically will not work with instances of this class.
 */
abstract class ReprAsObjectEncoder[A] extends Encoder.AsObject[A]

object ReprAsObjectEncoder extends LowPriorityImplicits {

  implicit def encodeHConsPatched[K <: Symbol, H, T <: HList](implicit
    key: Witness.Aux[K],
    encodeH: Encoder[H],
    encodeT: ReprAsObjectEncoder[T]
  ): ReprAsObjectEncoder[FieldType[K, Patched[H]] :: T] = new ReprAsObjectEncoder[FieldType[K, Patched[H]] :: T] {
    def encodeObject(a: FieldType[K, Patched[H]] :: T): JsonObject = a match {
      case h :: t =>
        Patched.encode(key.value.name, h).fold(encodeT.encodeObject(t)){
          case (k, v) => (k, v) +: encodeT.encodeObject(t)
        }
    }
  }

  implicit def encodeCoproductPatched[K <: Symbol, L, R <: Coproduct](implicit
    key: Witness.Aux[K],
    encodeL: Encoder[L],
    encodeR: Lazy[ReprAsObjectEncoder[R]]
  ): ReprAsObjectEncoder[FieldType[K, Patched[L]] :+: R] = new ReprAsObjectEncoder[FieldType[K, Patched[L]] :+: R] {
    private[this] lazy val cachedEncodeR: Encoder.AsObject[R] = encodeR.value

    def encodeObject(a: FieldType[K, Patched[L]] :+: R): JsonObject = a match {
      case Inl(l) => Patched.encode(key.value.name, l).fold(JsonObject.empty){
        case (k, json) => JsonObject.singleton(k, json)
      }
      case Inr(r) => cachedEncodeR.encodeObject(r)
    }
  }



}

private [encoding] trait LowPriorityImplicits {

  implicit val encodeHNil: ReprAsObjectEncoder[HNil] = new ReprAsObjectEncoder[HNil] {
    def encodeObject(a: HNil): JsonObject = JsonObject.empty
  }

  implicit def encodeHCons[K <: Symbol, H, T <: HList](implicit
    key: Witness.Aux[K],
    encodeH: Encoder[H],
    encodeT: ReprAsObjectEncoder[T]
  ): ReprAsObjectEncoder[FieldType[K, H] :: T] = new ReprAsObjectEncoder[FieldType[K, H] :: T] {
    def encodeObject(a: FieldType[K, H] :: T): JsonObject = a match {
      case h :: t => ((key.value.name, encodeH(h))) +: encodeT.encodeObject(t)
    }
  }

  implicit val encodeCNil: ReprAsObjectEncoder[CNil] = new ReprAsObjectEncoder[CNil] {
    def encodeObject(a: CNil): JsonObject =
      sys.error("No JSON representation of CNil (this shouldn't happen)")
  }

  implicit def encodeCoproduct[K <: Symbol, L, R <: Coproduct](implicit
    key: Witness.Aux[K],
    encodeL: Encoder[L],
    encodeR: Lazy[ReprAsObjectEncoder[R]]
  ): ReprAsObjectEncoder[FieldType[K, L] :+: R] = new ReprAsObjectEncoder[FieldType[K, L] :+: R] {
    private[this] lazy val cachedEncodeR: Encoder.AsObject[R] = encodeR.value

    def encodeObject(a: FieldType[K, L] :+: R): JsonObject = a match {
      case Inl(l) => JsonObject.singleton(key.value.name, encodeL(l))
      case Inr(r) => cachedEncodeR.encodeObject(r)
    }
  }

}