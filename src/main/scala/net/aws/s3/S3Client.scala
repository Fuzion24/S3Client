package net.aws.s3

import java.io.File
import scala.concurrent.{ExecutionContext, Future}
import utils.MapHelper._
import java.util.Date
import net.aws.s3.HTTPVerb._
import scala.xml.XML
import scala.Some

object S3 {
  val BASE_URL = "s3.amazonaws.com"
}


case class S3Listing(items:Seq[S3Item], truncated:Boolean, marker:Option[String] = None)

case class S3Item(key:String, lastModified:Date, size:Long)

case class AWSCreds(accessKeyID:String, secretAccessKey:String)

case class S3Key(key:String)

case class S3Request(httpVerb:HTTPVerb,
                     bucket:       Option[String] = None,
                     resource:     Option[String] = None,
                     contentMD5:   Option[String] = None,
                     contentType:  Option[String] = None,
                     date:Either[Date,String] = Left(new Date()),
                     amzHeaders:Map[String,Seq[String]] = Map(),
                     urlParams:    Map[String,String] = Map()){
  def canonicalizedResource = s"/${bucket.getOrElse("")}/${resource.getOrElse("")}"

  def canonicalizedAmzHeaders:String = {
    def sort(headers:Map[String,Seq[String]]):Seq[(String,Seq[String])] =
      headers.toSeq.sortWith(_._1.toLowerCase < _._1.toLowerCase)

    sort(amzHeaders).map{
      case (k,v) => "%s:%s".format(k.toLowerCase, v.map(_.trim).mkString(","))
    }

  }.mkString("\n")

  def dateString = date match {
    case Left(d)        =>  RFC822DateParser.format(d)
    case Right(dString) =>  dString
  }
}



class S3Bucket(bucketName:String)(implicit creds:AWSCreds, ec:ExecutionContext) {  self:S3RequestExecutor =>

  def put(file:File) = exec(
    S3Request(
      httpVerb    = PUT,
      bucket      = Option(bucketName),
      contentType = Option("image/jpeg"),
      resource    = Option("photos/puppy.jpg")
    )
  ) map(new String(_))

  def get(key:S3Key):Future[Array[Byte]] = exec(
    S3Request(
      httpVerb = GET,
      bucket   = Option(bucketName),
      resource = Option(key.key)
    )
  )

  def delete(key:S3Key):Future[String] = exec(
    S3Request(
      httpVerb = DELETE,
      bucket   = Option(bucketName),
      resource = Option(key.key)
    )
  ) map(new String(_))

  /*
  def listAll:Future[Seq[S3Item]] = {
    def listAllHelper(acc:List[S3Item])
      list()
  }
  */

  def list(prefix:Option[String] = None, maxKeys:Option[Int] = None, marker:Option[String] = None):Future[S3Listing] = exec(
    S3Request(
      httpVerb = GET,
      bucket   = Option(bucketName),
      urlParams = Map[String,String]() +++
                ("prefix"   -> prefix) +++
                ("max-keys" -> maxKeys.map(_.toString)) +++
                ("marker"   -> marker)
    )
  ) map { bytes => S3Listing(new String(bytes)) }

}

object S3Item {
  def apply(key:String,lastModified:String,size:String):S3Item =
    S3Item(key, S3ResultDate.parse(lastModified), Integer.parseInt(size))
}

object S3Listing {
  def apply(xmlString:String):S3Listing = {
    val xmlNode   = XML.loadString(xmlString)
    val truncated = (xmlNode \ "IsTruncated").text.toBoolean

    val marker    = (xmlNode \ "Marker").text.trim match {
      case ""       => None
      case s:String => Some(s)
    }

    val items = for { contents     <- xmlNode \ "Contents"
                      contentItem  <- contents
                      key          = (contentItem \ "Key").text
                      eTag         = (contentItem \ "ETag").text
                      lastModified = (contentItem \ "LastModified").text
                      size         = (contentItem \ "Size").text
    } yield S3Item(key, lastModified, size)

    S3Listing(items, truncated, marker)
  }
}


