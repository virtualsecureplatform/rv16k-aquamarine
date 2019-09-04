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

class MemPort extends Bundle {
  val in = Input(UInt(8.W))
  val address = Input(UInt(16.W))
  val writeEnable = Input(Bool())

  val out = Output(UInt(8.W))
}

class MemUnitPort extends Bundle {
  val in =  Input(UInt(16.W))
  val address = Input(UInt(16.W))

  val memRead = Input(Bool())
  val memWrite = Input(Bool())
  val byteEnable = Input(Bool())
  val signExt = Input(Bool())
  val Enable = Input(Bool())
  val regWriteEnableIn = Input(Bool())
  val regWriteIn = Input(UInt(4.W))

  val memA = Flipped(new MemPort)
  val memB = Flipped(new MemPort)

  val out = Output(UInt(16.W))
  val regWriteEnableOut = Output(Bool())
  val regWriteOut = Output(UInt(4.W))
}

class MemReg extends Bundle {
  val address = UInt(16.W)
  val memRead = Bool()
  val byteEnable = Bool()
  val signExt = Bool()

  val regWriteEnable = Bool()
  val regWrite = UInt(4.W)
  val outData = UInt(16.W)
}

class MemUnitTestPort extends Bundle{
  val in =  Input(UInt(16.W))
  val address = Input(UInt(16.W))

  val memRead = Input(Bool())
  val memWrite = Input(Bool())
  val byteEnable = Input(Bool())
  val signExt = Input(Bool())
  val Enable = Input(Bool())

  val out = Output(UInt(16.W))
}
class MemUnitTest(implicit val conf:RV16KConfig) extends Module {
  val io = IO(new MemUnitTestPort)
  val unit = Module(new MemUnit)
  val memA = Module(new ExternalRam)
  val memB = Module(new ExternalRam)

  unit.io.in := io.in
  unit.io.address := io.address
  unit.io.memRead := io.memRead
  unit.io.memWrite := io.memWrite
  unit.io.byteEnable := io.byteEnable
  unit.io.signExt := io.signExt
  unit.io.Enable := io.Enable
  memA.io.address := unit.io.memA.address
  memA.io.in := unit.io.memA.in
  memA.io.writeEnable := unit.io.memA.writeEnable
  unit.io.memA.out := memA.io.out
  memB.io.address := unit.io.memB.address
  memB.io.in := unit.io.memB.in
  memB.io.writeEnable := unit.io.memB.writeEnable
  unit.io.memB.out := memB.io.out
  io.out := unit.io.out
}

class MemUnit(implicit val conf:RV16KConfig) extends Module {
  val io = IO(new MemUnitPort)

  val pReg = RegInit(0.U.asTypeOf(new MemReg))

  when(io.Enable){
    pReg.address := io.address
    pReg.memRead := io.memRead
    pReg.signExt := io.signExt
    pReg.byteEnable := io.byteEnable

    pReg.regWriteEnable := io.regWriteEnableIn
    pReg.regWrite := io.regWriteIn
    pReg.outData := io.address
  }
  io.regWriteEnableOut := pReg.regWriteEnable
  io.regWriteOut := pReg.regWrite

  def sign_ext_8bit(v:UInt) : UInt = {
    val res = Wire(UInt(16.W))
    when(v(7,7) === 1.U){
      res := Cat(0xFF.U(8.W), v)
    }.otherwise{
      res := v
    }
    res
  }

  val addr = Wire(UInt(8.W))
  val data_upper = io.in(15, 8)
  val data_lower = io.in(7, 0)
  addr := io.address(8,1)

  io.memA.address := addr
  io.memB.address := addr
  io.memA.in := data_upper
  io.memB.in := data_lower
  io.memA.writeEnable := false.B
  io.memB.writeEnable := false.B

  when(io.byteEnable){
    when(io.memWrite){
      when(io.address(0, 0) === 1.U) {
        io.memA.writeEnable := true.B&io.Enable
        io.memA.in := data_lower
      }.otherwise {
        io.memB.writeEnable := true.B&io.Enable
        io.memB.in := data_lower
      }
    }
  }.otherwise {
    when(io.memWrite) {
      io.memA.writeEnable := true.B&io.Enable
      io.memB.writeEnable := true.B&io.Enable
    }
  }

  io.out := DontCare
  when(pReg.memRead){
    when(pReg.byteEnable){
      when(pReg.address(0,0) === 1.U){
        when(pReg.signExt){
          io.out := sign_ext_8bit(io.memA.out)
        }.otherwise{
          io.out := io.memA.out
        }
      }.otherwise{
        when(pReg.signExt){
          io.out := sign_ext_8bit(io.memB.out)
        }.otherwise{
          io.out := io.memB.out
        }
      }
    }.otherwise{
      io.out := Cat(io.memA.out, io.memB.out)
    }
  }.otherwise{
    io.out := pReg.outData
  }

  when(conf.debugMem.B){
    when(pReg.memRead) {
      printf("[MEM] MemRead Mem[0x%x] => Data:0x%x\n", pReg.address, io.out)
    }
  }
}
