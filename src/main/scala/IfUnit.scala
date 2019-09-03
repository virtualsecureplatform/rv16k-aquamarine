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

class IfUnitPort extends Bundle {
  val jumpAddress = Input(UInt(9.W))
  val jump = Input(Bool())

  val romAddress = Output(UInt(9.W))

  val Enable = Input(Bool())
}

class IfUnit(implicit val conf: RV16KConfig) extends Module {
  val io = IO(new IfUnitPort)
  val pc = Module(new PC)

  pc.io.jumpAddress := io.jumpAddress
  pc.io.jump := io.jump
  pc.io.Enable := io.Enable

  io.romAddress := pc.io.pcOut

  when(conf.debugIf.B){
    printf("\n[IF]PC Address:0x%x\n", io.romAddress)
  }
}

class PCPort extends Bundle {
  val jumpAddress = Input(UInt(9.W))
  val jump = Input(Bool())
  val Enable = Input(Bool())
  val pcOut = Output(UInt(9.W))
}

class PC extends Module {
  val io = IO(new PCPort)
  val RegPC = RegInit(0.U(9.W))
  when(io.Enable) {
    when(io.jump === false.B) {
      RegPC := RegPC + 2.U(9.W)
    }.otherwise {
      RegPC := io.jumpAddress
    }
  }.otherwise{
    RegPC := RegPC
  }
  io.pcOut := RegPC
}
