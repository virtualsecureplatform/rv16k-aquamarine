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
import chisel3.util.Cat

class IdUnitPort (implicit val conf:RV16KConfig) extends Bundle {
  val inst = Input(UInt(16.W))
  val writeData = Input(UInt(16.W))
  val wbEnable = Input(Bool())
  val Enable = Input(Bool())
  val pc = Input(UInt(9.W))
  val FLAGS = Input(UInt(4.W))
  val regWriteEnableIn = Input(Bool())

  val memWriteData = Output(UInt(16.W))
  val exOpcode = Output(UInt(3.W))
  val shifterSig = Output(Bool())
  val rsData = Output(UInt(16.W))
  val rdData = Output(UInt(16.W))

  val memRead = Output(Bool())
  val memWrite = Output(Bool())
  val memByteEnable = Output(Bool())
  val memSignExt = Output(Bool())

  val jumpAddress = Output(UInt(8.W))
  val jump = Output(Bool())

  val regWriteEnableOut = Output(Bool())

  val debugRs = if (conf.debugId) Output(UInt(4.W)) else Output(UInt(0.W))
  val debugRd = if (conf.debugId) Output(UInt(4.W)) else Output(UInt(0.W))
  val debugRegWrite = if(conf.debugId) Output(Bool()) else Output(UInt(0.W))
  val debugImmLongState = if(conf.debugId) Output(Bool()) else Output(UInt(0.W))
}

class LongImm extends Bundle {
  val inst = UInt(16.W)
}

class DecoderPort extends Bundle {
  val inst = Input(UInt(16.W))
  val FLAGS = Input(UInt(4.W))

  val rs = Output(UInt(4.W))
  val rd = Output(UInt(4.W))
  val writeEnable = Output(Bool())

  val immSel = Output(Bool())
  val imm = Output(UInt(16.W))

  val exOpcode = Output(UInt(3.W))
  val shifterSig = Output(Bool())

  val memRead = Output(Bool())
  val memWrite = Output(Bool())
  val memByteEnable = Output(Bool())
  val memSignExt = Output(Bool())

  val jump = Output(Bool())
}

class Decoder extends Module {
  def sign_ext_4bit(v:UInt) : UInt = {
    val res = Wire(UInt(16.W))
    when(v(3,3) === 1.U){
      res := Cat(0xFFF.U(12.W), v)
    }.otherwise{
      res := v
    }
    res
  }
  def sign_ext_7bit(v:UInt) : UInt = {
    val res = Wire(UInt(16.W))
    when(v(6,6) === 1.U){
      res := Cat(0x1FF.U(9.W), v)
    }.otherwise{
      res := v
    }
    res
  }
  val io = IO(new DecoderPort)


  io.rs := io.inst(7, 4)
  io.rd := io.inst(3, 0)
  io.writeEnable := io.inst(13, 13)

  io.immSel := false.B
  io.imm := DontCare
  io.memSignExt := DontCare
  io.memByteEnable := DontCare

  io.jump := false.B
  when(io.inst(15, 14) === 0.U){
    //NOP
    io.exOpcode := DontCare
    io.shifterSig := DontCare
    io.memRead := false.B
    io.memWrite := false.B
  }.elsewhen(io.inst(15, 14) === 1.U){
    //J-Instruction
    io.exOpcode := 0.U(3.W)
    io.shifterSig := false.B
    io.memRead := false.B
    io.memWrite := false.B
    io.imm := sign_ext_7bit(io.inst(6,0)) << 1
    io.immSel := true.B
    when(io.inst(11, 10) === 0.U){
      io.jump := true.B
      io.rd := 0.U(4.W)
    }.elsewhen(io.inst(11, 10) === 1.U){
      when(io.inst(9, 7) === 0.U){
        //JL
        io.jump := (io.FLAGS(3) != io.FLAGS(0))
      }.elsewhen(io.inst(9, 7) === 1.U){
        //JLE
        io.jump := (io.FLAGS(3) != io.FLAGS(0)) || (io.FLAGS(2) === 1.U)
      }.elsewhen(io.inst(9, 7) === 2.U){
        //JE
        io.jump := (io.FLAGS(2) === 1.U)
      }.elsewhen(io.inst(9, 7) === 3.U){
        //JNE
        io.jump := (io.FLAGS(2) === 0.U)
      }.elsewhen(io.inst(9, 7) === 4.U){
        //JB
        io.jump := (io.FLAGS(1) === 1.U)
      }.elsewhen(io.inst(9, 7) === 5.U){
        //JBE
        io.jump := (io.FLAGS(1) === 1.U) || (io.FLAGS(2) === 1.U)
      }

    }
  }.elsewhen(io.inst(15, 14) === 2.U){
    //M-Instruction
    io.exOpcode := 2.U(3.W) //Opcode ADD
    io.shifterSig := false.B
    io.memRead := (io.inst(13, 13) === 1.U)
    io.memWrite := (io.inst(13, 13) === 0.U)
    io.memSignExt := false.B
    io.memByteEnable := false.B
    when(io.inst(13,12) === 2.U){
      //LWSP
      io.immSel := true.B
      io.imm := Cat(0.U(7.W), io.inst(11, 4), 0.U(1.W))
      io.rs := 1.U(4.W)
    }.elsewhen(io.inst(13, 12) === 0.U) {
      //SWSP
      io.immSel := true.B
      io.imm := Cat(0.U(7.W), io.inst(11, 8), io.inst(3, 0), 0.U(1.W))
      io.rd := 1.U(4.W)
    }.otherwise{
      io.immSel := false.B
      when(io.inst(12,11) === 3.U){
        io.memByteEnable := true.B
      }
      when(io.inst(10,10) === 1.U) {
        io.memSignExt := true.B
      }
    }
  }.otherwise{
    //R-Instruction
    io.exOpcode := io.inst(10, 8)
    io.shifterSig := (io.inst(11, 11) === 1.U)
    io.immSel := (io.inst(12,12) === 1.U)
    io.imm := sign_ext_4bit(io.rs)
    io.memRead := false.B
    io.memWrite := false.B
  }

}

class IdRegister extends Bundle {
  val inst = UInt(16.W)
  val pc = UInt(9.W)
  val FLAGS = UInt(4.W)
  val longInst = UInt(16.W)
  val longInstState = UInt(2.W)
}

class IdWbUnit(implicit val conf: RV16KConfig) extends Module {

  val io = IO(new IdUnitPort)
  val mainRegister = Module(new MainRegister)
  val decoder = Module(new Decoder)

  val pReg = RegInit(0.U.asTypeOf(new IdRegister))

  // 0 -> mainRegister Rs
  // 1 -> decoder.imm
  // 2 -> inst
  // 3 -> PC+2
  val rsDataSrc = Wire(UInt(2.W))
  when(rsDataSrc === 0.U){
    io.rsData := mainRegister.io.rsData
  }.elsewhen(rsDataSrc === 1.U){
    io.rsData := decoder.io.imm
  }.elsewhen(rsDataSrc === 2.U){
    io.rsData := pReg.inst
  }.otherwise{
    io.rsData := pReg.pc + 2.U
  }

  val rdDataSrc = Wire(UInt(2.W))
  when(rdDataSrc === 0.U){
    io.rdData := mainRegister.io.rdData
  }.elsewhen(rdDataSrc === 1.U){
    io.rdData := decoder.io.imm
  }.otherwise{
    io.rdData := pReg.inst
  }

  when(io.Enable) {
    pReg.inst := io.inst
    pReg.pc := io.pc
    pReg.FLAGS := io.FLAGS
    when((io.inst(15,14) != 3.U) && (io.inst(12, 12) === 1.U) && ((pReg.longInstState === 0.U) || (pReg.longInstState === 2.U))) {
      pReg.longInst := io.inst
      pReg.longInstState := 1.U
    }.elsewhen(pReg.longInstState === 1.U) {
      pReg.longInstState := 2.U
    }.elsewhen(pReg.longInstState === 2.U) {
      pReg.longInstState := 0.U
    }
  }.otherwise{
    pReg := pReg
  }


  mainRegister.io.rs := decoder.io.rs
  mainRegister.io.rd := decoder.io.rd

  rsDataSrc := 0.U
  rdDataSrc := 0.U
  io.memWriteData := mainRegister.io.rsData
  io.jumpAddress := DontCare
  decoder.io.inst := 0.U(16.W)
  decoder.io.FLAGS := pReg.FLAGS
  when(pReg.longInstState === 0.U){
    decoder.io.inst := pReg.inst
    when(decoder.io.immSel) {
      when(pReg.inst(15, 14) === 2.U) {
        when(pReg.inst(13, 13) === 1.U) {
          //LWSP
          rdDataSrc := 1.U
        }.otherwise {
          //SWSP
          rsDataSrc := 1.U
        }
      }.elsewhen(pReg.inst(15, 14) === 1.U){
        when(pReg.inst(10, 10) === 1.U){
          //JL,JLE,JE,JNE,JB,JBE
          io.jumpAddress := pReg.pc + decoder.io.imm
        }.otherwise{
          //JALR,JR
          io.jumpAddress := mainRegister.io.rsData
          rsDataSrc := 3.U
        }
      }.otherwise {
        rsDataSrc := 1.U
      }
    }
  }.elsewhen((pReg.longInstState === 2.U) && (pReg.longInst(15,14) === 1.U)){
    decoder.io.inst := pReg.longInst
    when(pReg.longInst(11,11) === 1.U){
      //LI
      rsDataSrc := 2.U
    }.otherwise{
      //J,JAL
      rsDataSrc := 3.U
      io.jumpAddress := pReg.pc + pReg.inst
    }
  }.elsewhen((pReg.longInstState === 2.U) && (pReg.longInst(15,14) === 2.U)){
    decoder.io.inst := pReg.longInst
    when(pReg.longInst(13, 13) === 1.U) {
      //LW,LB,LBU
      rdDataSrc := 2.U
    }.otherwise{
      //SW,SB
      rsDataSrc := 2.U
    }
  }

  mainRegister.io.writeEnable := io.regWriteEnableIn
  mainRegister.io.writeData := io.writeData


  io.exOpcode := decoder.io.exOpcode
  io.shifterSig := decoder.io.shifterSig

  io.memRead := decoder.io.memRead
  io.memWrite := decoder.io.memWrite

  io.jump := decoder.io.jump

  io.memByteEnable := decoder.io.memByteEnable
  io.memSignExt := decoder.io.memSignExt

  io.regWriteEnableOut := decoder.io.writeEnable

  when(conf.debugId.B){
    printf("[ID] Instruction:0x%x\n", io.inst)
    printf("[ID] LongInstState:0x%x\n", pReg.longInstState)
    printf("[ID] LongInst:0x%x\n", pReg.longInst)
    printf("[ID] Decoder Inst:0x%x\n", decoder.io.inst)
    when(io.jump) {
      printf("[ID] JumpAddress:0x%x\n", io.jumpAddress)
    }
  }
  io.debugRs := decoder.io.rs
  io.debugRd := decoder.io.rd
  io.debugRegWrite := decoder.io.writeEnable
  io.debugImmLongState := pReg.longInstState
}
