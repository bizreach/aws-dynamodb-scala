package jp.co.bizreach.dynamodb4s

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import scala.collection.JavaConverters._

import reflect.ClassTag
import DynamoTable._

/**
 * Trait for Dynamo table definition.
 */
trait DynamoTable {

  val table: String
  type T = this.type

  /**
   * Delete item by the hash key.
   */
  def delete(hashPk: Any)(implicit db: awscala.dynamodbv2.DynamoDB): Unit = {
    db.deleteItem(db.table(table).get, hashPk)
  }

  /**
   * Delete item by the hash key and the range key.
   */
  def delete(hashPk: Any, rangePk: Any)(implicit db: awscala.dynamodbv2.DynamoDB): Unit = {
    db.deleteItem(db.table(table).get, hashPk, rangePk)
  }

  /**
   * Create or update the given item
   */
  def put[E <: AnyRef](entity: E)(implicit db: awscala.dynamodbv2.DynamoDB, c: ClassTag[E]): Unit = {
    val tableInfo = getTableInfo(this)

    if(tableInfo.hashKey.isDefined && tableInfo.rangeKey.isDefined){
      db.put(db.table(table).get,
        getValueFromEntity(entity, tableInfo.hashKey.get),
        getValueFromEntity(entity, tableInfo.rangeKey.get),
        tableInfo.attributes.map(p => p.name -> getValueFromEntity(entity, p)).filter(_ != null): _*)
    } else if(tableInfo.hashKey.isDefined){
      db.put(db.table(table).get,
        getValueFromEntity(entity, tableInfo.hashKey.get),
        tableInfo.attributes.map(p => p.name -> getValueFromEntity(entity, p)): _*)
    } else {
      throw new DynamoDBException(s"Primary key is not defined for ${entity.getClass.getName}")
    }
  }

  /**
   * Update specified attributes by the hash key.
   */
  def putAttributes(hashPk: Any)(f: T => Seq[(DynamoAttribute[_], Any)])(implicit db: awscala.dynamodbv2.DynamoDB): Unit = {
    db.table(table).get.putAttributes(hashPk, f(this).map { case (key, value) => (key.name, value) })
  }

  /**
   * Update specified attributes by the hash key and the range key.
   */
  def putAttributes(hashPk: Any, rangePk: Any)(f: T => Seq[(DynamoAttribute[_], Any)])(implicit db: awscala.dynamodbv2.DynamoDB): Unit = {
    db.table(table).get.putAttributes(hashPk, rangePk, f(this).map { case (key, value) => (key.name, value) })
  }

  def scan(): DynamoScanBuilder[T] = DynamoScanBuilder(this, _ => Nil)

  def query(): DynamoQueryBuilder[T] = DynamoQueryBuilder(this, _ => Nil, _ => Nil)

}

object DynamoTable {

  class DynamoRow(attrs: java.util.Map[String, AttributeValue]){
    def get[T](property: DynamoProperty[T]) = property.convert(getAttributeValue(attrs.get(property.name)))
  }

  private[dynamodb4s] case class TableInfo(
    hashKey: Option[DynamoHashKey[_]], rangeKey: Option[DynamoRangeKey[_]], attributes: Seq[DynamoAttribute[_]]){

    def getDynamoProperty(name: String): DynamoProperty[_] =
      (Seq[Option[DynamoProperty[_]]](hashKey, rangeKey).flatten ++ attributes).find(_.name == name).get
  }

  private[dynamodb4s] def getTableInfo(table: DynamoTable): TableInfo = {
    var hashKey: Option[DynamoHashKey[_]] = None
    var rangeKey: Option[DynamoRangeKey[_]] = None
    var attributes: scala.collection.mutable.ListBuffer[DynamoAttribute[_]]
      = new scala.collection.mutable.ListBuffer[DynamoAttribute[_]]

    table.getClass.getDeclaredFields.foreach { f =>
      f.setAccessible(true)
      if(f.getType.isAssignableFrom(classOf[DynamoHashKey[_]])){
        hashKey = Some(f.get(table).asInstanceOf[DynamoHashKey[_]])
      }
      else if(f.getType.isAssignableFrom(classOf[DynamoRangeKey[_]])){
        rangeKey = Some(f.get(table).asInstanceOf[DynamoRangeKey[_]])
      }
      else if(f.getType.isAssignableFrom(classOf[DynamoAttribute[_]])){
        attributes += f.get(table).asInstanceOf[DynamoAttribute[_]]
      }
    }

    TableInfo(hashKey, rangeKey, attributes.toSeq)
  }

  private[dynamodb4s] def getValueFromEntity(entity: AnyRef, property: DynamoProperty[_]): Any = {
    val f = entity.getClass.getDeclaredField(property.name)
    f.setAccessible(true)
    property.convert(f.get(entity))
  }

  private[dynamodb4s] def getAttributeValue(attr: AttributeValue): Any = {
    // TODO Support binary type
    //    if(attr.getB != null){
    //      attr.getB
    //    } else if(attr.getBS != null){
    //      attr.getBS
    //    } else
    if(attr == null) {
      null
    } else if(attr.getN != null){
      attr.getN
    } else if(attr.getNS != null){
      attr.getNS.asScala
    } else if(attr.getS != null){
      attr.getS
    } else if(attr.getSS != null){
      attr.getSS.asScala
    } else {
      null
    }
  }

  trait SecondaryIndex {
    val index: String
  }
}
