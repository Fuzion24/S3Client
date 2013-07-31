import sbt._
import Keys._
import com.github.hexx.GithubRepoPlugin._
import com.typesafe.sbt.SbtGit.GitKeys._

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
  lazy val application = Project("AWS-S3", file("."), settings = aws_s3_settings)

   lazy val aws_s3_settings = Defaults.defaultSettings ++ githubRepoSettings ++ Seq(
    name         := "S3Client",
    version      := Publish.version,
    organization := Publish.organization,    
    localRepo    := Publish.localRepo,
    githubRepo   := Publish.githubRepo,
    scalaVersion := Versions.scala,
    libraryDependencies ++= Seq(
	  "net.databinder.dispatch" %% "dispatch-core" % "0.10.1"	 
    )
  )
}