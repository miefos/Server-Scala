package clicker.model

import akka.actor.{Actor, ActorRef}
import clicker._

class GameActor(username: String, database: ActorRef) extends Actor {

  val game: Game = new Game (username, database)
  database ! StartedGame(username)

  override def receive: Receive = {
    case ClickGold =>
//      println("Gold clicked")
      game.increaseClickGold()
    case be: BuyEquipment =>
      game.buyEquipment(be.equipmentId)
    case Update =>
      game.increaseIdleGold()
      sender() ! GameState(game.getStringGameState)
    case Save =>
//      println("Game State saved (1/3)...")
      database ! SaveGame(username, game.getStringGameState)
    // This case happens if username already existed in DB
    case data: GameState =>
      game.updateFromDB(data.gameState)
  }

}
