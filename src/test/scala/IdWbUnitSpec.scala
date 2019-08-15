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

class IdWbUnitSpec extends ChiselFlatSpec {
  implicit val conf = RV16KConfig()
  assert(Driver(() => new IdWbUnit) {
    c =>
      new PeekPokeTester(c) {
        poke(c.io.inst, 0x7801.U)
        step(1)
        expect(c.io.memWrite, false.B)
        expect(c.io.writeEnable, false.B)
        poke(c.io.inst, 0x1.U)
        step(1)
        expect(c.io.rd, 1.U)
        expect(c.io.rsData, 0x1.U)
        expect(c.io.writeEnable, true.B)
      }
  })
}

