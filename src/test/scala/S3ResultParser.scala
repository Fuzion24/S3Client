import net.aws.s3.{S3BucketContents, S3BucketListing, S3Item, S3}
import org.scalatest.FunSuite

class S3ResultParser extends FunSuite{

  test("Parse Bucket Contents XML"){
    val bucketContentsXML = """<?xml version="1.0" encoding="UTF-8"?><ListBucketResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/"><Name>appz</Name><Prefix></Prefix><Marker></Marker><MaxKeys>10</MaxKeys><IsTruncated>true</IsTruncated><Contents><Key>0/00004a593fc62261114e4c659ce327eef91fa6ff</Key><LastModified>2013-02-27T05:31:23.000Z</LastModified><ETag>&quot;1c9e53188d368054a9ca04b73a630244&quot;</ETag><Size>5922400</Size><Owner><ID>aaaaaaaabf3cb3a84fcc53dbe6cd9dc2da7ccf19400ffffffffffffffff00007</ID><DisplayName>bobdole</DisplayName></Owner><StorageClass>STANDARD</StorageClass></Contents><Contents><Key>0/0000720b45affd7d33acb5c96697d1f29791ed05</Key><LastModified>2013-05-14T21:21:15.000Z</LastModified><ETag>&quot;71072d071da653e1db28cccf328b05cf&quot;</ETag><Size>2772248</Size><Owner><ID>d8fe9fa9bf3cb3a84eecffdbe6cd9dc2da0ccf19448c3d747ea823014ee9ea16</ID><DisplayName>bobdole</DisplayName></Owner><StorageClass>STANDARD</StorageClass></Contents><Contents><Key>0/0000b24b72917e03b793e7ebd7e95ee98662df8c</Key><LastModified>2013-02-26T23:59:45.000Z</LastModified><ETag>&quot;032bfbdd1ec44ac8d6d17011ab6dd9ac&quot;</ETag><Size>2280674</Size><Owner><ID>aaaaaaaabf3cb3a84fcc53dbe6cd9dc2da7ccf19400ffffffffffffffff00007</ID><DisplayName>bobdole</DisplayName></Owner><StorageClass>STANDARD</StorageClass></Contents><Contents><Key>0/0000c892d37452d30c6ed5ba990fc0cf5bcbd5af</Key><LastModified>2013-02-21T22:05:29.000Z</LastModified><ETag>&quot;bb6bc3bb40c6e078e7d2e4d17a0a8910&quot;</ETag><Size>2739420</Size><Owner><ID>aaaaaaaabf3cb3a84fcc53dbe6cd9dc2da7ccf19400ffffffffffffffff00007</ID><DisplayName>bobdole</DisplayName></Owner><StorageClass>STANDARD</StorageClass></Contents><Contents><Key>0/00011644fd2be5fc4c7958a7479d132cb316d950</Key><LastModified>2013-03-01T05:38:15.000Z</LastModified><ETag>&quot;a8de32369aac12f9ed314a957755a4d6&quot;</ETag><Size>85827</Size><Owner><ID>aaaaaaaabf3cb3a84fcc53dbe6cd9dc2da7ccf19400ffffffffffffffff00007</ID><DisplayName>bobdole</DisplayName></Owner><StorageClass>STANDARD</StorageClass></Contents><Contents><Key>0/000129130d7a7029a06b60092f040d1979690ac6</Key><LastModified>2013-05-09T20:24:59.000Z</LastModified><ETag>&quot;d9f0e7756be5747992e6be3b011ea13d&quot;</ETag><Size>775572</Size><Owner><ID>aaaaaaaabf3cb3a84fcc53dbe6cd9dc2da7ccf19400ffffffffffffffff00007</ID><DisplayName>bobdole</DisplayName></Owner><StorageClass>STANDARD</StorageClass></Contents><Contents><Key>0/00013b32439bc279d553015c6020eba79adca63e</Key><LastModified>2013-05-10T03:29:32.000Z</LastModified><ETag>&quot;81cce513920a72aacb17b67f6441ec2a&quot;</ETag><Size>1643291</Size><Owner><ID>aaaaaaaabf3cb3a84fcc53dbe6cd9dc2da7ccf19400ffffffffffffffff00007</ID><DisplayName>bobdole</DisplayName></Owner><StorageClass>STANDARD</StorageClass></Contents><Contents><Key>0/00014107b38bcb01c873997a4b83859c1ac64484</Key><LastModified>2013-05-08T19:04:33.000Z</LastModified><ETag>&quot;724a8495fcf323842a211d111e061bdf&quot;</ETag><Size>14729</Size><Owner><ID>aaaaaaaabf3cb3a84fcc53dbe6cd9dc2da7ccf19400ffffffffffffffff00007</ID><DisplayName>bobdole</DisplayName></Owner><StorageClass>STANDARD</StorageClass></Contents><Contents><Key>0/00016b42ab37d526ea828fd0de33d1cf165fc8f1</Key><LastModified>2013-05-06T18:51:14.000Z</LastModified><ETag>&quot;9a124cb402778c0e5bd4b55b26f03655&quot;</ETag><Size>187419</Size><Owner><ID>aaaaaaaabf3cb3a84fcc53dbe6cd9dc2da7ccf19400ffffffffffffffff00007</ID><DisplayName>bobdole</DisplayName></Owner><StorageClass>STANDARD</StorageClass></Contents><Contents><Key>0/00018ccf6f302aa0b6c2b31808c647988818df5f</Key><LastModified>2013-03-02T04:00:15.000Z</LastModified><ETag>&quot;6b33c25a55ba1d4cd10b580cc9ab94f7&quot;</ETag><Size>616869</Size><Owner><ID>aaaaaaaabf3cb3a84fcc53dbe6cd9dc2da7ccf19400ffffffffffffffff00007</ID><DisplayName>bobdole</DisplayName></Owner><StorageClass>STANDARD</StorageClass></Contents></ListBucketResult>"""

    val listing = S3BucketContents(bucketContentsXML)

    assert(listing.isSuccess)

    val S3BucketContents(parsedList,truncated,marker) = listing.get
    assert(parsedList.size == 10)
    assert(parsedList.head.key == "0/00004a593fc62261114e4c659ce327eef91fa6ff")
  }

  test("Parse bucket listing contents"){
    val listBucketsXML    = """<?xml version="1.0" encoding="UTF-8"?><ListAllMyBucketsResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/"><Owner><ID>ffffffffffffb3a84fcc53dbe6cd9dc2da7ffffffffffffffffffffffff9ea16</ID><DisplayName>batman</DisplayName></Owner><Buckets><Bucket><Name>y-so-serious</Name><CreationDate>2011-10-13T21:28:15.000Z</CreationDate></Bucket><Bucket><Name>assurant-server</Name><CreationDate>2011-06-02T12:12:26.000Z</CreationDate></Bucket><Bucket><Name>mapred-tmp</Name><CreationDate>2013-06-12T18:47:28.000Z</CreationDate></Bucket><Bucket><Name>mrjob-5eef3a8ee0c07ba6</Name><CreationDate>2013-06-12T18:01:19.000Z</CreationDate></Bucket><Bucket><Name>s3-us-east-dmz</Name><CreationDate>2011-10-19T20:29:56.000Z</CreationDate></Bucket><Bucket><Name>s3-us-east-batcave-apps</Name><CreationDate>2013-02-18T17:15:57.000Z</CreationDate></Bucket><Bucket><Name>s3-us-east-batcave-backups</Name><CreationDate>2012-09-10T19:35:39.000Z</CreationDate></Bucket><Bucket><Name>s3-us-east-batcave-development</Name><CreationDate>2011-09-02T01:23:23.000Z</CreationDate></Bucket><Bucket><Name>s3-us-east-batcave-integration</Name><CreationDate>2011-08-31T14:08:33.000Z</CreationDate></Bucket><Bucket><Name>s3-us-east-batcave-lost-and-found</Name><CreationDate>2012-09-10T19:07:54.000Z</CreationDate></Bucket><Bucket><Name>s3-us-east-batcave-ops</Name><CreationDate>2013-05-29T21:00:29.000Z</CreationDate></Bucket><Bucket><Name>s3-us-east-batcave-production</Name><CreationDate>2011-08-29T17:17:19.000Z</CreationDate></Bucket><Bucket><Name>s3-us-east-batcave-public-assets</Name><CreationDate>2012-07-02T14:07:17.000Z</CreationDate></Bucket><Bucket><Name>s3-us-east-batcave-qa</Name><CreationDate>2012-07-13T18:24:03.000Z</CreationDate></Bucket></Buckets></ListAllMyBucketsResult>"""
    val buckets = S3BucketListing(listBucketsXML)

    assert(buckets.isSuccess)

    val S3BucketListing(bkts) = buckets.get

    assert(bkts.size === 14)
    assert(bkts.head.bucketName === "y-so-serious")
  }

}
