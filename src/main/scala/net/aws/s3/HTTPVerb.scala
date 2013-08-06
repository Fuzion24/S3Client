package net.aws.s3.HTTPVerb

sealed trait HTTPVerb

case object GET extends HTTPVerb {
  override def toString = "GET"
}

case object PUT extends HTTPVerb {
  override def toString = "PUT"
}

case object DELETE extends HTTPVerb {
  override def toString = "DELETE"
}