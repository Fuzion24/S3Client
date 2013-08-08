package net.aws.s3

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

case class S3BucketContents(items:Seq[S3Item], truncated:Boolean, marker:Option[String] = None)

case class S3BucketListing(buckets:List[S3Bucket])

case class S3Bucket(bucketName:String, creationDate:Option[Date] = None)

case class S3Item(key:String, lastModified:Date, size:Long)

case class AWSCreds(accessKeyID:String, secretAccessKey:String)

case class S3Key(key:String)

case class S3Request(httpVerb:HTTPVerb,
                     bucket:       Option[String] = None,
                     resource:     Option[String] = None,
                     contentMD5:   Option[String] = None,
                     contentType:  Option[String] = None,
                     body:         Option[Array[Byte]] = None,
                     date:Either[Date,String] = Left(new Date()),
                     amzHeaders:Map[String,Seq[String]] = Map(),
                     urlParams:    Map[String,String] = Map()){
  def canonicalizedResource =
    if(bucket.isEmpty && resource.isEmpty) "/"
    else s"/${bucket.getOrElse("")}/${resource.getOrElse("")}"

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

class S3Client(implicit creds:AWSCreds,ec:ExecutionContext){ self:S3RequestExecutor =>
  def listBuckets:Future[S3BucketListing] = exec(
    S3Request(
      httpVerb = GET
    )
  ) flatMap { bytes => S3BucketListing(new String(bytes)).toFuture }
}

class S3BucketOperations(bucket:S3Bucket)(implicit creds:AWSCreds, ec:ExecutionContext) {  self:S3RequestExecutor =>
  val S3Bucket(bucketName, bucketCreationDate) = bucket

  def presignedURL(s3Req:S3Request, expirationDate:Option[Date] = None):String = ""

  def put(key:S3Key,data:Array[Byte]) = exec(
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
    def listAllHelper(lastList:Future[S3BucketContents], acc:Set[S3Item]):Future[Set[S3Item]] = lastList.flatMap{ ll =>
      if(!ll.truncated) Future { acc ++ ll.items }
      else {
        import utils.FutureHelper._
        val futureList = Directly(3)(() => list(marker = Some(ll.items.last.key)))
        listAllHelper(futureList, acc ++ ll.items)
      }
    }
    listAllHelper(list(prefix = prefix),Set())
  }

  def list(prefix:Option[String] = None, maxKeys:Option[Int] = None, marker:Option[String] = None):Future[S3BucketContents] = exec(
    S3Request(
      httpVerb = GET,
      bucket   = Option(bucketName),
      urlParams = Map[String,String]() +++
                ("prefix"   -> prefix) +++
                ("max-keys" -> maxKeys.map(_.toString)) +++
                ("marker"   -> marker)
    )
  ) flatMap { bytes => S3BucketContents(new String(bytes)).toFuture }

}

object S3Item {
  def apply(key:String, lastModified:String, size:String):Try[S3Item] = Try {

    S3Item(key, S3ResultDate(lastModified).recover{ case _ => new Date(0)}.get, java.lang.Double.parseDouble(size).toLong)
  }
}

object S3BucketListing {
  def apply(xmlString:String):Try[S3BucketListing] = Try {
    val xmlNode = XML.loadString(xmlString)
    val buckets =
    for {
        buckets      <- xmlNode \ "Buckets"
        bucket       <- buckets \ "Bucket"
        bucketName   =  (bucket \ "Name").text
        creationDate =  (bucket \ "CreationDate").text
    } yield S3Bucket(bucketName, S3ResultDate(creationDate).toOption)

    new S3BucketListing(buckets.toList)
  }
}

object S3BucketContents {
  def apply(xmlString:String):Try[S3BucketContents] = Try {
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

    flatten(tItems) map {items => S3BucketContents(items, truncated, marker) }

  }.flatten
}


