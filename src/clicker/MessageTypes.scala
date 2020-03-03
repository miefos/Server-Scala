package clicker

case object Update
case object ClickGold
case object Save
case object Setup
case class BuyEquipment(equipmentId: String)
case object UpdateGames
case object SaveGames
case class GameState(gameState: String)
case class StartedGame(username: String)
case class SaveGame(username: String, gameState: String)
case class Result(passed: Boolean)