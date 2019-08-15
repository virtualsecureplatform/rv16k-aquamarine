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
import chisel3.core.withReset

class TopUnitPort extends Bundle {
  val romInst = Input(UInt(16.W))

  val x1 = Output(UInt(16.W))
  val romAddr = Output(UInt(9.W))
}

class TopUnit(implicit val conf: RV16KConfig) extends Module {
  val io = IO(new TopUnitPort)

  val st = Module(new StateMachine)

  val ifUnit = Module(new IfUnit)
  val idwbUnit = Module(new IdWbUnit)
  val exUnit = Module(new ExUnit)
  val memUnit = Module(new MemUnit)

  ifUnit.io.Enable := st.io.clockIF
  ifUnit.io.jump := idwbUnit.io.jump
  ifUnit.io.jumpAddress := idwbUnit.io.jumpAddress
  io.romAddr := ifUnit.io.romAddress

  idwbUnit.io.inst := io.romInst
  idwbUnit.io.Enable := st.io.clockID
  idwbUnit.io.wbEnable := st.io.clockWB
  idwbUnit.io.pc := ifUnit.io.romAddress
  idwbUnit.io.FLAGS := exUnit.io.out.flag

  exUnit.io.Enable := st.io.clockEX
  exUnit.io.shifterSig := idwbUnit.io.shifterSig
  exUnit.io.in.opcode := idwbUnit.io.exOpcode
  exUnit.io.in.inA := idwbUnit.io.rdData
  exUnit.io.in.inB := idwbUnit.io.rsData
  exUnit.io.memWriteDataIn := idwbUnit.io.memWriteData

  memUnit.io.Enable := st.io.clockMEM
  memUnit.io.address := exUnit.io.out.res
  memUnit.io.in := exUnit.io.memWriteDataOut
  memUnit.io.memRead := idwbUnit.io.memRead
  memUnit.io.memWrite := idwbUnit.io.memWrite
  memUnit.io.byteEnable := exUnit.io.memByteEnableOut
  memUnit.io.signExt := exUnit.io.memSignExtOut

  idwbUnit.io.writeData := memUnit.io.out

  io.x1 := idwbUnit.io.x1
}
