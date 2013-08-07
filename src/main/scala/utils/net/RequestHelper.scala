package utils.net

import dispatch._
import com.ning.http.client.RequestBuilder


object RequestHelper {

  implicit class RequestBuilderHelper(req:RequestBuilder) {
    def maybeOrMaybeNotAddQueryParameter(name: String, value: Option[String]):RequestBuilder = value match {
      case None =>    req
      case Some(v) => req.addQueryParameter(name, v)
    }

    def addHeader(key:String, value:Either[String,String]):RequestBuilder = value match {
      case Left(lv)   => req.addHeader(key,lv)
      case Right(rv)  => req.addHeader(key,rv)
    }

    def maybeAddHeader(key:String, value:Option[String]):RequestBuilder = value match {
      case None    => req
      case Some(v) => req.addHeader(key, v)
    }

    def maybeAddPutBody(body:Option[String]):RequestBuilder = body match {
      case None =>    req
      case Some(b) => req.PUT.toRequestBuilder.setBody(b)
    }

    def maybeAddPostBody(body:Option[String]):RequestBuilder = body match {
      case None    => req
      case Some(b) => req.POST.toRequestBuilder.setBody(b)
    }
  }


}