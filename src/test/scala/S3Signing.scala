import net.aws.s3.{RFC822DateParser, RequestSigner, AWSCreds, S3Request}
import org.scalatest.FunSuite
import utils.{Base64, HMACSHA1}

class S3Signing extends FunSuite {

  val creds = AWSCreds("AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")

  test("Get is signedProperly") {

    val getReq = S3Request(
      httpVerb = "GET",
      bucket = "johnsmith",
      resource = "photos/puppy.jpg",
      date = Right("Tue, 27 Mar 2007 19:36:42 +0000")
    )

    assert(RequestSigner(getReq,creds) ===  "bWq2s1WEIj+Ydj0vQ697zp+IXMU=")

  }

  test("Put request is signed properly"){
    val putReq = S3Request(
      httpVerb = "PUT",
      bucket = "johnsmith",
      contentType = "image/jpeg",
      resource = "photos/puppy.jpg",
      date = Right("Tue, 27 Mar 2007 21:15:45 +0000")
    )
    assert(RequestSigner(putReq,creds) == "MyyxeRY7whkBe+bq8fHCL/2kKUg=")
  }

  test("List request is signed properly"){
    val listReq = S3Request(
      httpVerb = "GET",
      bucket = "johnsmith",
      date = Right("Tue, 27 Mar 2007 19:42:41 +0000")
    )

    assert(RequestSigner(listReq,creds) == "htDYFYduRNen8P9ZfE/s9SuKy0U=")
  }

  test("Delete request is signed properly"){
    val delReq = S3Request(
      httpVerb = "DELETE",
      bucket = "johnsmith",
      resource = "photos/puppy.jpg",
      date = Right("Tue, 27 Mar 2007 21:20:26 +0000")
    )
    assert(RequestSigner(delReq,creds) == "lx3byBScXR6KzyMaifNkardMwNk=")
  }

}
