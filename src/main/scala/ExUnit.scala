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
import chisel3.util.{BitPat, Cat}
object ALUOpcode {
  def MOV = BitPat("b000")
  def ADD = BitPat("b010")
  def SUB = BitPat("b011")
  def AND = BitPat("b100")
  def OR  = BitPat("b101")
  def XOR = BitPat("b110")
}
object ShifterOpcode {
  def LSL = BitPat("b001")
  def LSR = BitPat("b010")
  def ASR = BitPat("b100")
}

class ExUnitInput extends Bundle {
  val opcode = Input(UInt(3.W))
  val inA = Input(UInt(16.W))
  val inB = Input(UInt(16.W))
}

class ExUnitOutput extends Bundle {
  val res = Output(UInt(16.W))
  val flag = Output(UInt(4.W))
}

class ExUnitIO extends Bundle {
  val in = new ExUnitInput
  val out = new ExUnitOutput
}

class ExIO extends Bundle {
  val in = new ExUnitInput
  val shifterSig = Input(Bool())
  val out = new ExUnitOutput
}


class ExUnit extends Module {
  val io = IO(new ExIO)
  val alu = Module(new ALU)
  val shifter = Module(new Shifter)

  alu.io.in := io.in
  shifter.io.in := io.in
  when(io.shifterSig){
    io.out := shifter.io.out
  }.otherwise{
    io.out := alu.io.out
  }
}

object FLAGS{
  def check_sign(t:UInt):UInt = t(15)
  def check_zero(t:UInt):UInt = {
    val res = Wire(UInt(1.W))
    when(t === 0.U(16.W)) {
      res := 1.U
    }.otherwise{
      res := 0.U
    }
    res
  }
  def check_overflow(s1:UInt, s2:UInt, r:UInt) = {
    val s1_sign = Wire(UInt(1.W))
    val s2_sign = Wire(UInt(1.W))
    val res_sign = Wire(UInt(1.W))
    val res = Wire(UInt(1.W))
    s1_sign := s1(15)
    s2_sign := s2(15)
    res_sign := r(15)
    when(((s1_sign^s2_sign) === 0.U) && ((s2_sign^res_sign) === 1.U)){
      res := 1.U(1.W)
    }.otherwise{
      res := 0.U(1.W)
    }
    res
  }
}

class ALU extends Module{
  val io = IO(new ExUnitIO)
  val flagCarry = Wire(UInt(1.W))
  val resCarry = Wire(UInt(17.W))
  val flagOverflow = Wire(UInt(1.W))

  resCarry := DontCare
  flagCarry := 0.U(1.W)
  flagOverflow := 0.U(1.W)
  when(io.in.opcode === ALUOpcode.MOV){
    io.out.res := io.in.inB
  }.elsewhen(io.in.opcode === ALUOpcode.ADD){
    resCarry := io.in.inA+&io.in.inB
    io.out.res := resCarry(15,0)
    flagCarry := resCarry(16)
    flagOverflow := FLAGS.check_overflow(io.in.inA, io.in.inB, io.out.res)
  }.elsewhen(io.in.opcode === ALUOpcode.SUB){
    resCarry := io.in.inA-&io.in.inB
    io.out.res := resCarry(15,0)
    flagCarry := resCarry(16)
    flagOverflow := FLAGS.check_overflow(io.in.inA, io.in.inB, io.out.res)
  }.elsewhen(io.in.opcode === ALUOpcode.AND){
    io.out.res := io.in.inA&io.in.inB
  }.elsewhen(io.in.opcode === ALUOpcode.OR){
    io.out.res := io.in.inA|io.in.inB
  }.elsewhen(io.in.opcode === ALUOpcode.XOR){
    io.out.res := io.in.inA^io.in.inB
  }.otherwise{
    io.out.res := DontCare
  }
  io.out.flag := Cat(FLAGS.check_sign(io.out.res), FLAGS.check_zero(io.out.res), flagCarry, flagOverflow)
}

class Shifter extends Module {
  val io = IO(new ExUnitIO)
  when(io.in.opcode === ShifterOpcode.LSL){
    io.out.res := (io.in.inA << io.in.inB).asUInt()
  }.elsewhen(io.in.opcode === ShifterOpcode.LSR){
    io.out.res := (io.in.inA >> io.in.inB).asUInt()
  }.elsewhen(io.in.opcode === ShifterOpcode.ASR) {
    io.out.res := (io.in.inA.asSInt() >> io.in.inB).asUInt()
  }.otherwise{
    io.out.res := DontCare
  }
  io.out.flag := Cat(FLAGS.check_sign(io.out.res), FLAGS.check_zero(io.out.res), 0.U(2.W))
}
