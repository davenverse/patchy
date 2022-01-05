package io.chrisdavenport.patchy.decoding

import shapeless._
import shapeless.labelled.{ FieldType, field }

trait PatchWithOptions[R <: HList] {
  type Out <: HList

  def apply(r: R, o: Out): R
}

object PatchWithOptions extends LowPriorityImplicits {

  implicit final val hnilPatchWithOptions: Aux[HNil, HNil] =
    new PatchWithOptions[HNil] {
      final type Out = HNil

      final def apply(r: HNil, o: HNil): HNil = HNil
    }

  implicit final def hconsPatchWithOptionsOptions[K <: Symbol, V, T <: HList](implicit
    tailPatch: PatchWithOptions[T]
  ): Aux[FieldType[K, Option[V]] :: T, FieldType[K, RemovalAware[V]] :: tailPatch.Out] =
    new PatchWithOptions[FieldType[K, Option[V]] :: T] {
      final type Out = FieldType[K, RemovalAware[V]] :: tailPatch.Out
      final def apply(
        r: FieldType[K, Option[V]] :: T,
        o: FieldType[K, RemovalAware[V]] :: tailPatch.Out
      ): FieldType[K, Option[V]] :: T =
        field[K](RemovalAware.combineOpt(r.head, o.head)) :: tailPatch(r.tail, o.tail)
    }
  
}

trait LowPriorityImplicits {
  final type Aux[R <: HList, Out0 <: HList] = PatchWithOptions[R] { type Out = Out0 }

  // Should be lower priority as Option should have higher specificity, but just in case.
  implicit final def hconsPatchWithOptions[K <: Symbol, V, T <: HList](implicit
    tailPatch: PatchWithOptions[T]
  ): Aux[FieldType[K, V] :: T, FieldType[K, RemovalAware[V]] :: tailPatch.Out] =
    new PatchWithOptions[FieldType[K, V] :: T] {
      final type Out = FieldType[K, RemovalAware[V]] :: tailPatch.Out

      final def apply(
        r: FieldType[K, V] :: T,
        o: FieldType[K, RemovalAware[V]] :: tailPatch.Out
      ): FieldType[K, V] :: T =
        field[K](RemovalAware.combine(r.head, o.head)) :: tailPatch(r.tail, o.tail)
    }

}