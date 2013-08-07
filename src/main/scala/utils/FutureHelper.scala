package utils

import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import scala.concurrent.{Future,ExecutionContext}


object FutureHelper {
  implicit class RichTry[T](t:Try[T]) {
    def toFuture:Future[T] = t match {
      case Success(s) => Future.successful(s)
      case Failure(f) => Future.failed(f)
    }
  }

  def flatten[T](xs: Seq[Try[T]]): Try[Seq[T]] = {
    val (ss: Seq[Success[T]]@unchecked, fs: Seq[Failure[T]]@unchecked) =
      xs.partition(_.isSuccess)

    if (fs.isEmpty) Success(ss map (_.get))
    else Failure[Seq[T]](fs(0).exception) // Only keep the first failure
  }

  def after[A](time: Duration)(f: ⇒ Future[A])(implicit ec:ExecutionContext): Future[A] = Future {
    Thread.sleep(time.toMillis)
  } flatMap { _ ⇒ f }

  def retryUntil[T,X](condition:T => Boolean)(toF: X => Future[T])(item:X)(implicit ec:ExecutionContext):Future[T]  =
    toF(item) flatMap { a ⇒
      if(condition(a)) Future{ a } else
        after(5 seconds) (retryUntil(condition)(toF)(item))
    }

  object Directly extends CountingRetry {
    def apply[T](max: Int = 3)(promise: () => Future[T])
                (implicit executor: ExecutionContext): Future[T] = {
      retry(max, promise, Directly(_)(promise))
    }
  }

  trait CountingRetry {
    protected def retry[T](max: Int,
                           promise: () => Future[T],
                           orElse: Int => Future[T]
                            )(implicit executor: ExecutionContext) = {
      val fut = promise()
      fut.flatMap { res =>
        if (max < 1) fut
        else orElse(max - 1)
      }
    }
  }


}