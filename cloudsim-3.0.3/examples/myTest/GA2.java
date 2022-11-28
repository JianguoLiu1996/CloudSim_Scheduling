package myTest;

import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

/**  
*@Description:  
* 使用遗传算法计算最优的任务分配策略，
* 然后根据遗传算法结果，进行实际任务分配。
*/ 

public class GA2 extends GeneticAlgorithm{
	private static List<Cloudlet> cloudletList;//云任务列表
	private static List<Vm> vmList;//虚拟机列表
	
	public GA2(List<Cloudlet> cL, List<Vm>vL) {
		super(cL.size()); //调用父方法，设置基因长度。
		
		//获取任务列表和虚拟机列表
		GA2.cloudletList = cL;
		GA2.vmList = vL;
	}
	
	// 计算染色体得分。
	// 输入一个染色体
	// 返回该染色体译码得到的分配策略，在分配策略下，任务的最迟完成时间。
	public double changeX(Chromosome chro) {
		int[] gene = chro.getNum();
		double x_completeTime;//储存染色体最迟完成时间。
		double tempx[] = new double[] {0.0, 0.0, 0.0, 0.0, 0.0};
		for(int i=0; i<gene.length; i++) {
			switch(gene[i]) {
				case 0:
					tempx[0] += (cloudletList.get(i).getCloudletFileSize()/vmList.get(gene[i]).getBw()) +
					(cloudletList.get(i).getCloudletLength()/vmList.get(gene[i]).getMips());
					break;
				case 1:
					tempx[1] += (cloudletList.get(i).getCloudletFileSize()/vmList.get(gene[i]).getBw()) +
					(cloudletList.get(i).getCloudletLength()/vmList.get(gene[i]).getMips());
					break;
				case 2:
					tempx[2] += (cloudletList.get(i).getCloudletFileSize()/vmList.get(gene[i]).getBw()) +
					(cloudletList.get(i).getCloudletLength()/vmList.get(gene[i]).getMips());
					break;
				case 3:
					tempx[3] += (cloudletList.get(i).getCloudletFileSize()/vmList.get(gene[i]).getBw()) +
					(cloudletList.get(i).getCloudletLength()/vmList.get(gene[i]).getMips());
					break;
				case 4:
					tempx[4] += (cloudletList.get(i).getCloudletFileSize()/vmList.get(gene[i]).getBw()) +
					(cloudletList.get(i).getCloudletLength()/vmList.get(gene[i]).getMips());
					break;
				default:break;
			}
		}
		x_completeTime = tempx[0];
		for(int i=1; i<5; i++) {
			if(x_completeTime < tempx[i]) {
				x_completeTime = tempx[i];
			}
		}
		return (1/x_completeTime);//注意，返回的时任务最迟完成时间的倒数。
	}

	@Override
	public double caculateY(double x) {
		// TODO Auto-generated method stub  
		return x;
	}
}