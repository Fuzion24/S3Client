import net.aws.s3._
import net.aws.s3.AWSCreds
import net.aws.s3.S3Request
import org.scalatest.FunSuite

class S3RequestSigning extends FunSuite {

  /* Examples taken from:
      http://docs.aws.amazon.com/AmazonS3/latest/dev/RESTAuthentication.html
   */
  val creds = AWSCreds("AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")

  test("Get is signedProperly") {
    val getReq = S3Request(
      httpVerb     = HTTPVerb.GET,
      bucket       = Option("johnsmith"),
      resource     = Option("photos/puppy.jpg"),
      date         = Right("Tue, 27 Mar 2007 19:36:42 +0000")
    )

    assert(S3RequestSigner(getReq,creds) ===  "bWq2s1WEIj+Ydj0vQ697zp+IXMU=")
  }

  test("Put request is signed properly"){
    val putReq = S3Request(
      httpVerb      = HTTPVerb.PUT,
      bucket        = Option("johnsmith"),
      contentType   = Option("image/jpeg"),
      resource      = Option("photos/puppy.jpg"),
      date          = Right("Tue, 27 Mar 2007 21:15:45 +0000")
    )
    assert(S3RequestSigner(putReq,creds) == "MyyxeRY7whkBe+bq8fHCL/2kKUg=")
  }

  test("List request is signed properly"){
    val listReq = S3Request(
      httpVerb    = HTTPVerb.GET,
      bucket      = Option("johnsmith"),
      date        = Right("Tue, 27 Mar 2007 19:42:41 +0000")
    )

    assert(S3RequestSigner(listReq,creds) == "htDYFYduRNen8P9ZfE/s9SuKy0U=")
  }

  test("Delete request is signed properly"){
    val delReq = S3Request(
      httpVerb = HTTPVerb.DELETE,
      bucket   = Option("johnsmith"),
      resource = Option("photos/puppy.jpg"),
      date     = Right("Tue, 27 Mar 2007 21:20:26 +0000")
    )
    assert(S3RequestSigner(delReq,creds) == "lx3byBScXR6KzyMaifNkardMwNk=")
  }

}
