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

class DataRamPort extends Bundle {
  val address = Input(UInt(9.W))

  val writeData = Input(UInt(16.W))
  val writeEnable = Input(Bool())
  val byteEnable = Input(Bool())

  val out = Output(UInt(16.W))
}

class DataRam extends Module{
  val io = IO(new DataRamPort)

  val memory_upper = Mem(256, UInt(8.W))
  val memory_lower = Mem(256, UInt(8.W))


  val addr = Wire(UInt(8.W))
  val data_upper = io.writeData(15, 8)
  val data_lower = io.writeData(7, 0)
  addr := io.address(8,1)

  when(io.byteEnable){
    when(io.address(0, 0) === 1.U) {
      when(io.writeEnable) {
        memory_upper(addr) := data_lower
        io.out := DontCare
      }.otherwise {
        io.out := memory_upper(addr)
      }
    }.otherwise{
      when(io.writeEnable) {
        memory_lower(addr) := data_lower
        io.out := DontCare
      }.otherwise {
        io.out := memory_lower(addr)
      }
    }
  }.otherwise {
    when(io.writeEnable) {
      memory_lower(addr) := data_lower
      memory_upper(addr) := data_upper
      io.out := DontCare
    }.otherwise {
      io.out := Cat(memory_upper(addr), memory_lower(addr))
    }
  }

}
