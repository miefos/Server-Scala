package clicker.server

import akka.actor.{ActorRef, PoisonPill, Props}
import clicker.{BuyEquipment, ClickGold, Save}
import clicker.model.GameActor
import com.corundumstudio.socketio.{AckRequest, SocketIOClient}
import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}

// Object initialized when someone is disconnected from server
class DisconnectionListener(server: ClickerServer) extends DisconnectListener {
  override def onDisconnect(socket: SocketIOClient): Unit = {
    if (server.currentPlayersSocketToStr.contains(socket)){
      val username: String = server.currentPlayersSocketToStr(socket)
      val userActorRef: ActorRef = server.currentPlayers(username)
      userActorRef ! PoisonPill
      server.currentPlayers -= username
      server.currentPlayersSocketToStr -= socket
      server.currentPlayersStrToSocket -= username
      println(username + " disconnected.")
    }
  }
}

// Register New User in Chat
class RegisterNewUserListener (server: ClickerServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    println(username + " logged in")
    val newActor: ActorRef = server.context.actorOf(Props(classOf[GameActor], username, server.database))
    server.currentPlayers += (username -> newActor)
    server.currentPlayersSocketToStr += (socket -> username)
    server.currentPlayersStrToSocket += (username -> socket)
    if (server.configuration != "") {
      socket.sendEvent("init", server.configuration)
    }
  }
}

// Click Gold Listener
class ClickGoldListener (server: ClickerServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, data: Nothing, ackRequest: AckRequest): Unit = {
    if (server.currentPlayersSocketToStr.contains(socket)) {
      val username: String = server.currentPlayersSocketToStr(socket)
      val userActorRef: ActorRef = server.currentPlayers(username)
      userActorRef ! ClickGold
//      println("Click gold listener worked!")
    } else {
      println("No such player registered. Cannot click gold.")
      println("Curernt players Map[Username -> ActorRef]" + server.currentPlayers)
      println("Curernt players Map[Socket -> Username]" + server.currentPlayersSocketToStr)
      println("Curernt players Map[Username -> Socket]" + server.currentPlayersStrToSocket)
    }
  }
}

// Buy Listener
class BuyListener (server: ClickerServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, equipmentID: String, ackRequest: AckRequest): Unit = {
    if (server.currentPlayersSocketToStr.contains(socket)) {
      val username: String = server.currentPlayersSocketToStr(socket)
      val userActorRef: ActorRef = server.currentPlayers(username)
      userActorRef ! BuyEquipment(equipmentID)
//      println("Buy Listener worked!")
    }
  }
}

// Hack Listeners
class hackListener (server: ClickerServer, count: Int) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, nothing: Nothing, ackRequest: AckRequest): Unit = {
    if (server.currentPlayersSocketToStr.contains(socket)) {
      val username: String = server.currentPlayersSocketToStr(socket)
      val userActorRef: ActorRef = server.currentPlayers(username)
      for (i <- 1 to count) {
        userActorRef ! ClickGold
      }
      //      println("Buy Listener worked!")
    }
  }
}