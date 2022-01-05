import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val Scala213 = "2.13.7"

ThisBuild / crossScalaVersions := Seq("2.12.15", Scala213)

ThisBuild / testFrameworks += new TestFramework("munit.Framework")

val circeV = "0.14.1"

val munitCatsEffectV = "1.0.7"

// Projects
lazy val `patchy` = project.in(file("."))
  .disablePlugins(MimaPlugin)
  .enablePlugins(NoPublishPlugin)
  .aggregate(core.jvm, core.js)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
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
  .disablePlugins(MimaPlugin)
  .enablePlugins(DavenverseMicrositePlugin)
  .dependsOn(core.jvm)
  .settings{
    import microsites._
    Seq(
      micrositeDescription := "Circe Patch Support",
    )
  }
