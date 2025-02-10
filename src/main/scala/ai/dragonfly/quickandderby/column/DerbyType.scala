package ai.dragonfly.quickandderby.column

import java.sql.{PreparedStatement, ResultSet}

case class DerbyType[T](derbyName: String, scalaType: Class[_], setValue: (PreparedStatement, Int, T) => Unit, getValue: ResultSet => T)

object DerbyType {

  val TypeMap: Map[Class[_], DerbyType[_]] = Map[Class[_], DerbyType[_]](
    classOf[Int] -> DerbyType[Int]("INTEGER", classOf[Int], (statement: PreparedStatement, index: Int, value: Int) => statement.setInt(index, value), (resultSet: ResultSet) => resultSet.getInt("value")),
    classOf[Long] -> DerbyType[Long]("BIGINT", classOf[Long], (statement: PreparedStatement, index: Int, value: Long) => statement.setLong(index, value), (resultSet: ResultSet) => resultSet.getLong("value")),
    classOf[String] -> DerbyType[String]("CLOB", classOf[String], (statement: PreparedStatement, index: Int, value: String) => statement.setString(index, value), (resultSet: ResultSet) => resultSet.getString("value")),
    classOf[Float] -> DerbyType[Float]("FLOAT", classOf[Float], (statement: PreparedStatement, index: Int, value: Float) => statement.setFloat(index, value), (resultSet: ResultSet) => resultSet.getFloat("value")),
    classOf[Double] -> DerbyType[Double]("DOUBLE", classOf[Double], (statement: PreparedStatement, index: Int, value: Double) => statement.setDouble(index, value), (resultSet: ResultSet) => resultSet.getDouble("value")),
    classOf[Array[Byte]] -> DerbyType[Array[Byte]]("BLOB", classOf[Array[Byte]], (statement: PreparedStatement, index: Int, value: Array[Byte]) => statement.setBytes(index, value), (resultSet: ResultSet) => resultSet.getBytes("value"))
  )

  println(TypeMap)

}