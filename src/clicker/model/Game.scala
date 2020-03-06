package clicker.model

import akka.actor.ActorRef
import play.api.libs.json._

class Game (var username: String, database: ActorRef){

  var lastUpdateTime: Long = System.nanoTime()

  var current_gold: Double = 0
  val shop: Map[String, Item] = Equipment.available
  var inventory: Map[String, Int] = Map()
  var leftTime: Double = 0
  var GPS: Double = 0 // Gold per second
  var GPC: Double = 1 // Gold per click


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

  // Updates when data got from DB (existing user logged in)
  def updateFromDB (data: String): Unit = {
    println("Updating data from DB")
    val parsed: JsValue = Json.parse(data)
    username = (parsed \ "username").as[String]
    current_gold = (parsed \ "gold").as[Double]
    lastUpdateTime = (parsed \ "lastUpdateTime").as[Long]
    val equipment: Map[String, JsValue] = (parsed \ "equipment").as[Map[String, JsValue]]
    for (itemMap <- equipment) {
      val item: JsValue = itemMap._2
      val itemID: String = (item \ "id").as[String]
      val numberOwned: Int = (item \ "numberOwned").as[Int]
      inventory += (itemID -> numberOwned)
    }
    println("New inventory is " + inventory)
    println("New current gold is " + current_gold)
  }

//  def increaseClickGold(): Unit = {
//    var updateGold: Double = 1
//    for (item <- inventory) {
//      val itemID = item._1
//      val numOfAcquired = item._2
//      val goldPerClick = shop(itemID).goldPerClick
//      updateGold += goldPerClick*numOfAcquired
//    }
//    current_gold += updateGold
////    println("Current gold updated by " + updateGold + " (increased due to a click)")
////    println("Current gold now is " + current_gold)
//  }

  def increaseClickGold(): Unit = {
    current_gold += GPC
  }

  def increaseIdleGold(): Unit = {
    val numOfSeconds: Double = calcUpdateTime()
    current_gold += GPS * numOfSeconds
  }

//  def increaseIdleGold(): Unit = {
//    val numOfSeconds: Double = calcUpdateTime()
//    var updateGold: Double = 0
//
//    for (item <- inventory) {
//      val itemID = item._1
//      val numOfAcquired = item._2
//      var goldPerSecond: Int = 0
//      if (shop.contains(itemID)) {
//        goldPerSecond = shop(itemID).idleGold
//      }
//      updateGold += goldPerSecond * numOfAcquired * numOfSeconds
//    }
//
////    println("...................." + numOfSeconds + " Passed......")
//
//    current_gold += updateGold
////    println("Current gold updated by " + updateGold + " (increased due to idling)")
////    println("Current gold now is " + current_gold)
//  }

  def calcUpdateTime(): Double = {
    val updateNanoSeconds: Long = System.nanoTime() - lastUpdateTime
    lastUpdateTime = System.nanoTime()
    val DoubleSeconds: Double = (updateNanoSeconds.toDouble / 1000000000)
    var IntegerSeconds: Int = DoubleSeconds.toInt
    val partOfSecondsLeft: Double = DoubleSeconds - IntegerSeconds.toDouble
//    println("Double seconds, integer seconds, partofSecondsLeft = " + DoubleSeconds +  ", " + IntegerSeconds + ", " + partOfSecondsLeft)
    leftTime += partOfSecondsLeft
    if (leftTime >= 1) {
      val leftTimeInt: Int = leftTime.toInt
      leftTime = leftTime - leftTimeInt
      IntegerSeconds += leftTimeInt
//      println(IntegerSeconds + ", " + leftTime)
    }
//    println("Seconds to update is " + IntegerSeconds)
//    println("Left time is " + leftTime)
    IntegerSeconds
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
        val gpc: Int = shop(id).goldPerClick
        val gps: Int = shop(id).idleGold
        current_gold -= price
        GPS += gps
        GPC += gpc
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
