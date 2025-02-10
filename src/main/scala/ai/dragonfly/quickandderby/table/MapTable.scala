package ai.dragonfly.quickandderby.table

import ai.dragonfly.quickandderby.column.DerbyType
import ai.dragonfly.quickandderby.column.DerbyType.TypeMap

import java.sql.{Connection, PreparedStatement, ResultSet, SQLException}
import javax.sql.DataSource
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}


object MapTable {

  def ensureTable[K, V](ds: DataSource, name: String)(implicit keyTag:ClassTag[K], valueTag:ClassTag[V]): Unit = {
    if (name.matches("[0-9a-zA-Z\\.\\_\\:]+")) {
      val c = ds.getConnection
      println(s"CREATE TABLE \"$name\" (id ${TypeMap(keyTag.runtimeClass).derbyName} NOT NULL, value ${TypeMap(valueTag.runtimeClass).derbyName} NOT NULL, PRIMARY KEY (id))")
      val createTable: PreparedStatement = c.prepareStatement(s"CREATE TABLE \"$name\" (id ${TypeMap(keyTag.runtimeClass).derbyName} NOT NULL, value ${TypeMap(valueTag.runtimeClass).derbyName} NOT NULL, PRIMARY KEY (id))")
      if (!c.getMetaData.getTables(null, null, name, null).next()) {
        createTable.execute()
        createTable.close()
        println("Created Table: " + name)
      } else {
        println("Table: " + name + " exists.")
      }
      c.close()
    } else throw new java.sql.SQLException(s"Invalid table name: $name")
  }

}

class MapTable[K, V](name: String, ds: DataSource)(implicit keyTag:ClassTag[K], valueTag:ClassTag[V]) {

  MapTable.ensureTable[K, V](ds, name)

  val keyType: DerbyType[K] = TypeMap(keyTag.runtimeClass).asInstanceOf[DerbyType[K]]
  val valueType: DerbyType[V] = TypeMap(valueTag.runtimeClass).asInstanceOf[DerbyType[V]]

  val getSelectQuery = s"""SELECT * FROM "$name" WHERE id = ? FETCH FIRST ROW ONLY"""

  def get(key: K): Try[V] = {
    var c: Connection = null
    var select: PreparedStatement = null
    var rs: ResultSet = null

    var returnValue: Try[V] = null
    try {
      c = ds.getConnection
      select = c.prepareStatement(getSelectQuery)
      keyType.setValue(select, 1, key)
      rs = select.executeQuery()

      returnValue = if (rs.next()) Try[V](
        valueType.getValue(rs)
      )
      else Failure(new SQLException(s"$name table has no record associated with key: $key."))
    } catch {
      case e: Throwable => returnValue = Failure(e)
    } finally {
      try { if (rs != null) rs.close() } catch { case e: Throwable => }
      try { if (select != null) select.close() } catch { case e: Throwable => }
      try { if (c != null) c.close() } catch { case e: Throwable => }
    }
    returnValue
  }

  def put(key: K, value: V): Try[V] = {

      var c: Connection = null
      var psWrite: PreparedStatement = null

      var returnValue: Try[V] = null

      try {
        c = ds.getConnection
        get(key) match {
          case Success(s: V) =>
            // Record Exists.
            returnValue = if ( s.equals(value) ) {
              Try[V](s) // no change, why write?
            } else {
              // Update existing record.
              psWrite = c.prepareStatement(s"UPDATE $name SET value = ? WHERE id = ?")
              valueType.setValue(psWrite, 1, value)
              keyType.setValue(psWrite, 2, key)
              if (psWrite.executeUpdate() > 0) Try[V](value) else Failure(new SQLException(s"Could not UPDATE $key -> $value in MapTable: $name."))
            }
          case Failure(_) =>
            // insert new record
            psWrite = c.prepareStatement(s"""INSERT INTO "$name" (id, value) VALUES (?, ?)""")
            keyType.setValue(psWrite, 1, key)
            valueType.setValue(psWrite, 2, value)
            returnValue = if (psWrite.executeUpdate() > 0) Try[V](value) else Failure(new SQLException(s"Could not INSERT $key -> $value in MapTable: $name."))
        }

      } catch {
        case e: Throwable => returnValue = Failure(e)
      } finally {
        try { if (psWrite != null) psWrite.close() } catch { case e: Throwable => }
        try { if (c != null) c.close() } catch { case e: Throwable => }
      }
      returnValue
  }

  override def toString: String = s"DerbySource($name)"
}
