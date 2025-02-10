package ai.dragonfly.quickandderby

import ai.dragonfly.quickandderby.table.MapTable

import scala.util.{Failure, Success}


object Demo extends App {
  System.setSecurityManager(null)
  System.setProperty("derby.system.home", "./derby")
  val ds = DerbyDataSource(DerbyConfig("jdbc:derby:demo;create=true", "demo", "demo"))

  val moi: Long = 265395001406521351L

  val testTable = new MapTable[Long, String]("Kurosawa", ds)
  testTable.put(moi, "Hidden Fortress") match {
    case Success(value) => println(s"$value")
    case Failure(e) => println(e)
  }

  println( testTable.get(moi) )

  val testTable1 = new MapTable[Long, String]("com.whatever.Foo:0.1", ds)
  testTable1.put(moi, "Oh, we fail, don't we fail?") match {
    case Success(value) => println(s"$value")
    case Failure(e) => println(e.printStackTrace())
  }

}
