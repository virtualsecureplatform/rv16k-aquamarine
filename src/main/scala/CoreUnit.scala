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

class CoreUnitPort(implicit val conf:RV16KConfig) extends Bundle {
  val romInst = Input(UInt(16.W))
  val romAddr = Output(UInt(conf.romAddrWidth.W))
  val memA = Flipped(new MemPort)
  val memB = Flipped(new MemPort)

  val testRegx8 = if (conf.test) Output(UInt(16.W)) else Output(UInt(0.W))
  val testFinish = if (conf.test) Output(Bool()) else Output(UInt(0.W))
}

class CoreUnit(implicit val conf: RV16KConfig) extends Module {
  val io = IO(new CoreUnitPort)

  val st = Module(new StateMachine)

  val ifUnit = Module(new IfUnit)
  val idwbUnit = Module(new IdWbUnit)
  val exUnit = Module(new ExUnit)
  val memUnit = Module(new MemUnit)

  ifUnit.io.Enable := st.io.clockIF&&(!idwbUnit.io.ifStole)
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
  exUnit.io.memReadIn := idwbUnit.io.memRead
  exUnit.io.memWriteIn := idwbUnit.io.memWrite
  exUnit.io.regWriteEnableIn := idwbUnit.io.regWriteEnableOut
  exUnit.io.regWriteIn := idwbUnit.io.regWriteOut
  exUnit.io.memSignExtIn := idwbUnit.io.memSignExt
  exUnit.io.memByteEnableIn := idwbUnit.io.memByteEnable

  memUnit.io.Enable := st.io.clockMEM
  memUnit.io.address := exUnit.io.out.res
  memUnit.io.in := exUnit.io.memWriteDataOut
  memUnit.io.memRead := exUnit.io.memReadOut
  memUnit.io.memWrite := exUnit.io.memWriteOut
  memUnit.io.byteEnable := exUnit.io.memByteEnableOut
  memUnit.io.signExt := exUnit.io.memSignExtOut
  memUnit.io.regWriteEnableIn := exUnit.io.regWriteEnableOut
  memUnit.io.regWriteIn := exUnit.io.regWriteOut
  io.memA.address := memUnit.io.memA.address
  io.memA.in := memUnit.io.memA.in
  io.memA.writeEnable := memUnit.io.memA.writeEnable
  memUnit.io.memA.out := io.memA.out
  io.memB.address := memUnit.io.memB.address
  io.memB.in := memUnit.io.memB.in
  io.memB.writeEnable := memUnit.io.memB.writeEnable
  memUnit.io.memB.out := io.memB.out

  idwbUnit.io.writeData := memUnit.io.out
  idwbUnit.io.regWriteEnableIn := memUnit.io.regWriteEnableOut
  idwbUnit.io.regWriteIn := memUnit.io.regWriteOut
  idwbUnit.io.exRegWrite := exUnit.io.regWriteOut
  idwbUnit.io.exRegWriteEnable := exUnit.io.regWriteEnableOut
  idwbUnit.io.exFwdData := exUnit.io.fwdData
  idwbUnit.io.exMemRead := exUnit.io.memReadOut
  idwbUnit.io.exMemWrite := exUnit.io.memWriteOut
  idwbUnit.io.memRegWrite := memUnit.io.regWriteOut
  idwbUnit.io.memRegWriteEnable := memUnit.io.regWriteEnableOut
  idwbUnit.io.memFwdData := memUnit.io.fwdData

  io.testRegx8 := idwbUnit.io.testRegx8
  io.testFinish := idwbUnit.io.testFinish
}
