/*
Copyright 2019 Naoki Matsumoto

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import java.io.File

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class TopUnitSpec() extends ChiselFlatSpec {
  implicit val conf = RV16KConfig()
  conf.debugIf = false
  conf.debugId = false
  conf.debugEx = false
  conf.debugMem = false
  conf.debugWb = false
  conf.test = true

  val testDir = new File("src/test/binary/")

  testDir.listFiles().foreach { f =>
    if(f.getName().contains("fib.bin")) {
      val parser = new TestBinParser(f.getAbsolutePath())
      val rom = new ExternalRom(parser.romData)
      assert(Driver(() => new TopUnit) {
        c =>
          new PeekPokeTester(c) {
            for (i <- 0 until parser.cycle) {
              val addr = peek(c.io.romAddr).toInt
              poke(c.io.romInst, rom.readInst(addr))
              step(1)
            }
            expect(c.io.testRegx8, parser.res)
          }
      })
    }
  }
}

