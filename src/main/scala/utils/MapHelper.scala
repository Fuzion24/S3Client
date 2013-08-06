package utils


object MapHelper {

  implicit class MapHelper[A,B](req:Map[A,B]) {
    def +++ [B1 >:B](kv:(A,Option[B1])):Map[A,B1] = kv._2 match {
      case None => req
      case Some(v) => req + (kv._1 -> v)
    }
  }

}