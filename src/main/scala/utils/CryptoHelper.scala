package utils

import java.security.MessageDigest
import javax.crypto.Mac

case class SHA1(hash:String)
case class MD5(hash:String)

abstract class HashAlg(val digestName: String) {

  class Hash(val bytes: Array[Byte]) {
    def asString =
      asBytes
        .map("%02X" format _)
        .mkString
        .toLowerCase

    def asBytes =
      MessageDigest
        .getInstance(digestName)
        .digest(bytes)
  }

  def apply(bytes: Array[Byte]) = new Hash(bytes)
}

object SHA1   extends HashAlg("SHA1")
object MD5    extends HashAlg("MD5")
object SHA256 extends HashAlg("SHA-256")

object HMACSHA1 extends IntegrityAlgo("HMACSHA1")

abstract class IntegrityAlgo(val digestName:String){
  class Integrity(val key:Array[Byte], val bytes: Array[Byte]) {
    def asString =
      asBytes
        .map("%02X" format _)
        .mkString
        .toLowerCase

    def asBase64 = Base64(asBytes)

    lazy val asBytes = {
      val key = new javax.crypto.spec.SecretKeySpec(key, digestName)
      val m = Mac.getInstance(digestName)
      m.init(key)
      m.update(bytes)
      m.doFinal()
    }
  }

  def apply(key:Array[Byte], data: Array[Byte]) = new Integrity(key,data)
  def apply(key:String,      data: Array[Byte]) = new Integrity(key.getBytes("UTF-8"), data)
  def apply(key:String,      data: String)      = new Integrity(key.getBytes("UTF-8"), data.getBytes("UTF-8"))
}

object Base64 {
  def apply(data:Array[Byte]):String =
    new sun.misc.BASE64Encoder().encode(data)
}

object CryptoHelper {

  type Hash = Array[Byte]

  def hashData(data:Array[Byte]):(String,String) = (SHA1(data).asString, MD5(data).asString)

  def SHAHash(s:String):String  = SHA1(s.getBytes).asString

  def hashToBytes(data:Array[Byte]):(Array[Byte],Array[Byte]) = (SHA1(data).asBytes, MD5(data).asBytes)

  def hashToString(hash: Hash) = hash.map(_.formatted("%02X")).mkString

  def stringToHash(hex: String): Hash = {
    try {
      (for { i <- 0 to hex.length-1 by 2 if i > 0 || !hex.startsWith( "0x" )} yield hex.substring( i, i+2 ))
        .map( Integer.parseInt( _, 16 ).toByte ).toArray
    } catch {
      case e: NumberFormatException => Array()
    }
  }

}
