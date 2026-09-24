ThisBuild / tlBaseVersion := "0.2" // current series x.y

ThisBuild / organization := "io.chrisdavenport"
ThisBuild / organizationName := "Christopher Davenport"
ThisBuild / startYear := Some(2021)
ThisBuild / licenses := Seq(License.MIT)
ThisBuild / developers := List(
  tlGitHubDev("christopherdavenport", "Christopher Davenport")
)

// sbt-davenverse published a snapshot from main on every push; preserve that.
ThisBuild / tlCiReleaseBranches := Seq("main")

val Scala213 = "2.13.18"
// Scala 2 only: shapeless 2.x has no Scala 3 build.
ThisBuild / crossScalaVersions := Seq("2.12.20", Scala213)
ThisBuild / scalaVersion := Scala213

ThisBuild / testFrameworks += new TestFramework("munit.Framework")

val circeV = "0.14.1"
val munitCatsEffectV = "1.0.7"

// Compiler settings DavenversePlugin injected globally. sbt-typelevel-ci-release
// does not supply these (only sbt-typelevel-settings would).
lazy val davenverseCompat = Seq(
  libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, _)) =>
      Seq(
        compilerPlugin("org.typelevel" % "kind-projector" % "0.13.4" cross CrossVersion.full),
        compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
      )
    case _ => Nil
  }),
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((3, _)) => Seq("-Ykind-projector")
    case Some((2, 12)) => Seq("-Ypartial-unification")
    case _ => Nil
  })
)

// Projects
lazy val `patchy` = tlCrossRootProject.aggregate(core)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(davenverseCompat)
  .settings(
    name := "patchy",

    libraryDependencies ++= Seq(
      "io.circe"                    %%% "circe-core"                 % circeV,
      "com.chuusai"                 %%% "shapeless"                  % "2.3.7",
      "io.circe"                    %%% "circe-parser"               % circeV % Test,
      "io.circe"                    %%% "circe-generic"              % circeV % Test,
      "org.typelevel"               %%% "munit-cats-effect-3"        % munitCatsEffectV         % Test,
    )
  ).jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule)},
  )

lazy val site = project.in(file("site"))
  .enablePlugins(TypelevelSitePlugin)
  .dependsOn(core.jvm)
  .settings(
    laikaTheme := tlSiteHelium.value.site
      .topNavigationBar(
        homeLink = laika.helium.config.IconLink.internal(laika.ast.Path.Root / "index.md", laika.helium.config.HeliumIcon.home)
      )
      .build
  )
