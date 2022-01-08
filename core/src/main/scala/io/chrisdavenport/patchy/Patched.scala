package io.chrisdavenport.patchy

import io.circe._
import cats.syntax.all._

sealed trait Patched[+A]
object Patched {
  case class Replace[A](a: A) extends Patched[A] // key: value
  case object Remove extends Patched[Nothing] // key: null
  case object Ignore extends Patched[Nothing] // absent key

  def combineOpt[A](opt: Option[A], ra: Patched[A]): Option[A] = ra match {
    case Ignore => opt
    case Remove => None
    case Replace(a) => Some(a)
  }

  def combine[A](a: A, ra: Patched[A]): A = ra match {
    case Ignore | Remove => a
    case Replace(newA) => newA
  }

  implicit def decodePatched[A](implicit d: Decoder[A]): Decoder[Patched[A]] = 
    new Decoder[Patched[A]]{
      def apply(c: HCursor): Decoder.Result[Patched[A]] = tryDecode(c)

      override def tryDecode(c: ACursor): Decoder.Result[Patched[A]] = c match {
        case c: HCursor => 
          if (c.value.isNull) Remove.asRight
          else d(c) match {
            case Right(a) => Replace(a).asRight
            case Left(df) => Left(df)
          }
        case c: FailedCursor => 
          if (!c.incorrectFocus) Ignore.asRight
          else  Left(DecodingFailure("Missing required field", c.history))
        case _ => Left(DecodingFailure("Unknown", c.history))
      }
    }

  def encode[A](field: String, patched: Patched[A])(implicit e: Encoder[A]): Option[(String, Json)] = patched match {
    case Ignore => None
    case Remove =>  (field -> Json.Null).some
    case Replace(a) => (field -> e(a)).some
  }
}