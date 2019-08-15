import chisel3._

import scala.io.Source

class ExternalRom {
  val source = Source.fromFile("src/test/binary/test.bin")
  val lines = source.getLines

  var instRom:Array[Int] = Array.empty
  lines.foreach { s=>
    instRom = instRom :+ parse(s)
  }
  def parse(s:String):Int = Integer.parseUnsignedInt(s.split(" ", 0)(1), 16).toInt
  def readInst(addr:Int) = instRom(addr>>1)
}
