package jdbc

import java.sql.DriverManager
import java.sql.Connection

/**
 * A Scala JDBC connection example by Alvin Alexander,
 * https://alvinalexander.com
 */
object ScalaJdbcConnectSelect {

  def main(args: Array[String]) {
    // connect to the database named "mysql" on the localhost
    val driver = "com.mysql.cj.jdbc.Driver"
    val url = "jdbc:mysql://localhost:3306/clickerdatabase"
    val username = "root"
    val password = "root"

    // there's probably a better way to do this
    var connection:Connection = null

    try {
      // make the connection
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)

      // create the statement, and run the select query
      val statement = connection.createStatement()
      val resultSet = statement.executeQuery("SELECT heyheyhey, hey FROM testtees")
      while ( resultSet.next() ) {
        val username = resultSet.getString("heyheyhey")
        val gameState = resultSet.getString("hey")
        println("col1 = " + username + ", col2 = " + gameState)
      }
    } catch {
      case e => e.printStackTrace
    }
    connection.close()
  }

}
