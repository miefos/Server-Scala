package clicker.database

trait Database {

  def playerExists(username: String): Boolean
  def createPlayer(username: String): Unit
  def saveGameState(username: String, gameState: String): Unit
  def loadGameState(username: String): String

}
