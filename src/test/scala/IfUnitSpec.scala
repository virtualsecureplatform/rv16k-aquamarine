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

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class IfUnitSpec extends ChiselFlatSpec {
  implicit val conf = RV16KConfig()
  conf.debugIf = false
  conf.debugId = false
  conf.debugEx = false
  conf.debugMem = false
  conf.debugWb = false
  assert(Driver(() => new IfUnit) {
    c =>
      new PeekPokeTester(c) {
        poke(c.io.jump, false)
        poke(c.io.Enable, true.B)
        for (i <- 0 until 100) {
          expect(c.io.romAddress, (i<<1).U)
          step(1)
        }
        poke(c.io.jump, true)
        poke(c.io.jumpAddress, 10.U)
        step(1)
        expect(c.io.romAddress, 10.U)
        poke(c.io.jump, false)
        step(1)
        for (i <- 0 until 100) {
          expect(c.io.romAddress, ((i<<1) + 12).U)
          step(1)
        }
      }
  })
}

