case class RV16KConfig() {
  var debugIf = true
  var debugId = true
  var debugEx = true
  var debugMem = true
  var debugWb = true

  var test = false
  val romAddrWidth = 9
}