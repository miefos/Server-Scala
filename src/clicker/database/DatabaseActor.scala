package clicker.database

import akka.actor.Actor
import clicker._

/***
  * @param dbType Indicates the type of database to be used. Use "mySQL" to connect to a MySQL server, or "test" to
  *               use data structures in a new class that extends the Database trait.
  */
class DatabaseActor(dbType: String) extends Actor {

  val database: Database = dbType match {
    case "mySQL" => new MySQLDatabase()
    case "test" => new testDatabase()
  }

  override def receive: Receive = {
    case "mySQL" => new MySQLDatabase()
    case data: SaveGame =>
//      println("Game State saved (2/3)...")
      database.saveGameState(data.username, data.gameState)
    case game: StartedGame =>
      println("Game started (1/2) with username " + game.username)
      if (database.playerExists(game.username)) {
        println("Game started (2/2) with username " + game.username + " (player existed and retrieved data from DB)")
        sender() ! GameState(database.loadGameState(game.username))
      } else {
        println("Game started (2/2) with username " + game.username + " (new player created and inserted into DB)")
        database.createPlayer(game.username)
      }
  }
}
