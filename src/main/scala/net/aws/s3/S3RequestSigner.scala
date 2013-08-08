package net.aws.s3

import utils.HMACSHA1

object S3RequestSigner {
  def apply(req:S3Request, creds:AWSCreds) =
    HMACSHA1(creds.secretAccessKey, getRequestString(req)).asBase64

  private def getRequestString(req:S3Request):String = {
    val S3Request(httpVerb, bucket, resource, contentMD5, contentType, requestBody, date, amzHeaders, urlParams) = req
    s"""$httpVerb
          |${contentMD5.getOrElse("")}
          |${contentType.getOrElse("")}
          |${req.dateString}
          |${req.canonicalizedAmzHeaders}${req.canonicalizedResource}
       """.trim.stripMargin
  }
}
