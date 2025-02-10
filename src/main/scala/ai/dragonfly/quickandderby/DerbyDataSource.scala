package ai.dragonfly.quickandderby

import com.zaxxer.hikari._

case class DerbyConfig(
  jdbcUrl: String,
  userName: String,
  password: String,
  driverName: String = "org.apache.derby.jdbc.EmbeddedDriver"
) {
  def asHikariConfig: HikariConfig = {
    val conf:HikariConfig = new HikariConfig()
    conf.setDriverClassName(driverName)
    conf.setJdbcUrl(jdbcUrl)
    conf.setUsername(userName)
    conf.setPassword(password)
    conf
  }
}

object DerbyDataSource {
  //System.setProperty("derby.system.home", "/path/to/derby/database")

  def apply(conf: DerbyConfig): HikariDataSource = {

    val ds = new HikariDataSource(conf.asHikariConfig)

    val c = ds.getConnection
    var s = c.createStatement()

    s = c.createStatement()
    s.close()
    c.close()
    ds
  }
}