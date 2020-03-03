package clicker.model

import akka.actor.{Actor, ActorRef}
import clicker.{BuyEquipment, ClickGold, GameState, Update}


////
////
//
//
// No Timer implemented (for objective 2)
//
//
////
////




class GameActor(username: String, database: ActorRef) extends Actor {

  val game: Game = new Game (username, database)

  override def receive: Receive = {
    case ClickGold =>
      println("Gold clicked.")
      game.increaseClickGold()
    case be: BuyEquipment =>
      println(be.equipmentId + " bought")
      game.buyEquipment(be.equipmentId)
    case Update =>
      game.increaseIdleGold()
      sender() ! GameState(game.getStringGameState)
  }

}
