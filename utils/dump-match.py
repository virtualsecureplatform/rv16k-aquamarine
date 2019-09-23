sim_dump = 'sim.dump'
cpu_dump = 'cpu.dump'


sim_f = open(sim_dump)
sim_dump_list = sim_f.readlines()

cpu_f = open(cpu_dump)
cpu_dump_list = cpu_f.readlines()

for sim, cpu in zip(sim_dump_list, cpu_dump_list):
    sim_split = sim.split(" ")
    sim_pc = int(sim_split[0], 16)
    sim_res = int(sim_split[4], 16)
    cpu_split = cpu.strip().split(" ")
    cpu_pc = int(cpu_split[0], 16)
    cpu_res = int(cpu_split[4], 16)
    if((sim_pc != cpu_pc) or (sim_res != cpu_res)):
        print("SIM PC:{0} CPU PC:{1}   SIM RES:{2}  CPU RES:{3}".format(sim_pc, cpu_pc, sim_res, cpu_res))
