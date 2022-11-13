package org.cloudbus.cloudsim.examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;

public class GA {
	private static List<Cloudlet> cloudletList;//云任务列表
	private static List<Vm> vmList;//虚拟机列表
	private static int goodResNum;//好结果个数
	private static List<PositionBean[]> goodTours;//好结果
	private int scale;//种群规模
	private int max_gen;//运行代数
	private double[] Pi;//种群中各个个体的累计概率
	private double Pc;//交叉概率
	private double Pm;//变异概率
	private int t;//当前代数
	public List<PositionBean> tour;//路径（解，任务分配给虚拟机的分法）
	public double tourLength;//路径长度（分配好后，总的花费时间）
	public long[] TL_task;//每个虚拟机的任务总量
	public long[] FL_task;//任务的文件长度
	private int[][] distance;//距离矩阵
	private int bestT;//最佳出现代数
	private double bestLength = Double.MAX_VALUE;//最佳结果
	private int[] bestTour;//最佳路径
	private int cls;//云任务个数
	private int VMs;//城市的个数（相当于虚拟机的个数）
	private int tasks;//任务个数
	private int sumVC;//云任务个数+虚拟机个数
	//初始种群，父代种群，行数表示种群规模，一行代表一个个体，即染色体，列表示染色体基因片段
	private int[][] oldPopulation;
	private int[][] newPopulation;//新的种群，子代种群
	private double[] fitness;//种群适应度，表示种群中各个个体的适应度
	private Random random = new Random();
	
	public GA() {
		
	}
	
	public GA(int scale, int max_gen,double Pc, double Pm, int goodResNum) {
		this.scale = scale;//种群规模
		this.max_gen = max_gen;//运行代数
		this.Pc = Pc;//交叉概率
		this.Pm = Pm;//变异概率
		this.goodResNum = goodResNum;//好结果个数
		fitness = new double[scale];//种群适应度，表示种群中各个个体的适应度
		Pi = new double[scale];//种群中各个个体的累计概率
		goodTours = new ArrayList<>();//好的结果
	}
	
	//初始化
	public void init(List<Cloudlet> cL, List<Vm>vL) {
		//读取数据
		cloudletList = cL;
		vmList = vL;
		
		//初始化
		cls = cloudletList.size();//任务数量
		VMs = vmList.size();//虚拟机数量
		//sumVC = cls + VMs;
		sumVC = cls + 1;
		TL_task = new long[VMs];//每个虚拟机任务的数量
		FL_task = new long[VMs];//任务的文件长度
		for(int i = 0; i < VMs; i ++) {
			TL_task[i] = 0;
			FL_task[i] = 0;
		}
		oldPopulation = new int[scale][sumVC - 1];
		newPopulation = new int[scale][sumVC - 1];//新种群
		bestTour = new int[sumVC - 1];
	}
	
	public List<List<PositionBean>> solve() {//解决
		List<List<PositionBean>> goodTours = new ArrayList<>();
		int i;
		int k;
		
		//初始化种群
		initGroup();
		
		//计算初始化种群适应度，Fitness[max]
		for(k = 0; k < scale; k++) {
			fitness[k] = evaluate(oldPopulation[k]);
		}
		
		//计算初始化种群中各个个体的累积概率，Pi[max]
		countRate();
		
		//输出信息
		System.out.println("Initial scale....");
		for(k = 0; k < scale; k++) {
			for(i = 0; i < sumVC -1; i++) {
				System.out.print(oldPopulation[k][i] + ",");
			}
			System.out.println();
			System.out.println("----" + fitness[k]);
		}
		
		for(t = 0; t < max_gen; t++) {
			//交叉、变异
			evolution();
			
			//将新种群newGroup复制到旧种群oldGroup中，准备下一代进化
			for(k = 0; k < scale; k++) {
				for(i = 0; i < sumVC -1; i++) {
					oldPopulation[k][i] = newPopulation[k][i];
				}
			}
			
			//计算种群适应度
			for(k = 0; k < scale; k++) {
				fitness[k] = evaluate(oldPopulation[k]);
			}
			
			//计算种群中各个个体的累计概率
			countRate();
		}
		
		//排序，给最后一代，找fitness最小的
		int[][] besRests = new int[goodResNum][sumVC -1];
		for(k = 0; k < scale; k++) {
			for(i = k+1; i < scale; i++) {
				if(fitness[k] > fitness[i]) {
					double temp = fitness[k];
					fitness[k] = fitness[i];
					fitness[i] = temp;
					int[] tRes = new int[sumVC -1];
					for(int j = 0; j < sumVC - 1; j++) {
						tRes[j] = oldPopulation[k][j];
					}
					for(int j = 0; j < sumVC -1; j++) {
						oldPopulation[k][j] = oldPopulation[i][j];
					}
					for(int j = 0; j < sumVC -1; j++) {
						oldPopulation[i][j] = tRes[j];
					}
				}
			}
		}
		
		for(k = 0; k < goodResNum; k++) {
			for(int j = 0; j < sumVC -1; j++) {
				besRests[k][j] = oldPopulation[k][j];
			}
		}
		
		System.out.println("Resout scale...");
		for(k = 0; k < scale; k++) {
			for(i = 0; i < sumVC -1; i++) {
				System.out.print(oldPopulation[k][i] + ",");
			}
			System.out.println();
			System.out.println("----" + fitness[k]);
		}
		System.out.println("最佳长度出现次数the number of occur best length：" + bestT);
		System.out.println("最佳长度the best lenght：" + bestLength);
		
		//解码
		List<PositionBean>bt = deCode(bestTour);
		System.out.println("分配方案：allocate formular");
		printCloudletLists(bt);
		for(int j = 0; j < besRests.length; j++) {
			List<PositionBean>pbs = deCode(besRests[j]);
			goodTours.add(pbs);
		}
		
		return goodTours;//DEBUG
	}
	
	void initGroup() {
		Set<Integer> hashSet = new HashSet<>();
		for(int i = 0; i < scale; i++) {//种群数，生产scale个个体
			int element;
			for(int j =0; j < sumVC -1; j++) {//染色体长度，生产sumVC-1个数。前：云任务，后：插板
				element = random.nextInt(sumVC -1);
				while(hashSet.contains(element)) {
					element = random.nextInt(sumVC -1);
				}
				
				hashSet.add(element);
				oldPopulation[i][j] = element;
			}
			hashSet.removeAll(hashSet);
		}
	}
	
	//计算totalFinishTime
	public double evaluate(int[] chromosome) {
		//解码
		List<PositionBean> tempTour = deCode(chromosome);
		double MaxFinishTime = 0.0;
		Map<Integer,Double> map = new HashMap<Integer,Double>();
		for(int i = 0; i < vmList.size(); i++) {
			map.put(i, 0.0);//初始化为0
		}
		
		//累加
		for(int i = 0; i < tempTour.size(); i++) {
			map.put(tempTour.get(i).vm, map.get(tempTour.get(i).vm) + tempTour.get(i).FinishTime);
		}
		
		for(int i = 0; i < vmList.size(); i++) {
			//取得最大
			if(MaxFinishTime < map.get(i)) {
				MaxFinishTime = map.get(i);
			}
		}
		
		return MaxFinishTime;
	}
	
	//解码(param chromosome: 一条染色体)
	private List<PositionBean> deCode(int[] chromosome){
		List<PositionBean> tempTour = new ArrayList<>();//云任务分配方案
		int curVm = 0;//默认从0号虚拟机开始
		List<Integer> clList = new ArrayList<>();
		for(int i = 0; i < chromosome.length; i++) {
			if(chromosome[i] >= cls) {
				//碰到虚拟机，就往里装任务
				for(int cl:clList) {
					PositionBean p = new PositionBean(curVm,cl);
					p.setTime = cloudletList.get(cl).getCloudletFileSize() * 1.0 / vmList.get(curVm).getBw();
					p.executeTime = cloudletList.get(cl).getCloudletLength() * 1.0 / vmList.get(curVm).getMips();
					p.FinishTime = p.sentTime + p.executeTime;
					tempTour.add(p);
				}
				
				//装完后，清空
				clList.removeAll(clList);
				curVm++;//虚拟机往后走
			}
			else {
				clList.add(chromosome[i]);
			}
			
			if(i + 1 == chromosome.length && clList.size() > 0) {
				//往最后一台虚拟机装任务
				for(int cl : clList) {
					PositionBean p = new PositionBean(curVm,cl);
					p.setTime = cloudletList.get(cl).getCloudletFileSize() * 1.0 / vmList.get(curVm).getBw();
					p.executeTime = cloudletList.get(cl).getCloudletLength() * 1.0 / vmList.get(curVm).getMips();
					p.FinishTime = p.sentTime + p.executeTime;
					tempTour.add(p);
				}
			}
		}
		
		return tempTour;
	}
	
	//计算种群中各个个体的累计概率，前提是已经计算出各个个体的适应度fitness[max]，作为赌轮选择策略一部分，Pi[max]
	void countRate() {
		int k;
		double sumFitness = 0;//适应度总和
		double[] tempf = new double[scale];
		for(k = 0; k < scale; k++) {
			tempf[k] = 1.0 / fitness[k];//取倒数，时间越短适应度越大
			sumFitness += tempf[k];
		}
		Pi[0] = tempf[0] / sumFitness;//0-pi[0]表示一个个体被选中到的累计概率区域
		for(k = 1; k < scale; k++) {
			Pi[k] = tempf[k] / sumFitness + Pi[k -1];
		}
	}
	
	//进化函数，保留最好染色体不进行交叉变异
	public void evolution() {
		int k;
		
		//挑选某代种群中适应度最高的个体
		selectBestGh();
		
		//轮赌选择策略挑选scale -1个下一代个体
		select();
		
		float r;
		for(k = 1; k + 1 < scale / 2; k = k + 2) {
			r = random.nextFloat();//产生概率
			if(r < Pc) {
				OXCross(k, k+1);//进行交叉
			}
			else {
				r = random.nextFloat();//产生概率
				
				//变异
				if(r < Pm) {
					OnCVariation(k);
				}
				r = random.nextFloat();//产生概率
				
				//变异
				if(r < Pm) {
					OnCVariation(k + 1);
				}
			}
		}
		
		//如果剩最后一个染色体没有交叉L-1和变异
		if(k == scale / 2 - 1) {
			r = random.nextFloat();//产生概率
			if(r < Pm) {
				OnCVariation(k);
			}
		}
	}
	
	//挑选某代种群中适应度最高的个体（fitness最小的），直接复制到子代中，实行精英保留策略
	public void selectBestGh() {
		int k, i, maxid;
		double maxEvaluation;
		
		maxid = 0;
		maxEvaluation = fitness[0];
		for(k = 1; k < scale; k++) {
			if(maxEvaluation > fitness[k]) {
				maxEvaluation = fitness[k];
				maxid = k;
			}
		}
		
		//bestLength越小越好
		if(bestLength > maxEvaluation) {
			bestLength = maxEvaluation;
			bestT = t;//最好的染色体出现代数；
			for(i = 0; i < sumVC -1; i++) {
				bestTour[i] = oldPopulation[maxid][i];
			}
		}
		
		//将当代种群中适应度最高的染色体k复制到新种群中，排在第一位0
		copyGh(0, maxid);
	}
	
	//复制染色体，k表示新染色体在种群中的位置，kk表示旧的染色体在种群中的位置
	public void copyGh(int k, int kk) {
		for(int i = 0; i < sumVC - 1; i++) {
			newPopulation[k][i] = oldPopulation[kk][i];
		}
	}
	
	//赌轮选择策略挑选
	public void select() {
		int k, i, selectId;
		float ran1;
		for(k = 1; k < scale; k++) {
			ran1 = random.nextFloat();
			//生产方式
			for(i = 0; i < scale; i++) {
				if(ran1 <= Pi[i]) {
					break;
				}
			}
			selectId = i;
			copyGh(k, selectId);
		}
	}
	
	//交叉算子，染色体交叉产生不同子代染色体
	public void OXCross(int k1, int k2) {
		int i, j, k, flag;
		int ran1, ran2, temp;
		int[] Gh1 = new int[sumVC - 1];
		int[] Gh2 = new int[sumVC - 1];
		//Random random = new Random(System.currentTimeMillis());
		ran1 = random.nextInt(sumVC -1);
		ran2 = random.nextInt(sumVC -1);
		while(ran1 == ran2) {
			ran2 = random.nextInt(sumVC - 1);
		}
		if(ran1 > ran2) {//确保ran1 < ran2
			temp = ran1;
			ran1 = ran2;
			ran2 = temp;
		}
		
		//将染色体1中的第三部分移到染色体2的首部0-ran1, ran1-ran2, ran2-48
		for(i=0, j=ran2; j<sumVC-1; i++,j++) {
			Gh2[i] = newPopulation[k1][j];
		}
		
		flag = i;//染色体2原基因开始位置
		for(k=0,j=flag; j<sumVC-1;) {//染色体长度，用k2的顺序去补全Gh2
			Gh2[j] = newPopulation[k2][k++];
			for(i=0; i<flag; i++) {
				if(Gh2[i] == Gh2[j]) {
					break;
				}
			}
			if(i == flag) {
				j++;
			}
		}
		flag = ran1;
		for(k=0,j=0; k<sumVC-1;) {//染色体长度
			Gh1[j] = newPopulation[k1][k++];
			for(i=0; i<flag; i++) {
				if(newPopulation[k2][i] == Gh1[j]) {
					break;
				}
			}
			if(i==flag) {
				j++;
			}
		}
		
		flag = sumVC-1-ran1;
		for(i=0,j=flag; j<sumVC-1;j++,i++) {
			Gh1[j] = newPopulation[k2][i];
		}
		
		for(i=0; i<sumVC-1; i++) {
			newPopulation[k1][i] = Gh1[i];//交叉完毕放回种群
			newPopulation[k2][i] = Gh2[i];//交叉完毕放回种群
		}
	}
	
	//多次对换变异算子
	public void OnCVariation(int k) {
		int ran1, ran2, temp;
		int count;
		count = random.nextInt(sumVC-1);
		for(int i=0; i<count; i++) {
			ran1 = random.nextInt(sumVC-1);
			ran2 = random.nextInt(sumVC-1);
			while(ran1 == ran2) {
				ran2 = random.nextInt(sumVC-1);
			}
			temp = newPopulation[k][ran1];
			newPopulation[k][ran1] = newPopulation[k][ran2];
			newPopulation[k][ran2] = temp;
		}
	}
	
	private static void printCloudletLists(List<PositionBean> bestRes) {
		String indent = "	";
		Log.printLine();
		Log.printLine("============OUTPUT=============");
		Log.printLine("Cloudlet ID"
				+ indent + "VM ID"
				+ indent + indent + "Start Time"
				+ indent + "Send Time"
				+ indent + "Execute Time"
				+ indent + "Finish Time");
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		for(int i=0; i<vmList.size(); i++) {
			map.put(i, 0.0);//startTime初始化为0
		}
		
		for(PositionBean pb : bestRes) {
			pb.FinishTime = map.get(pb.vm) + pb.sentTime + pb.executeTime;
			Log.printLine(
					indent + pb.task
					+ indent + indent + indent + pb.vm
					+ indent + indent + indent + (double)Math.round(map.get(pb.vm)*100)/100
					+ indent + indent + (double)Math.round(pb.sentTime*100)/100
					+ indent + indent + (double)Math.round(pb.executeTime*1000)/1000
					+ indent + indent + indent + (double)Math.round(pb.FinishTime*1000)/1000);
			map.put(pb.vm, map.get(pb.vm) + pb.sentTime + pb.executeTime);
		}
	}
}
