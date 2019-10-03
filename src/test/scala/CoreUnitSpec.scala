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

class CoreUnitSpec() extends ChiselFlatSpec {
  implicit val conf = RV16KConfig()
  conf.debugIf = false
  conf.debugId = false
  conf.debugEx = false
  conf.debugMem = false
  conf.debugWb = false
  conf.test = true

  val testDir = new File("src/test/binary/")

  testDir.listFiles().foreach { f =>
    if(f.getName().contains(".bin")) {
      println(f.getName())
      val parser = new TestBinParser(f.getAbsolutePath())
      val rom = new ExternalRom(parser.romData)

      val memA = new ExternalTestRam(parser.memAData)
      val memB = new ExternalTestRam(parser.memBData)

      var cycle = parser.cycle
      var cycleFinishFlag = false
      assert(Driver(() => new CoreUnit) {
        c =>
          new PeekPokeTester(c) {
            for (i <- 0 until cycle) {
              if((peek(c.io.testFinish) == 1)&&(!cycleFinishFlag)){
                cycle = i+5
                cycleFinishFlag = true
              }
              val addr = peek(c.io.romAddr).toInt
              val memAAddr = peek(c.io.memA.address).toInt
              val memAData = peek(c.io.memA.in).toInt
              val memAWrite = (peek(c.io.memA.writeEnable) != 0)
              val memBAddr = peek(c.io.memB.address).toInt
              val memBData = peek(c.io.memB.in).toInt
              val memBWrite = (peek(c.io.memB.writeEnable) != 0)
              poke(c.io.romInst, rom.readInst(addr))
              step(1)
              memA.step(memAWrite, memAAddr, memAData)
              memB.step(memBWrite, memBAddr, memBData)
              poke(c.io.memA.out, memA.memRead())
              poke(c.io.memB.out, memB.memRead())
            }
            expect(c.io.testRegx8, parser.res)
          }
      })
    }
  }
}

