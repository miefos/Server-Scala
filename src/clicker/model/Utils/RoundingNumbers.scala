package clicker.model.Utils

object RoundingNumbers {
  def roundAt(p: Int)(n: Double): Double = {
    val s = math pow (10, p); (math round n * s) / s
  }
}
