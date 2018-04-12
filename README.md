aws-dynamodb-scala
==========

Scala client for Amazon DynamoDB

## How to use

Add a following dependency into your `build.sbt` at first.

```scala
libraryDependencies += "jp.co.bizreach" %% "aws-dynamodb-scala" % "0.0.7"
```

Then you can use aws-dynamodb-scala in your code.

```scala
import jp.co.bizreach.dynamodb4s._
import awscala.dynamodbv2.DynamoDB

// for local environment
implicit val db = DynamoDB.local()

// for AWS environment
implicit val db = DynamoDB.apply(accessKeyId = "xxx", secretAccessKey = "xxx")
```

Create table definition as below:

```scala
object Members extends DynamoTable {
  val table   = "members"
  val country = DynamoHashKey[String]("country")
  val id      = DynamoRangeKey[Int]("id")
  val name    = DynamoAttribute[String]("name")
  val age     = DynamoAttribute[Int]("age")
  val company = DynamoAttribute[String]("company")
}

// Case class is optional
case class Member(
  val country: String,
  val id: Int,
  val name: String,
  val age: Int,
  val company: Option[String]
)
```

### Put

```scala
// Put by case class
Members.put(Member("Japan", 1, "Naoki Takezoe", 30, Some("BizReach")))

// Update only specified properties
Members.putAttributes("Japan", 1){ t =>
  t.name -> "Takako Shimamoto" :: t.age  -> 25 :: Nil
}
```

### Query

```scala
// Query with case class mapping
val list: Seq[Member] = Members.query.filter { t =>
  t.country -> DynamoDBCondition.eq("Japan") :: t.id -> DynamoDBCondition.eq(1) :: Nil
}.list[Member]

// Query with manual mapping
val list: Seq[(String, Int)] = Members.query
  .select { t => t.name :: t.age :: Nil }
  .filter { t => t.country -> DynamoDBCondition.eq("Japan") :: t.id -> DynamoDBCondition.eq(1) :: Nil }
  .limit(100000)
  .map { (t, x) =>
    (x.get(t.name), x.get(t.age))
  }
```

### Secondary index

```scala
object Members extends DynamoTable {
  val table   = "members"
  val id      = DynamoHashKey[Int]("id")
  val country = DynamoRangeKey[String]("country")
  val name    = DynamoAttribute[String]("name")
  val age     = DynamoAttribute[Int]("age")
  val company = DynamoAttribute[String]("company")
  object companyIndex extends DynamoTable.SecondaryIndex {
    val index   = "companyIndex"
    val country = DynamoHashKey[String]("country")
    val company = DynamoRangeKey[String]("company")
  }  
}

val list: Seq[Member] = Members.query.filter2(_.companyIndex){ t =>
  t.country -> DynamoDBCondition.eq("Japan") :: t.company -> DynamoDBCondition.eq("BizReach") :: Nil
}.list[Member]
```

### Scan

```scala
Members.scan.filter("company = :company", "company" -> "BizReach").as[Member]{ x =>
  println(x)
}
```
