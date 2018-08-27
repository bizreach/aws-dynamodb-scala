package jp.co.bizreach.dynamodb4s

import org.scalatest._

import awscala._
import dynamodbv2._

// TODO Is it possible to create table from this definition?
object Members extends DynamoTable {
  val table   = "members"
  val country = DynamoHashKey[String]("country")
  val id      = DynamoRangeKey[Int]("id")
  val name    = DynamoAttribute[String]("name")
  val age     = DynamoAttribute[Int]("age")
  val company = DynamoAttribute[String]("company")

  object companyIndex extends DynamoTable.SecondaryIndex {
    val index   = "companyIndex"
    val country = DynamoHashKey[String]("country")
    val company = DynamoRangeKey[String]("company")
  }
}

case class Member(
  country: String,
  id: Int,
  name: String,
  age: Int,
  company: Option[String]
)


class DynamoDBTest extends FunSuite with BeforeAndAfter {

  implicit val db = DynamoDB.local()

  before {
    db.createTable(
      name    = "members",
      hashPK  = "country" -> AttributeType.String,
      rangePK = "id"      -> AttributeType.Number,
      otherAttributes = Seq("company" -> AttributeType.String),
      indexes = Seq(LocalSecondaryIndex(
        name       = "companyIndex",
        keySchema  = Seq(KeySchema("country", KeyType.Hash), KeySchema("company", KeyType.Range)),
        projection = Projection(ProjectionType.Include, Seq("id", "country", "name", "age", "company"))
      ))
    )

    val table: Table = db.table("members").get
    table.put("Japan", 1, "name" -> "Takezoe", "age" -> 37, "company" -> "BizReach")
    table.put("Japan", 2, "name" -> "Shimamoto",   "age" -> 33, "company" -> "BizReach")
  }

  after {
    db.table("members") foreach db.delete
  }


  test("Query using secondary index"){
    val result = Members.query.filter2(_.companyIndex){ t =>
      t.country -> DynamoDBCondition.eq("Japan") :: t.company -> DynamoDBCondition.eq("BizReach") :: Nil
    }.list[Member]

    assert(result.size == 2)
    val m1 = result.find(_.id == 1)
    assert(m1 contains Member("Japan", 1, "Takezoe", 37, Some("BizReach")))
    val m2 = result.find(_.id == 2)
    assert(m2 contains Member("Japan", 2, "Shimamoto", 33, Some("BizReach")))
  }

  test("Query using DynamoDBCondition"){
    val result = Members.query
      .select { t => t.id :: t.country :: t.name :: t.company :: Nil }
      .filter(_.country -> DynamoDBCondition.eq("Japan"))
      .limit(100000)
      .map { (t, x) =>
        (x.get(t.id), x.get(t.country), x.get(t.name), x.get(t.company))
      }

    assert(result.size == 2)
    val m1 = result.find(_._1 == 1)
    assert(m1 contains (1, "Japan", "Takezoe", "BizReach"))
    val m2 = result.find(_._1 == 2)
    assert(m2 contains (2, "Japan", "Shimamoto", "BizReach"))
  }

  test("Scan"){
    val result = new collection.mutable.ArrayBuffer[Member]()
    Members.scan.filter("company = :company", "company" -> "BizReach").as[Member]{ x =>
      result += x
    }

    assert(result.size == 2)
    assert(result.exists(_.id == 1))
    assert(result.exists(_.id == 2))
  }

  test("Insert"){
    Members.put(Member("U.S.", 3, "Alice", 23, Some("Google")))
    Members.put(Member("U.S.", 4, "Bob", 36, None))

    val alice = Members.query.filter { t =>
      t.country -> DynamoDBCondition.eq("U.S.") :: t.id -> DynamoDBCondition.eq(3) :: Nil
    }.firstOption[Member]
    assert(alice contains Member("U.S.", 3, "Alice", 23, Some("Google")))

    val bob = Members.query.filter { t =>
      t.country -> DynamoDBCondition.eq("U.S.") :: t.id -> DynamoDBCondition.eq(4) :: Nil
    }.firstOption[Member]
    assert(bob contains Member("U.S.", 4, "Bob", 36, None))
  }

  test("Update"){
    Members.putAttributes("Japan", 1){ t =>
      t.name -> "Naoki" :: Nil
    }

    val m = Members.query.filter { t =>
      t.country -> DynamoDBCondition.eq("Japan") :: t.id -> DynamoDBCondition.eq(1) :: Nil
    }.firstOption[Member]
    assert(m contains Member("Japan", 1, "Naoki", 37, Some("BizReach")))
  }

  test("Delete"){
    Members.delete("Japan", 1)

    val m = Members.query.filter { t =>
      t.country -> DynamoDBCondition.eq("Japan") :: t.id -> DynamoDBCondition.eq(1) :: Nil
    }.firstOption[Member]
    assert(m.isEmpty)
  }

}
