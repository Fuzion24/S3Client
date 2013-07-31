import sbt._
import Keys._
import com.github.hexx.GithubRepoPlugin._
import com.typesafe.sbt.SbtGit.GitKeys._
import sbtassembly.Plugin._
import AssemblyKeys._

object Publish {
  val organization = "aws"
  val version      = "0.4"
  val localRepo    = Path.userHome / "github" / "maven"
  val githubRepo   = "git@github.com:fuzion24/maven.git"
}

object Versions {
  val scala     = "2.10.2"
  val scalatra  = "2.2.0"
  val scalatest = "1.9.1"
}

object AWSS3 extends Build {
  val projectName = "S3Client"

  lazy val application = Project(projectName, file("."), settings = aws_s3_settings)

  lazy val aws_s3_settings = 
    Defaults.defaultSettings ++ 
    githubRepoSettings ++ 
    assemblySettings ++
    Seq(
      name         := projectName,
      version      := Publish.version,
      organization := Publish.organization,    
      localRepo    := Publish.localRepo,
      githubRepo   := Publish.githubRepo,
      scalaVersion := Versions.scala,
      jarName in assembly := projectName + ".jar",
      mainClass in assembly := Some("com.apk.service.web.Standalone"),
      mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
      {
        case "application.conf" => MergeStrategy.concat
        case "reference.conf"   => MergeStrategy.concat
        case "mime.types"       => MergeStrategy.filterDistinctLines
        case PathList("org", "hamcrest", _ @ _*) => MergeStrategy.first
        case PathList("com", "google", "common", _ @ _*) => MergeStrategy.first
        case PathList("org", "xmlpull", _ @ _*) => MergeStrategy.first
        case PathList(ps @ _*) if ps.last.toLowerCase.startsWith("notice") ||
                                  ps.last.toLowerCase == "license" || 
                                  ps.last.toLowerCase == "license.txt" || 
                                  ps.last.toLowerCase == "asm-license.txt" ||
                                  ps.last.endsWith(".html") => MergeStrategy.rename 
        case PathList("META-INF", xs @ _*) =>
          (xs map {_.toLowerCase}) match {
            case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
              MergeStrategy.discard
            case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa")  =>
              MergeStrategy.discard
            case "services" :: xs =>
              MergeStrategy.filterDistinctLines
            case _ => MergeStrategy.deduplicate
          }
        case _ => MergeStrategy.deduplicate
      }
    },
    libraryDependencies ++= Seq(
	    "net.databinder.dispatch" %% "dispatch-core" % "0.10.1",
      "com.github.scopt" %% "scopt" % "3.1.0"
    )
  )
}