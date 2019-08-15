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

class MainRegisterPort extends Bundle {
  val rs = Input(UInt(4.W))
  val rd = Input(UInt(4.W))
  val writeEnable = Input(Bool())
  val writeData = Input(UInt(16.W))

  val rsData = Output(UInt(16.W))
  val rdData = Output(UInt(16.W))

  val x1 = Output(UInt(16.W))
}

class MainRegister extends Module{
  val io = IO(new MainRegisterPort)

  val MainReg = Mem(16, UInt(16.W))

  io.rsData := MainReg(io.rs)
  io.rdData := MainReg(io.rd)
  io.x1 := MainReg(1)

  when(io.writeEnable === true.B) {
    MainReg(io.rd) := io.writeData
    printf("[WB] Reg x%d <= 0x%x\n", io.rd, io.writeData)
  }.otherwise {}
}
