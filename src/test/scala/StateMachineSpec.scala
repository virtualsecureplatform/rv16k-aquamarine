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

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class StateMachineSpec extends ChiselFlatSpec {
  assert(Driver(() => new StateMachine) {
    c =>
      new PeekPokeTester(c) {
        for (i <- 0 until 100) {
          i % 5 match {
            case 0 =>
              expect(c.io.clockIF, true)
              expect(c.io.clockID, false)
              expect(c.io.clockEX, false)
              expect(c.io.clockMEM, false)
              expect(c.io.clockWB, false)
            case 1 =>
              expect(c.io.clockIF, false)
              expect(c.io.clockID, true)
              expect(c.io.clockEX, false)
              expect(c.io.clockMEM, false)
              expect(c.io.clockWB, false)
            case 2 =>
              expect(c.io.clockIF, false)
              expect(c.io.clockID, false)
              expect(c.io.clockEX, true)
              expect(c.io.clockMEM, false)
              expect(c.io.clockWB, false)
            case 3 =>
              expect(c.io.clockIF, false)
              expect(c.io.clockID, false)
              expect(c.io.clockEX, false)
              expect(c.io.clockMEM, true)
              expect(c.io.clockWB, false)
            case 4 =>
              expect(c.io.clockIF, false)
              expect(c.io.clockID, false)
              expect(c.io.clockEX, false)
              expect(c.io.clockMEM, false)
              expect(c.io.clockWB, true)
          }
          step(1)
        }
      }
  })
}
