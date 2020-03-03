package clicker.model

import clicker.model.Utils.RoundingNumbers
import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue}

/**
 *
 * @param basePrice is price of the first item
 * @param increasePricePerItem is percentage by how many percents price increase on each item acquired
 * @param goldPerClick is gold acquired for each click
 * @param idleGold is gold acquired per second without any action from player
 */
class Item (val id: String,
            val name: String,
            var basePrice: Double,
            val increasePricePerItem: Double,
            val goldPerClick: Int,
            val idleGold: Int) {

  override def toString: String = {
    "(" +
          id + ", " +
          basePrice + ", " +
          increasePricePerItem + ", " +
          goldPerClick + ", " +
          idleGold +
      ")"
  }

  // Get Price
  def getPrice(acquired: Int): Double = {
    // Compute
    val basePrice: Double = this.basePrice
    val percentage: Double = this.increasePricePerItem / 100
    val compoundPercentage: Double = Math.pow(1 + percentage, acquired)
    // Round at 10 decimal digits and return
    RoundingNumbers.roundAt(10)(basePrice * compoundPercentage)
  }
}

// Define all available items
object Equipment {
  val available: Map[String, Item] =
        Map(
          "shovel" -> new Item("shovel", "Shovel", 10, 5, 1, 0),
          "excavator" -> new Item("excavator", "Excavator", 200, 10, 5, 10),
          "mine" -> new Item("mine", "Mine", 1000, 10, 0, 100)
        )

  val availableEquipmentJson: List[JsValue] = List()

  for ((id, item) <- available) {
    val itemJson: JsValue = JsObject(
        Seq(
          "id" -> JsString(id),
          "name" -> JsString(id),
        ))
  }

}

object test {
  def main(args: Array[String]): Unit = {
    println(Equipment.available("shovel").getPrice(10))
  }
}