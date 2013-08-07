package net.aws.s3

import java.io.File
import scala.concurrent.{ExecutionContext, Future}
import utils.MapHelper._
import java.util.Date
import net.aws.s3.HTTPVerb._
import scala.xml.XML
import scala.Some
import scala.util.{Failure, Success, Try}
import utils.FutureHelper._

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

  def listAll(prefix:Option[String]):Future[Set[S3Item]] = {
    def listAllHelper(lastList:Future[S3Listing], acc:Set[S3Item]):Future[Set[S3Item]] = lastList.flatMap{ ll =>
      if(!ll.truncated) Future { acc ++ ll.items }
      else {
        import utils.FutureHelper._
        val futureList = Directly(3)(() => list(marker = Some(ll.items.last.key)))
        listAllHelper(futureList, acc ++ ll.items)
      }
    }
    listAllHelper(list(prefix = prefix),Set())
  }

  def list(prefix:Option[String] = None, maxKeys:Option[Int] = None, marker:Option[String] = None):Future[S3Listing] = exec(
    S3Request(
      httpVerb = GET,
      bucket   = Option(bucketName),
      urlParams = Map[String,String]() +++
                ("prefix"   -> prefix) +++
                ("max-keys" -> maxKeys.map(_.toString)) +++
                ("marker"   -> marker)
    )
  ) flatMap { bytes => S3Listing(new String(bytes)).toFuture }

}

object S3Item {
  def apply(key:String, lastModified:String, size:String):Try[S3Item] = Try {

    val date = Try { S3ResultDate.parse(lastModified) } recover {
      case _  => new Date(0)
    }

    S3Item(key, date.get, java.lang.Double.parseDouble(size).toLong)
  }
}

object S3Listing {
  def apply(xmlString:String):Try[S3Listing] = Try {
    val xmlNode   = XML.loadString(xmlString)
    val truncated = (xmlNode \ "IsTruncated").text.toBoolean
    val marker    = (xmlNode \ "Marker").text.trim match {
      case ""       => None
      case s:String => Some(s)
    }

    val tItems = for { contents     <- xmlNode \ "Contents"
                      contentItem  <- contents
                      key          = (contentItem \ "Key").text
                      eTag         = (contentItem \ "ETag").text
                      lastModified = (contentItem \ "LastModified").text
                      size         = (contentItem \ "Size").text
    } yield S3Item(key, lastModified, size)

    flatten(tItems) map {items => S3Listing(items, truncated, marker) }

  }.flatten
}


