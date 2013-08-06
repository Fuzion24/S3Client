package net.aws.s3

import java.text.SimpleDateFormat
import java.util.{SimpleTimeZone, Locale}

object RFC822DateParser extends SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US) {
  this.setTimeZone(new SimpleTimeZone(0, "GMT"))
}

//Example Date: "2013-05-14T21:21:15.000Z"
object S3ResultDate extends SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US) {
  this.setTimeZone(new SimpleTimeZone(0, "GMT"))
}
