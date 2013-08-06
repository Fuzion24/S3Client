package net.aws.s3

import java.io.File
import scala.concurrent.{ExecutionContext, Future}
import java.util.Date
import utils.HMACSHA1
import utils.MapHelper._
import net.aws.s3.HTTPVerb.HTTPVerb

object S3 {
  val BASE_URL = "s3.amazonaws.com"
}

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

  //def put(file:String):Future[String] = put(new File(file))

  def put(file:File) = exec(
    S3Request(
      httpVerb    = HTTPVerb.PUT,
      bucket      = Option(bucketName),
      contentType = Option("image/jpeg"),
      resource    = Option("photos/puppy.jpg")
    )
  ) map(new String(_))

  def get(key:S3Key):Future[Array[Byte]] = exec(
    S3Request(
      httpVerb = HTTPVerb.GET,
      bucket   = Option(bucketName),
      resource = Option(key.key)
    )
  )

  def delete(key:S3Key):Future[String] = exec(
    S3Request(
      httpVerb = HTTPVerb.DELETE,
      bucket   = Option(bucketName),
      resource = Option(key.key)
    )
  ) map(new String(_))

  def list(prefix:Option[String], maxKeys:Option[Int], marker:Option[String]):Future[String] = exec(
    S3Request(
      httpVerb = HTTPVerb.GET,
      urlParams = Map[String,String]() +++
                ("prefix"   -> prefix) +++
                ("max-keys" -> maxKeys.map(_.toString)) +++
                ("marker"   -> marker)
    )
  ) map(new String(_))

}


object S3RequestSigner {
  def apply(req:S3Request, creds:AWSCreds) =
    HMACSHA1(creds.secretAccessKey, getRequestString(req)).asBase64

  private def getRequestString(req:S3Request):String = {
    val S3Request(httpVerb, bucket, resource, contentMD5, contentType, date, amzHeaders, urlParams) = req
       s"""$httpVerb
          |${contentMD5.getOrElse("")}
          |${contentType.getOrElse("")}
          |${req.dateString}
          |${req.canonicalizedAmzHeaders}${req.canonicalizedResource}
       """.trim.stripMargin
  }
}
