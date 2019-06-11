import java.io.File

import play.api.Logger

import scala.concurrent._
import scala.util.control.NonFatal
import scala.util.{Failure, Try}

package object utils {

  implicit class TryOps[T](t: Try[T]) {
    def logError(msg: => String): Try[T] = t.recoverWith {
      case e => Logger.error(msg, e)
        Failure(e)
    }
  }

  implicit class FutureOps[T](f: Future[T]) {
    def logError(msg: => String)(implicit ec: ExecutionContext): Future[T] = f.recoverWith {
      case NonFatal(e) => Logger.error(msg, e)
        Future.failed(e)
    }
  }

  implicit class FutureOption[T](future: Future[Option[T]]) {
    def flattenOption(implicit ec: ExecutionContext): Future[T] = {
      future.flatMap(_.map(Future.successful).getOrElse(Future.failed(new Exception())))
    }
  }

  implicit class PathExt(path: String) {
    def fixPathSeparator: String = path.replace('/', File.separatorChar)
  }
}
