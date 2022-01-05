package io.chrisdavenport.patchy

import munit.FunSuite
import io.circe.Decoder
import io.circe.parser._

class PatchySpec extends FunSuite {

  test("Should Unset a Key Succesfully") {
    case class Foo(foo: Option[String])
    implicit val patchy: Decoder[Foo => Foo] = Patchy.deriveFor[Foo].patch
    val json = parse("""{ "foo" : null }""")
    val initial = Foo(Some("bar"))
    val test = json.flatMap(patchy.decodeJson).map(_(initial))

    assertEquals(test, Right(Foo(None)))
  }

  test("Should ignore and absent value"){
    case class Foo(foo: Option[String])
    implicit val patchy: Decoder[Foo => Foo] = Patchy.deriveFor[Foo].patch
    val json = parse("""{}""")
    val initial = Foo(Some("bar"))
    val test = json.flatMap(patchy.decodeJson).map(_(initial))

    assertEquals(test, Right(initial))
  }

  test("Should Update a value succesfully"){
    case class Foo(foo: Option[String])
    implicit val patchy: Decoder[Foo => Foo] = Patchy.deriveFor[Foo].patch
    val json = parse("""{ "foo" : "baz" }""")
    val initial = Foo(Some("bar"))
    val test = json.flatMap(patchy.decodeJson).map(_(initial))

    assertEquals(test, Right(Foo(Some("baz"))))
  }

  test("Set a nested value correctly"){
    case class Foo(foo: Option[String])
    case class Zed(i: Int, foo2: Option[Foo])

    import io.circe.generic.auto._ // Necessary for the generic for Foo to be in scope to then be dealt with

    implicit val patchy: Decoder[Zed => Zed] = Patchy.deriveFor[Zed].patch
    val json = parse("""{ "foo2": { "foo" : "baz" }}""")
    val initial = Zed(3, Some(Foo(Some("bar"))))
    val test = json.flatMap(patchy.decodeJson).map(_(initial))

    assertEquals(test, Right(Zed(3, Some(Foo(Some("baz"))))))
  }

  test("Unset a nested value correctly"){
    case class Foo(foo: Option[String])
    case class Zed(i: Int, foo2: Option[Foo])

    import io.circe.generic.auto._ // Necessary for the generic for Foo to be in scope to then be dealt with

    implicit val patchy: Decoder[Zed => Zed] = Patchy.deriveFor[Zed].patch
    val json = parse("""{ "foo2": { "foo" : null }}""")
    val initial = Zed(3, Some(Foo(Some("bar"))))
    val test = json.flatMap(patchy.decodeJson).map(_(initial))

    assertEquals(test, Right(Zed(3, Some(Foo(None)))))
  }

  test("Unset an entire nested structure"){
    case class Foo(foo: String, z: Int)
    case class Zed(i: Int, foo2: Option[Foo])

    import io.circe.generic.auto._ // Necessary for the generic for Foo to be in scope to then be dealt with

    implicit val patchy: Decoder[Zed => Zed] = Patchy.deriveFor[Zed].patch
    val json = parse("""{ "foo2": null}""")
    val initial = Zed(3, Some(Foo("bar", 5)))
    val test = json.flatMap(patchy.decodeJson).map(_(initial))

    assertEquals(test, Right(Zed(3, None)))
  }

}
