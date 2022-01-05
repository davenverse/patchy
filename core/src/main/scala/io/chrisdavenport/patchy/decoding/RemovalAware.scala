package io.chrisdavenport.patchy.decoding

import io.circe._
import cats.syntax.all._

sealed trait RemovalAware[+A]
object RemovalAware {
  case class Set[A](a: A) extends RemovalAware[A]
  case object Unset extends RemovalAware[Nothing]
  case object Ignore extends RemovalAware[Nothing]

  def combineOpt[A](opt: Option[A], ra: RemovalAware[A]): Option[A] = ra match {
    case Ignore => opt
    case Unset => None
    case Set(a) => Some(a)
  }

  def combine[A](a: A, ra: RemovalAware[A]): A = ra match {
    case Ignore | Unset => a
    case Set(newA) => newA
  }

  implicit def decodeRemovalAware[A](implicit d: Decoder[A]): Decoder[RemovalAware[A]] = 
    new Decoder[RemovalAware[A]]{
      def apply(c: HCursor): Decoder.Result[RemovalAware[A]] = tryDecode(c)

      override def tryDecode(c: ACursor): Decoder.Result[RemovalAware[A]] = c match {
        case c: HCursor => 
          if (c.value.isNull) Unset.asRight
          else d(c) match {
            case Right(a) => Set(a).asRight
            case Left(df) => Left(df)
          }
        case c: FailedCursor => 
          if (!c.incorrectFocus) Ignore.asRight
          else  Left(DecodingFailure("Missing required field", c.history))
        case _ => Left(DecodingFailure("Unknown", c.history))
      }
    }
}