import java.util.concurrent.Executors
import net.aws.s3._
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import scala.concurrent.{Future, ExecutionContext, Await}
import scala.concurrent.duration._
import scala.Option
import scala.util.{Failure, Success}
import utils.SHA1

class LiveS3Test extends FunSuite with BeforeAndAfter{

  val pool = Executors.newFixedThreadPool(100)
  implicit val executionContext = ExecutionContext.fromExecutorService(pool)

  import S3TestValues._ //This is where my private creds are

  val s3bucket = new S3BucketOperations(S3Bucket(S3TestValues.testBucket)) with DispatchS3HTTPExecutor
  val s3client = new S3Client with DispatchS3HTTPExecutor


  test("File Download"){
    pending
    val file = s3bucket.get(S3Key("0/00004a593fc62261114e4c659ce327eef91fa6ff"))
    val res = Await.result(file, 1 minute)
    assert(SHA1(res).asString == "00004a593fc62261114e4c659ce327eef91fa6ff")
  }

  test("List Operation"){
    val prefixes = (0 to 3).map(_.toString) ++ ('a' to 'f').map(_.toString)
    val fLists = prefixes map {p => s3bucket.listAll(Option(p))}
    val lists:Future[Set[S3Item]] = Future.sequence(fLists).map{ ll => ll.flatten.toSet }
    val files = Await.result(lists, 30 minutes)
  }


  test("List Buckets"){
    pending
    val fBuckets = s3client.listBuckets
    val buckets = Await.result(fBuckets, 10 minutes)
  }
}