package net.aws.s3

import java.io.File
import scala.concurrent.{ExecutionContext, Future}
import java.util.Date
import utils.HMACSHA1
import com.ning.http.client.RequestBuilder
import dispatch.{url, Http}

case class AWSCreds(accessKeyID:String, secretAccessKey:String)

case class S3Key(key:String)

case class S3Request(httpVerb:String,
                     bucket:String = "",
                     resource:String = "",
                     contentMD5:String = "",
                     contentType:String = "",
                     date:Either[Date,String] = Left(new Date()),
                     amzHeaders:Map[String,Seq[String]] = Map())


abstract case class Bucket(name:String) {
  def put(file:String)

  def put(file:File)

  def get(key:S3Key)

  def delete(key:S3Key)

  def list(prefix:String)

  def rename(oldKey:S3Key, newKey:S3Key)
}

trait ReportingServiceExecutor {
  def exec(req:RequestBuilder)(implicit ec:ExecutionContext):Future[String]
  val BASE_URL:String
}

trait ReportingServiceHTTPExecutor extends ReportingServiceExecutor {
  val BASE_NAME = "s3.amazonaws.com"
  def exec(req:RequestBuilder)(implicit ec:ExecutionContext):Future[String] =
    Http(req) map {r =>
      if(r.getStatusCode == 200) r.getResponseBody
      else throw new Exception(s"Bad StatusCode: ${r.getStatusCode} ${r.getStatusText} ${r.getResponseBody}")
    }
}

object RequestSigner {
  val BASE_NAME = "s3.amazonaws.com"

  def apply(req:S3Request, creds:AWSCreds) =
    HMACSHA1(creds.secretAccessKey, getRequestString(req)).asBase64

  def getRequestString(req:S3Request):String = {
    val S3Request(httpVerb, bucket, resource, contentMD5, contentType, date, amzHeaders) = req
    val dateString = date match {
      case Left(d)        =>  RFC822DateParser.format(date)
      case Right(dString) =>  dString
    }
       s"""$httpVerb
          |$contentMD5
          |$contentType
          |$dateString
          |${canonicalizedAmzHeaders(amzHeaders)}${canonicalizedResource(bucket,resource)}
       """.trim.stripMargin
  }


  private def canonicalizedAmzHeaders(amzHeaders:Map[String,Seq[String]]):String = {
    def sort(headers:Map[String,Seq[String]]):Seq[(String,Seq[String])] =
      headers.toSeq.sortWith(_._1.toLowerCase < _._1.toLowerCase)

    sort(amzHeaders).map{
      case (k,v) => "%s:%s".format(k.toLowerCase, v.map(_.trim).mkString(","))
    }
  }.mkString("\n")

  private def canonicalizedResource(bucket:String, resource:String) = s"/$bucket/$resource"
}

/*
class S3Client(creds:AWSCreds) { self:RequestExecutor =>

  def listBuckets:Future[List[Bucket]] = {
    url("http://" + RequestSigner.BASE_NAME )
  }

  def createBucket(bucket:Bucket):Future[Bucket]

  def deleteBucket(bucketName:String)

}
*/
