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
  conf.debugIf = false
  conf.debugId = false
  conf.debugEx = false
  conf.debugMem = false
  conf.debugWb = false
  conf.test = true
  assert(Driver(() => new IdWbUnit) {
    c =>
      new PeekPokeTester(c) {
        poke(c.io.Enable, true.B)
        poke(c.io.wbEnable, false.B)
        poke(c.io.FLAGS, 0.U)
        poke(c.io.pc, 0x10.U)
        poke(c.io.writeData, 0x10.U)
        poke(c.io.inst, 0x0.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        //LW
        poke(c.io.inst, 0xB2AB.U)
        step(1)
        expect(c.io.debugImmLongState, 1.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        poke(c.io.inst, 0x000A.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, true.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.memRead, true.B)
        expect(c.io.debugRs, 10.U)
        expect(c.io.debugRd, 11.U)
        expect(c.io.exOpcode, 2.U)
        expect(c.io.rdData, 10.U)
        expect(c.io.memByteEnable, false.B)
        expect(c.io.memSignExt, false.B)
        //LWSP
        poke(c.io.inst, 0xA05B.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, true.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.memRead, true.B)
        expect(c.io.debugRs, 1.U)
        expect(c.io.debugRd, 11.U)
        expect(c.io.exOpcode, 2.U)
        expect(c.io.rdData, 10.U)
        expect(c.io.memByteEnable, false.B)
        expect(c.io.memSignExt, false.B)
        //LBU
        poke(c.io.inst, 0xBAAB.U)
        step(1)
        expect(c.io.debugImmLongState, 1.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        poke(c.io.inst, 0x000A.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, true.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.memRead, true.B)
        expect(c.io.debugRs, 10.U)
        expect(c.io.debugRd, 11.U)
        expect(c.io.exOpcode, 2.U)
        expect(c.io.rdData, 10.U)
        expect(c.io.memByteEnable, true.B)
        expect(c.io.memSignExt, false.B)
        //LB
        poke(c.io.inst, 0xBEAB.U)
        step(1)
        expect(c.io.debugImmLongState, 1.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        poke(c.io.inst, 0x000A.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, true.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.memRead, true.B)
        expect(c.io.debugRs, 10.U)
        expect(c.io.debugRd, 11.U)
        expect(c.io.exOpcode, 2.U)
        expect(c.io.rdData, 10.U)
        expect(c.io.memByteEnable, true.B)
        expect(c.io.memSignExt, true.B)
        //SW
        poke(c.io.inst, 0x92BA.U)
        step(1)
        expect(c.io.debugImmLongState, 1.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        poke(c.io.inst, 0x000A.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, true.B)
        expect(c.io.memRead, false.B)
        expect(c.io.debugRs, 11.U)
        expect(c.io.debugRd, 10.U)
        expect(c.io.exOpcode, 2.U)
        expect(c.io.rsData, 10.U)
        expect(c.io.memByteEnable, false.B)
        //SWSP
        poke(c.io.inst, 0x80B5.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, true.B)
        expect(c.io.memRead, false.B)
        expect(c.io.debugRs, 11.U)
        expect(c.io.debugRd, 1.U)
        expect(c.io.exOpcode, 2.U)
        expect(c.io.rsData, 10.U)
        expect(c.io.memByteEnable, false.B)
        //SB
        poke(c.io.inst, 0x9ABA.U)
        step(1)
        expect(c.io.debugImmLongState, 1.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        poke(c.io.inst, 0x000A.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, true.B)
        expect(c.io.memRead, false.B)
        expect(c.io.debugRs, 11.U)
        expect(c.io.debugRd, 10.U)
        expect(c.io.exOpcode, 2.U)
        expect(c.io.rsData, 10.U)
        expect(c.io.memByteEnable, true.B)
        //MOV
        //ADD
        //SUB
        //AND
        //OR
        //XOR
        //LSL
        //LSR
        //ASR
        //CMP
        /*
        //J
        poke(c.io.inst, 0x5200.U)
        step(1)
        expect(c.io.debugImmLongState, 1.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        poke(c.io.inst, 0x000A.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.jump, true.B)
        expect(c.io.jumpAddress, 0x1A.U)
        //JAL
        poke(c.io.inst, 0x7300.U)
        step(1)
        expect(c.io.debugImmLongState, 1.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        poke(c.io.inst, 0x000A.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, true.B)
        expect(c.io.rsData, 0x12.U)
        expect(c.io.memWrite, false.B)
        expect(c.io.jump, true.B)
        expect(c.io.jumpAddress, 0x1A.U)
        //JALR
        poke(c.io.inst, 0x61A0.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, true.B)
        expect(c.io.rsData, 0x12.U)
        expect(c.io.memWrite, false.B)
        expect(c.io.jump, true.B)
        //JR
        poke(c.io.inst, 0x40A0.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.rsData, 0x12.U)
        expect(c.io.memWrite, false.B)
        expect(c.io.jump, true.B)
        //JL
        poke(c.io.inst, 0x4405.U)
        poke(c.io.FLAGS, 0.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.jump, false.B)

        poke(c.io.FLAGS, 8.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.jump, true.B)
        expect(c.io.jumpAddress, 0x1A.U)
        //JLE
        poke(c.io.inst, 0x4485.U)
        poke(c.io.FLAGS, 0.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.jump, false.B)

        poke(c.io.FLAGS, 4.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.jump, true.B)
        expect(c.io.jumpAddress, 0x1A.U)
        //JE
        poke(c.io.inst, 0x4505.U)
        poke(c.io.FLAGS, 0.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.jump, false.B)

        poke(c.io.FLAGS, 4.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.jump, true.B)
        expect(c.io.jumpAddress, 0x1A.U)
        //JNE
        poke(c.io.inst, 0x4585.U)
        poke(c.io.FLAGS, 4.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.jump, false.B)

        poke(c.io.FLAGS, 0.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.jump, true.B)
        expect(c.io.jumpAddress, 0x1A.U)
        //JB
        poke(c.io.inst, 0x4605.U)
        poke(c.io.FLAGS, 0.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.jump, false.B)

        poke(c.io.FLAGS, 2.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.jump, true.B)
        expect(c.io.jumpAddress, 0x1A.U)
        //JBE
        poke(c.io.inst, 0x4685.U)
        poke(c.io.FLAGS, 0.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.jump, false.B)

        poke(c.io.FLAGS, 4.U)
        step(1)
        expect(c.io.debugImmLongState, 0.U)
        expect(c.io.debugRegWrite, false.B)
        expect(c.io.memWrite, false.B)
        expect(c.io.jump, true.B)
        expect(c.io.jumpAddress, 0x1A.U)
        */
      }
  })
}

