package clicker.database

import scala.io.Source

class testDatabase extends Database {

  var allPlayers: Map[String, String] = Map() // username -> gameState

  override def playerExists(username: String): Boolean = {
    if (allPlayers.contains(username)) {
      true
    } else {
      false
    }
  }

  override def createPlayer(username: String): Unit = {
    if (!playerExists(username)) {
      val newGame: String = Source.fromFile("newGame.json").mkString
        .replace("USERNAME", username)
        .replace("TIMESTAMP", System.nanoTime().toString)
      allPlayers += (username -> newGame)
    } else {
      println("Player does not exist (creating player in testDatabase)")
    }
  }

  override def saveGameState(username: String, gameState: String): Unit = {
    if (playerExists(username)) {
      allPlayers += (username -> gameState)
    } else {
      println("Player does not exist (saving game state in testDatabase)")
    }
  }

  override def loadGameState(username: String): String = {
    if (playerExists(username)) {
      allPlayers(username)
    } else {
      println("Player does not exist (loading game state in testDatabase)")
      ""
    }
  }

}
