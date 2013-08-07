package net.aws.s3

import scala.concurrent.{Future, ExecutionContext}
import dispatch.{Http, url}


trait S3RequestExecutor {
  protected def exec(req:S3Request)(implicit ec:ExecutionContext, creds:AWSCreds ):Future[Array[Byte]]
  val BASE_URL:String
}

trait DispatchS3HTTPExecutor extends S3RequestExecutor {
  val BASE_URL = "s3.amazonaws.com"
  import utils.net.RequestHelper._
  protected def exec(req:S3Request)(implicit ec:ExecutionContext, creds:AWSCreds):Future[Array[Byte]] = {
    Http(toDispatchReq(req)) map {r =>
      if(r.getStatusCode == 200) r.getResponseBodyAsBytes
      else throw new Exception(s"Bad StatusCode: ${r.getStatusCode} ${r.getStatusText} ${r.getResponseBody}")
    }
  }

  protected def toDispatchReq(req:S3Request)(implicit creds:AWSCreds) = {
    var r = url(s"http://$BASE_URL${req.canonicalizedResource}").toRequestBuilder.
      maybeAddHeader("content-type",req.contentType).
      maybeAddHeader("Content-MD5", req.contentMD5).
      addHeader("Date",req.dateString).
      addHeader("Accept-Encoding", "gzip").
      addHeader("Authorization", s"AWS ${creds.accessKeyID}:${S3RequestSigner(req,creds)}")
    for { (k,seqV) <- req.amzHeaders
          v <- seqV
    } { r = r.addHeader(k,v) }

    for {(k,v) <- req.urlParams}{ r = r.addQueryParameter(k,v)}

    r
  }

}
