package net.aws.s3

import java.text.SimpleDateFormat
import java.util.{SimpleTimeZone, Locale}

object RFC822DateParser extends SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US) {
  this.setTimeZone(new SimpleTimeZone(0, "GMT"))
}