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
import chisel3.util.BitPat
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
  val flag = Output(UInt(16.W))
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

class ALU extends Module{
  val io = IO(new ExUnitIO)
  when(io.in.opcode === ALUOpcode.MOV){
    io.out.res := io.in.inB
  }.elsewhen(io.in.opcode === ALUOpcode.ADD){
    io.out.res := io.in.inA+io.in.inB
  }.elsewhen(io.in.opcode === ALUOpcode.SUB){
    io.out.res := io.in.inA-io.in.inB
  }.elsewhen(io.in.opcode === ALUOpcode.AND){
    io.out.res := io.in.inA&io.in.inB
  }.elsewhen(io.in.opcode === ALUOpcode.OR){
    io.out.res := io.in.inA|io.in.inB
  }.elsewhen(io.in.opcode === ALUOpcode.XOR){
    io.out.res := io.in.inA^io.in.inB
  }.otherwise{
    io.out.res := DontCare
  }
  io.out.flag := DontCare
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
  io.out.flag := DontCare
}