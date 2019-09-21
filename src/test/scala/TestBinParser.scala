import scala.io.Source

class TestBinParser(filePath: String) {
  val source = Source.fromFile(filePath)
  val lines = source.getLines

  var memAData = Map[Int, Int]()
  var memBData = Map[Int, Int]()
  var romData = Map[Int, Int]()
  var res = 0
  var cycle = 0


  lines.foreach(s => parseLine(s))

  def parseLine(line:String){
    val tokens = line.split(" ", 0)
    if(tokens.length == 3){
      val addr = Integer.parseUnsignedInt(tokens(1), 16).toInt
      val data = Integer.parseUnsignedInt(tokens(2), 16).toInt
      if(tokens(0).contains("ROM")){
        romData += (addr->data)
      }
      else if(tokens(0).contains("RAM")){
        if(addr%2 == 1){
          memAData += (addr->data)
        }else{
          memBData += (addr->data)
        }
      }
    }
    else if(tokens.length == 2){
      if(tokens(0).contains("FIN")){
        res = Integer.parseUnsignedInt(tokens(1), 16)
      }
      else if(tokens(0).contains("CYCLE")){
        cycle = Integer.parseUnsignedInt(tokens(1), 16)
      }
    }
  }
}