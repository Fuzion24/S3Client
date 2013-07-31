import java.io.File

case class Config( accessKeyID:String =  "",
                   accessKeySecret:String = "",
                   put:Option[File] = None,
                   get:Option[String] = None,
                   out:Option[File] = None
                  )

object Main extends App {

  val parser = new scopt.OptionParser[Config]("S3Client") {
    head("S3 Cmd Client", "1.0")
    opt[File]('p', "put") valueName("<file>") action { (x, c) =>
      c.copy(put = Some(x)) } text("path to file or folder to upload")
    opt[String]('g', "get") valueName("<S3Key>") action { (x, c) =>
      c.copy(get = Some(x)) } text("Get the file from S3 with this key")
    opt[String]('a', "accessKey") required() valueName("<AccessKeyID>") action { (x, c) =>
      c.copy(accessKeyID = x) } text("Your S3 Access Key ID")
    opt[String]('s', "accessKeySecret") required() valueName("<AccessKeySecret>") action { (x, c) =>
      c.copy(accessKeyID = x) } text("Your S3 Access Key Secret")
    opt[File]('o', "out") valueName("<file>") action { (x, c) =>
      c.copy(out = Some(x)) } text("output APK path")
    help("help") text("prints this usage text")
  }

  parser.parse(args, Config()) map { config =>

  } getOrElse {
    // arguments are bad, usage message will have been displayed
  }

}
