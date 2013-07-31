package net.aws.s3

import java.io.File
import scala.concurrent.Future
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.Http
import com.twitter.finagle.http.RequestBuilder
import org.jboss.netty.handler.codec.http.HttpRequest
import com.sun.deploy.net.HttpResponse
import java.text.SimpleDateFormat
import java.util.{Date, SimpleTimeZone, Locale}
import java.util

//import com.twitter.util.Future

case class AWSCreds(accessKeyID:String, secretAccessKey:String)

case class S3Key(key:String)


object rfc822DateParser extends SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US) {
  this.setTimeZone(new SimpleTimeZone(0, "GMT"))
}

abstract case class Bucket(name:String) {
  def put(file:File)

  def get(key:S3Key)

  def delete(key:S3Key)

  def list(prefix:String)

  def rename(oldKey:S3Key, newKey:S3Key)
}


object S3 {
  val BASE_NAME = "s3.amazonaws.com"

  def getRequestString(httpVerb:String,
                       bucket:String = "",
                       resource:String = "",
                       contentMD5:String = "",
                       contentType:String = "",
                       date:Date = new Date(),
                       amzHeaders:Map[String,Seq[String]] = Map()):String =
    s"""
      |$httpVerb
      |$contentType
      |$contentMD5
      |${rfc822DateParser.format(date)}
      |${canonicalizedAmzHeaders(amzHeaders)}
      |${canonicalizedResource(bucket,resource)}
    """.stripMargin

  private def canonicalizedAmzHeaders(amzHeaders:Map[String,Seq[String]]):String = {
    def sort(headers:Map[String,Seq[String]]):Seq[(String,Seq[String])] =
      headers.toSeq.sortWith(_._1.toLowerCase < _._1.toLowerCase)

    sort(amzHeaders).map{
      case (k,v) => "%s:%s".format(k.toLowerCase, v.map(_.trim).mkString(","))
    }
  }.mkString("\n")

  private def canonicalizedResource(bucket:String, resource:String) = s"/$bucket$resource"
}

class S3Client(creds:AWSCreds) { self:RequestExecutor =>

  def listBuckets:Future[List[Bucket]] = {
    RequestBuilder().url("http://" + S3.BASE_NAME ).buildGet
  }

  def createBucket(bucket:Bucket):Future[Bucket]

  def deleteBucket(bucketName:String)

}

class RequestExecutor {

}
