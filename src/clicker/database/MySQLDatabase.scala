package clicker.database

import java.sql.{Connection, DriverManager, ResultSet}

import scala.io.Source

class MySQLDatabase extends Database {

  val url = "jdbc:mysql://localhost:3306/clickerdatabase"
  val username = "root"
  val password = "root"
  var connection: Connection = DriverManager.getConnection(url, username, password)

  setupTable()

  def setupTable(): Unit = {
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS players (username TEXT, gameState TEXT)")
  }


  def playerExists(username: String): Boolean = {
    val statement = connection.prepareStatement("SELECT * FROM players WHERE username=?")
    statement.setString(1, username)
    val result: ResultSet = statement.executeQuery()
    result.next()
  }


  def createPlayer(username: String): Unit = {
    val statement = connection.prepareStatement("INSERT INTO players VALUE (?, ?)")
    statement.setString(1, username)
    val newGame: String = Source.fromFile("newGame.json").mkString
      .replace("USERNAME", username)
      .replace("TIMESTAMP", System.nanoTime().toString)
    statement.setString(2, newGame)
    statement.execute()
  }


  def saveGameState(username: String, gameState: String): Unit = {
    val statement = connection.prepareStatement("UPDATE players SET gameState = ? WHERE username = ?")
    statement.setString(1, gameState)
    statement.setString(2, username)
    statement.execute()
//    println("Game State saved (3/3)...")
  }


  def loadGameState(username: String): String = {
    val statement = connection.prepareStatement("SELECT * FROM players WHERE username=?")
    statement.setString(1, username)
    val result: ResultSet = statement.executeQuery()
    result.next()
    result.getString("gameState")
  }


}
