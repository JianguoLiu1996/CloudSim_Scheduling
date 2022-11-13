package myTest;


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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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
public class MyAllocationMinMin {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;
	
	/** 设置全局变量，云任务数量和虚拟机数量*/
	private static int cloudletNum = 1000;//云任务数量
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
//			long[] length = new long[] {
//					19365, 49809, 30218, 44157, 16754, 26785,12348, 28894, 33889, 58967,
//					35045, 12236, 20085, 31123, 32227, 41727, 51017, 44787, 65854, 39836,
//					18336, 20047, 31493, 30727, 31017, 30218, 44157, 16754, 26785, 12348,
//					49809, 30218, 44157, 16754, 26785, 44157, 16754, 26785, 12348, 28894};//云任务指令数
//			long[] fileSize = new long[] {
//					30000, 50000, 10000, 40000, 20000, 41000, 27000, 43000, 36000, 33000,
//					23000, 22000, 41000, 42000, 24000, 23000, 36000, 42000, 46000, 33000,
//					23000, 22000, 41000, 42000, 50000, 10000, 40000, 20000, 41000, 10000,
//					40000, 20000, 41000, 27000, 30000, 50000, 10000, 40000, 20000, 17000};//云任务文件大小
			long[] length = new long[cloudletNum];
			long[] fileSize = new long[cloudletNum]; 
			Random random = new Random();
			random.setSeed(10000L);
			for(int i = 0 ; i < cloudletNum ; i ++) {
				length[i] = random.nextInt(4000) + 1000;
	        }
			random.setSeed(5000L);
			for(int i = 0 ; i < cloudletNum ; i ++) {
				fileSize[i] = random.nextInt(20000) + 10000;
	        }
			
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
			
			//使用贪心算法分配任务
			bindCloudletsToVmsTimeAwared();
					
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
		
		double[] executeTimeOfVM = new double[vmNum];//记录每个虚拟机VM的最后一个任务完成时间
		double meanOfExecuteTimeOfVM = 0;//虚拟机平均运行时间
		for(int i=0;i<vmNum;i++) {//初始化数组
			executeTimeOfVM[i] = 0;
		}
		double LB=0;//负载平衡因子
		
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
				//计算每个虚拟机最后完成的时间
				if(cloudlet.getFinishTime() > executeTimeOfVM[cloudlet.getVmId()]) {
					executeTimeOfVM[cloudlet.getVmId()] = cloudlet.getFinishTime();
				}
			}
		}
		
		//求所有虚拟机平均运行时间
		for(int i=0;i<vmNum;i++) {
			meanOfExecuteTimeOfVM += executeTimeOfVM[i];
			Log.printLine("VM" + i +" executeTime:" + executeTimeOfVM[i] + "\n");
		}
		meanOfExecuteTimeOfVM /= vmNum;
		Log.printLine("meanOfExecuteTimeOfVM:" + meanOfExecuteTimeOfVM + "\n");
		
		//计算负载平衡因子
		for(int i=0; i<vmNum; i++) {
			LB += Math.pow(executeTimeOfVM[i]-meanOfExecuteTimeOfVM, 2);
		}
		LB = Math.sqrt(meanOfExecuteTimeOfVM/vmNum);
		Log.printLine("LB:" + LB + "\n");
	}
	

	//贪心算法
	//Cloudlet根据MI降序排列,MinMin算法
	public static void bindCloudletsToVmsTimeAwared(){
        int cloudletNum=cloudletList.size();
        int vmNum=vmlist.size();
        
        //time[i][j] 表示任务i在虚拟机j上的执行时间
        double[][] time=new double[cloudletNum][vmNum];
        
        //cloudletList按MI降序排列, vm按MIPS升序排列
        Collections.sort(cloudletList,new MyAllocationMinMin().new CloudletComparator());
        Collections.sort(vmlist,new MyAllocationMinMin().new VmComparator());
        
        //For test (查看cloudletList和vmlist排序结果)
        System. out .println("///////////For test///////////////");
        for(int i=0;i<cloudletNum;i++){
            System. out .print(cloudletList.get(i).getCloudletId()+":"+cloudletList.get
                    (i).getCloudletLength()+" ");
        }
        System. out .println();
        for (int i=0;i< vmNum;i++){
            System. out .print(vmlist.get(i).getId()+":"+vmlist.get(i).getMips()+" ");
        }
        System. out .println();
        System. out .println("//////////////////////////////////");
        
        //计算time[i][j]，即：计算任务i在虚拟机j上执行时间
       for (int i=0;i<cloudletNum;i++){
    	   for(int j=0;j<vmNum;j++){
    		   time[i][j]= (double)cloudletList.get(i).getCloudletLength()/vmlist.get(j).getMips() 
    				   + (double)cloudletList.get(i).getCloudletFileSize()/vmlist.get(j).getBw();
    		   System. out .print("time["+i+"]["+j+"]="+time[i][j]);
    	   }
         
    	   //For test,输出time[i][j]计算结果
    	   System. out .println();
       }

       double[] vmLoad=new double[vmNum];//在某个虚拟机上任务的总执行时间
       int[] vmTasks=new int[vmNum]; //在某个Vm上运行的任务数量
       double minLoad=0;//记录当前任务分配方式的最优值
       int idx=0;//记录当前任务最优分配方式对应的虚拟机列号
       
       //第一个cloudlet分配给最快的vm
       vmLoad[vmNum-1]=time[0][vmNum-1];
       vmTasks[vmNum-1]=1;
       cloudletList.get(0).setVmId(vmlist.get(vmNum-1).getId());
       
       //遍历任务，对于每个虚拟机，如果该任务加到这个虚拟机上时，总执行时间最短，则将该任务分配给该虚拟机
       for(int i=1;i<cloudletNum;i++){
    	   minLoad=vmLoad[vmNum-1]+time[i][vmNum-1];
    	   idx=vmNum-1;
    	   
    	   //对于每个虚拟机，如果任务加到虚拟机上，所有的任务执行时间最短，则将该任务分配给该虚拟机
    	   for(int j=vmNum-2;j>=0;j--){
    		   //如果当前虚拟机未分配任务,则比较完当前任务分配给该虚拟机是否最优
    		   if(vmLoad[j]==0){
    			   if(minLoad>=time[i][j]) {
    				   idx = j;
    			   }
    			   break;
    		   }
    		   if(minLoad>vmLoad[j]+time[i][j]){
    			   minLoad=vmLoad[j]+time[i][j];
    			   idx=j;
    		   }
    		   //简单的负载均衡
    		   else if(minLoad==vmLoad[j]+time[i][j]&&vmTasks[j]<vmTasks[idx]) {
    			   idx = j;
    		   }
    	   }
    	   vmLoad[idx]+=time[i][idx];
    	   vmTasks[idx]++;
    	   cloudletList.get(i).setVmId(vmlist.get(idx).getId());
    	   System. out .print(i+"th "+"vmLoad["+idx+"]="+vmLoad[idx]+"minLoad="+minLoad);
    	   System. out .println();
       }
	}
	
	//Cloudlet根据length降序排列
	private class CloudletComparator implements Comparator<Cloudlet>{
        @Override
        public int compare(Cloudlet cl1, Cloudlet cl2){
            return (int)(cl2.getCloudletLength()-cl1.getCloudletLength());
        }
    }
    
    //Vm根据MIPS升序排列
        private class VmComparator implements Comparator<Vm>{
        @Override
        public int compare(Vm vm1, Vm vm2){
            return (int)(vm1.getMips()-vm2.getMips());
        }
    }        
}

