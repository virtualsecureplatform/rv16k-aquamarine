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

class InstRomPort extends Bundle {
  val address = Input(UInt(5.W))

  val out = Output(UInt(16.W))

}

class InstRom extends Module {
  val io = IO(new InstRomPort);

  def romData() = {
    val rawInst = Array(0x0000 ,0x5200 ,0x002A ,0x7803 ,0x0000 ,0x8031 ,0x7804 ,0x0001 ,0x8041 ,0x7805 ,0x0032 ,0x4000 ,0xE243 ,0xC345 ,0x4408 ,0x8031 ,0xE234 ,0xC345 ,0x4604 ,0x8041 ,0x5200 ,0xFFEE ,0x4000 ,0x7300 ,0xFFD6 ,0x7300 ,0xFFE4 ,0x8030 ,0x5200 ,0xFFFE)
    val times = (0 until 32).map(i => rawInst(i % (rawInst.size)).asUInt(16.W))
    VecInit(times)
  }

  io.out := romData()(io.address)
}
