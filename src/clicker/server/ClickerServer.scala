package clicker.server

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import clicker.database.DatabaseActor
import clicker.model.{Equipment, Item}
import clicker.{GameState, Save, SaveGame, SaveGames, Update, UpdateGames}
import com.corundumstudio.socketio.{Configuration, SocketIOClient, SocketIOServer}
import play.api.libs.json.{JsObject, JsValue, Json}

import scala.io.Source

/** *
  * @param database      Reference to the database actor
  * @param configuration Custom configuration of the game (Used in Bonus Objective. Pass empty string before bonus)
  */
class ClickerServer(val database: ActorRef, val configuration: String) extends Actor {


  // If Configuration provided, set up configuration
  if (configuration != "") {
    var shopMap: Map[String, Item] = Map()

    val parsed: JsValue = Json.parse(configuration)
    Equipment.currency = (parsed \ "currency").as[String]
    val equipment: List[JsValue] = (parsed \ "equipment").as[List[JsValue]]
    for (item <- equipment) {
      val itemID: String = (item \ "id").as[String]
      val itemName: String = (item \ "name").as[String]
      val GPC: Int = (item \ "incomePerClick").as[Int]
      val GPS: Int = (item \ "incomePerSecond").as[Int]
      val initialCost: Double = (item \ "initialCost").as[Double]
      val priceExponent: Double = (item \ "priceExponent").as[Double]
      shopMap += (itemID -> new Item(itemID, itemName, initialCost, priceExponent, GPC, GPS))
    }

    Equipment.available = shopMap
  }

  // Used to store online users
  var currentPlayers: Map[String, ActorRef] = Map()
  var currentPlayersStrToSocket: Map[String, SocketIOClient] = Map()
  var currentPlayersSocketToStr: Map[SocketIOClient, String] = Map()

  // Set up server
  val config: Configuration = new Configuration {
    setHostname("localhost")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("register", classOf[String], new RegisterNewUserListener(this))
  server.addEventListener("clickGold", classOf[Nothing], new ClickGoldListener(this))
  server.addEventListener("buy", classOf[String], new BuyListener(this))
  server.addEventListener("hack100", classOf[Nothing], new hackListener(this, 100))
  server.addEventListener("hack1000", classOf[Nothing], new hackListener(this, 1000))
  server.start()

  // Actor behaviour
  override def receive: Receive = {
    case UpdateGames =>
      for (player <- currentPlayers) {
        val userActorRef: ActorRef = player._2
        userActorRef ! Update
      }
    case SaveGames =>
      for (player <- currentPlayers) {
        val userActorRef: ActorRef = player._2
        userActorRef ! Save
      }
    case gs: GameState =>
      val username = (Json.parse(gs.gameState) \ "username").as[String]
      if (currentPlayersStrToSocket.contains(username)){
        val userSocket: SocketIOClient = currentPlayersStrToSocket(username)
        userSocket.sendEvent("gameState", gs.gameState)
      }
  }



  // Comment in server.stop() to stop your web socket server when the actor system shuts down. This will free
  // the port and allow to to test again immediately. Note that this doesn't work if you stop your server through
  // IntelliJ. If you use IntelliJ's stop button you will have to wait for the port to be freed before restarting
  // your server. By using the TestServer test suite and this method to stop the server you can avoid having to
  // wait before restarting while testing
  override def postStop(): Unit = {
    println("stopping server")
//    server.stop()
  }

}

object ClickerServer {

  def main(args: Array[String]): Unit = {
    val actorSystem = ActorSystem()

    import actorSystem.dispatcher
    import scala.concurrent.duration._


    val db = actorSystem.actorOf(Props(classOf[DatabaseActor], "mySQL"))
//    val server = actorSystem.actorOf(Props(classOf[ClickerServer], db, ""))
//    val server = actorSystem.actorOf(Props(classOf[ClickerServer], db, Source.fromFile("customGame.json").mkString))
    val server = actorSystem.actorOf(Props(classOf[ClickerServer], db, Source.fromFile("customGame2.json").mkString))

    actorSystem.scheduler.schedule(0.milliseconds, 100.milliseconds, server, UpdateGames)
    actorSystem.scheduler.schedule(0.milliseconds, 1000.milliseconds, server, SaveGames)
  }

}
