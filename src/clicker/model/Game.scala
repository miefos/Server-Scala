package clicker.model

import akka.actor.ActorRef
import play.api.libs.json._

class Game (username: String, database: ActorRef){

  var lastUpdateTime: Long = System.nanoTime()

  var current_gold: Double = 0
  val shop: Map[String, Item] = Equipment.available
  var inventory: Map[String, Int] = Map()

  // Uses method getInventory
  def getStringGameState: String = {
    val returnObj: JsValue = JsObject(
      Seq(
        "username" -> JsString(username),
        "gold" -> JsNumber(current_gold),
        "lastUpdateTime" -> JsNumber(lastUpdateTime),
        "equipment" -> getInventory
      ))

    Json.stringify(returnObj)
  }

  def getInventory: JsValue = {
    var inv: Map[String, JsValue] = Map()

    // Iterate over all items and add to inv
    for (elem <- shop) {
      val itemID: String = elem._1
      val item: Item = elem._2
      val itemName: String = item.name
      val numOwned: Int = if (inventory.contains(itemID)) inventory(itemID) else 0
      val cost: Double = item.getPrice(numOwned)

      val itemJson: JsValue = JsObject(
          Seq(
            "id" -> JsString(itemID),
            "name" -> JsString(itemName),
            "numberOwned" -> JsNumber(numOwned),
            "cost" -> JsNumber(cost)
          )
        )
      inv += (itemID -> itemJson)
    }

    Json.toJson(inv)
  }

  def increaseClickGold(): Unit = {
    var updateGold: Double = 1
    for (item <- inventory) {
      val itemID = item._1
      val numOfAcquired = item._2
      val goldPerClick = shop(itemID).goldPerClick
      updateGold += goldPerClick*numOfAcquired
    }
    current_gold += updateGold
    println("Current gold updated by " + updateGold + " (increased due to a click)")
    println("Current gold now is " + current_gold)
  }

  def increaseIdleGold(): Unit = {
    var updateGold: Double = 0

    for (item <- inventory) {
      val itemID = item._1
      val numOfAcquired = item._2
      val goldPerSecond = shop(itemID).idleGold
      updateGold += goldPerSecond * numOfAcquired
    }

    current_gold += updateGold
    println("Current gold updated by " + updateGold + " (increased due to idling)")
    println("Current gold now is " + current_gold)
  }

  def buyEquipment (id: String): Unit = {
    if (shop.contains(id)){
      // Get item and price
      val itemToBuy: Item = shop(id)
      var numAcquired: Int = 0
      if (inventory.contains(id)) {
        numAcquired = inventory(id)
      }
      val price: Double = itemToBuy.getPrice(numAcquired)

      // Check if gold is enough and buy
      if (current_gold >= price) {
        current_gold -= price
        if (inventory.contains(id)) {
          inventory += (id -> (inventory(id) + 1))
        } else {
          inventory += (id -> 1)
        }
        println(id + " bought for " + price + ", gold left " + current_gold)
      } else {
        println("Could not buy " + id + " because not enough money... (you have " + current_gold + " but needed " + price + ")")
      }
    }
  }

}
