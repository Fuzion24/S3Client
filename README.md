AWS S3 Client
=============

This is a Scala S3 library and command-line client that is both asyncronous and concurrent using evented I/O.  

Add the following to your Build.sbt:

```
libraryDependencies ++= Seq(""aws" %% "s3client" % "0.4")
 
resolvers ++= Seq("Fuzion Releases"   at "http://fuzion24.github.io/maven/releases/")
```

Downloading a file:

```scala
  implicit val reds = AWSCreds("XXXXXXXXXXXXX", "XXXXXXXXXXXXXXX")
  val s3bucket      = new S3BucketOperations(S3Bucket(")) with DispatchS3HTTPExecutor
  val s3client      = new S3Client with DispatchS3HTTPExecutor
  val file          = s3bucket.get(S3Key("S3KEYHERE"))   
```


