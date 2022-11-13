package org.cloudbus.cloudsim.examples;
/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * A simple example showing how to create a datacenter with one host and run one
 * cloudlet on it.
 */
public class MyAllocationTest {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;
	
	/** 设置全局变量，云任务数量和虚拟机数量*/
	private static int cloudletNum = 40;//云任务数量
	private static int vmNum = 5;//虚拟机数量

	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {

		Log.printLine("Starting CloudSimExample1...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			//第一步，即，初始化
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();//日历
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			//初始化CloudSim库
			CloudSim.init(num_user, calendar, trace_flag);

			
			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			//第二步，创建数据中心
			Datacenter datacenter0 = createDatacenter("Datacenter_0");

			
			// Third step: Create Broker
			//第三步，创建代理
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			
			// Fourth step: Create five virtual machine
			//第四步，创建5个虚拟机
			vmlist = new ArrayList<Vm>();

			// VM description（虚拟机参数）
			int vmid = 0;
			int[] mips = new int[] {278, 289, 132, 209, 286};//频率
			long size = 10000; // image size (MB)
			int ram = 2048; // vm memory (MB)
			long[] bw = new long[] {1000, 1200, 1100, 1300, 900};
			int pesNumber = 1; // number of cpus
			String vmm = "Xen"; // VMM name

			// create VM
			// add the VM to the vmList		
			for(int i=0; i<vmNum; i++) {
				vmlist.add(new Vm(vmid, brokerId, mips[i], pesNumber, ram, bw[i], size, vmm, new CloudletSchedulerTimeShared()));
				vmid++;
			}
			
			// submit vm list to the broker(将虚拟机列表提交给代理商)
			broker.submitVmList(vmlist);

			
			// Fifth step: Create one Cloudlet
			//第五步，创建40个任务
			cloudletList = new ArrayList<Cloudlet>();

			// Cloudlet properties（任务列表）
			int id = 0;
			long[] length = new long[] {
					19365, 49809, 30218, 44157, 16754, 26785,12348, 28894, 33889, 58967,
					35045, 12236, 20085, 31123, 32227, 41727, 51017, 44787, 65854, 39836,
					18336, 20047, 31493, 30727, 31017, 30218, 44157, 16754, 26785, 12348,
					49809, 30218, 44157, 16754, 26785, 44157, 16754, 26785, 12348, 28894};//云任务指令数
			long[] fileSize = new long[] {
					30000, 50000, 10000, 40000, 20000, 41000, 27000, 43000, 36000, 33000,
					23000, 22000, 41000, 42000, 24000, 23000, 36000, 42000, 46000, 33000,
					23000, 22000, 41000, 42000, 50000, 10000, 40000, 20000, 41000, 10000,
					40000, 20000, 41000, 27000, 30000, 50000, 10000, 40000, 20000, 17000};//云任务文件大小
			long outputSize = 300;
			UtilizationModel utilizationModel = new UtilizationModelFull();
			
			// add the cloudlet to the list
			for(int i=0; i<cloudletNum; i++) {
				Cloudlet cloudlet = new Cloudlet(id, length[i], pesNumber, fileSize[i], outputSize, utilizationModel, utilizationModel, utilizationModel);
				cloudlet.setUserId(brokerId);
				cloudletList.add(cloudlet);
				id++;
			}
					
			// submit cloudlet list to the broker.（当任务提交给代理商）
			broker.submitCloudletList(cloudletList);

			//测试
			//通过函数将任务和虚拟机绑定
			//bindCloudletToVmSimple();//此函数为简单绑定法，即轮巡绑定
			
			//GA操作，使用遗传算法计算结果进行分配任务
			GA2 test = new GA2(cloudletList,vmlist);
			int[] resultGene = test.caculte();
			for(int i=0; i<40; i++) {
				cloudletList.get(i).setVmId(vmlist.get(resultGene[i]).getId());
			}
			
			// Sixth step: Starts the simulation
			//第六步，开始仿真
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			
			//Final step: Print results when simulation is over
			//最后一步，输出仿真结果
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);

			Log.printLine("CloudSimExample1 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	/**
	 * Creates the datacenter.
	 *
	 * @param name the name
	 *
	 * @return the datacenter
	 */
	private static Datacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 1000;
		
		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int hostId = 0;
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000; //带宽
		
		for(int i=0; i<vmNum; i++) {
			// 3. Create PEs and add these into a list.
			peList.add(new Pe(i, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
			
			hostList.add(
					new Host(
						hostId,
						new RamProvisionerSimple(ram),
						new BwProvisionerSimple(bw),
						storage,
						peList,
						new VmSchedulerTimeShared(peList)
					)
				); // This is our machine
			hostId++;
		}

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
													// devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	/**
	 * Creates the broker.
	 *
	 * @return the datacenter broker
	 */
	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
	}

	// 实现简单任务绑定操作
	private static void bindCloudletToVmSimple() {
		int idx =0;
		for(int i=0; i<40; i++) {
			cloudletList.get(i).setVmId(vmlist.get(idx).getId());
			//broker.bindCloudletToVm(cloudletList.get(i).getCloudletId(),vmlist.get(idx).getId());
			idx = (idx + 1) % vmNum;
		}
	}
}
