package jp.co.bizreach

import com.amazonaws.services.dynamodbv2.model.Condition
import scala.language.implicitConversions

package object dynamodb4s {

  trait DynamoDataType[T] {
    def convert(value: Any): T
  }

  implicit val IntDynamoType = new DynamoDataType[Int]{
    def convert(value: Any): Int = value match {
      case Some(x: Int)    => x
      case Some(x: Long)   => x.toInt
      case Some(x: Double) => x.toInt
      case Some(x: Float)  => x.toInt
      case Some(x: Any)    => x.toString.toInt
      case None            => 0
      case x: Int          => x
      case x: Long         => x.toInt
      case x: Double       => x.toInt
      case x: Float        => x.toInt
      case x               => x.toString.toInt
    }
  }
  implicit val LongDynamoType = new DynamoDataType[Long]{
    def convert(value: Any): Long = value match {
      case Some(x: Int)    => x.toLong
      case Some(x: Long)   => x
      case Some(x: Double) => x.toLong
      case Some(x: Float)  => x.toLong
      case Some(x)         => x.toString.toLong
      case None            => 0
      case x: Int          => x.toLong
      case x: Long         => x
      case x: Double       => x.toLong
      case x: Float        => x.toLong
      case x               => x.toString.toLong
    }
  }
  implicit val DoubleDynamoType = new DynamoDataType[Double]{
    def convert(value: Any): Double = value match {
      case Some(x: Int)    => x.toDouble
      case Some(x: Long)   => x.toDouble
      case Some(x: Double) => x
      case Some(x: Float)  => x.toDouble
      case Some(x: Any)    => x.toString.toDouble
      case None            => 0
      case x: Int          => x
      case x: Long         => x.toDouble
      case x: Double       => x
      case x: Float        => x.toDouble
      case x               => x.toString.toDouble
    }
  }
  implicit val FloatDynamoType = new DynamoDataType[Float]{
    def convert(value: Any): Float = value match {
      case Some(x: Int)    => x.toFloat
      case Some(x: Long)   => x.toFloat
      case Some(x: Double) => x.toFloat
      case Some(x: Float)  => x
      case Some(x: Any)    => x.toString.toFloat
      case None            => 0
      case x: Int          => x
      case x: Long         => x.toFloat
      case x: Double       => x.toFloat
      case x: Float        => x
      case x               => x.toString.toFloat
    }
  }
  implicit val StringDynamoType = new DynamoDataType[String]{
    def convert(value: Any): String = value match {
      case Some(x) => x.toString
      case None    => null
      case x       => x.toString
    }
  }
  implicit val IntListDynamoType = new DynamoDataType[List[Int]]{
    def convert(value: Any): List[Int] = value.asInstanceOf[List[Any]].map(IntDynamoType.convert)
  }
  implicit val LongListDynamoType = new DynamoDataType[List[Long]]{
    def convert(value: Any): List[Long] = value.asInstanceOf[List[Any]].map(LongDynamoType.convert)
  }
  implicit val DoubleListDynamoType = new DynamoDataType[List[Double]]{
    def convert(value: Any): List[Double] = value.asInstanceOf[List[Any]].map(DoubleDynamoType.convert)
  }
  implicit val FloatListDynamoType = new DynamoDataType[List[Float]]{
    def convert(value: Any): List[Float] = value.asInstanceOf[List[Any]].map(FloatDynamoType.convert)
  }
  implicit val StringListDynamoType = new DynamoDataType[List[String]]{
    def convert(value: Any): List[String] = value.asInstanceOf[List[Any]].map(StringDynamoType.convert)
  }

  trait DynamoKey { val name: String }
  trait DynamoProperty[T] {
    val name: String
    def convert(value: Any): T
  }
  case class DynamoHashKey[T](name: String)(implicit t: DynamoDataType[T]) extends DynamoKey with DynamoProperty[T] {
    def convert(value: Any): T = t.convert(value)
  }
  case class DynamoRangeKey[T](name: String)(implicit t: DynamoDataType[T]) extends DynamoKey with DynamoProperty[T] {
    def convert(value: Any): T = t.convert(value)
  }
  case class DynamoAttribute[T](name: String)(implicit t: DynamoDataType[T]) extends DynamoProperty[T] {
    def convert(value: Any): T = t.convert(value)
  }

  implicit def condition2seq(x: (DynamoKey, Condition)): Seq[(DynamoKey, Condition)] = Seq(x)
  implicit def property2seq(x: DynamoProperty[_]): Seq[DynamoProperty[_]] = Seq(x)

}
