# patchy - Circe Patch Support [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.chrisdavenport/patchy_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.chrisdavenport/patchy_2.12) ![Code of Conduct](https://img.shields.io/badge/Code%20of%20Conduct-Scala-blue.svg)

## [Head on over to the microsite](https://ChristopherDavenport.github.io/patchy)

## Quick Start

To use patchy in an existing SBT project with Scala 2.12 or a later version, add the following dependencies to your
`build.sbt` depending on your needs:

```scala
libraryDependencies ++= Seq(
  "io.chrisdavenport" %% "patchy" % "<version>"
)
```

## Examples

Patch Decoding with circe. currently cannot unset a value as absence of a value, and nulling of
a value are both equivalent. Ideally this place can work out the details, and then contribute it
back to circe.

The semantics are `"key": null` for an optional field will instead clear the field. The absence
of the field will not effect the option. If the key is present in the patch it will be updated.

```
case class Foo(x: String, foo: Option[String])

{
  "foo": null
}

Clear The Field

Foo("bar", Some("baz")) -> Patch -> Foo("bar", None)

-----

{ 
  "x" : "zed"
}

Optional Field Absent

Foo("bar", Some("baz")) -> Patch -> Foo("zed", Some("baz"))

----

{
  "foo": "set by patch"
}

Set Optional Field

Foo("bar", Some("baz")) -> Patch -> Foo("bar", Some("set by patch"))
```