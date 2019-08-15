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

class MemUnitPort extends Bundle {
  val in =  Input(UInt(16.W))
  val address = Input(UInt(16.W))

  val memRead = Input(Bool())
  val memWrite = Input(Bool())
  val byteEnable = Input(Bool())
  val signExt = Input(Bool())

  val Enable = Input(Bool())

  val out = Output(UInt(16.W))
}

class MemUnit extends Module {
  val io = IO(new MemUnitPort)

  def sign_ext_8bit(v:UInt) : UInt = {
    val res = Wire(UInt(16.W))
    when(v(7,7) === 1.U){
      res := Cat(0xFF.U(8.W), v)
    }.otherwise{
      res := v
    }
    res
  }

  val memory = Module(new DataRam)

  memory.io.address := io.address
  memory.io.writeData := io.in
  memory.io.writeEnable := io.memWrite&&io.Enable
  memory.io.byteEnable := io.byteEnable

  when(io.memRead){
    when(io.signExt){
      io.out := sign_ext_8bit(memory.io.out)
    }.otherwise{
      io.out := memory.io.out
    }
  }.otherwise{
    io.out := io.address
  }

  val debug = RegInit(false.B)
  debug := io.Enable
  when(debug){
    when(io.memRead) {
      printf("[MEM] MemRead Mem[0x%x] => Data:0x%x\n", io.address, io.out)
    }
    when(io.memWrite) {
      printf("[MEM] MemWrite Mem[0x%x] <= Data:0x%x\n", io.address, io.in)
    }
  }
}
