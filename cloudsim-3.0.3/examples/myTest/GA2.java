package myTest;

import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

/**  
*@Description:     
*/ 

public class GA2 extends GeneticAlgorithm{
	private static List<Cloudlet> cloudletList;//云任务列表
	private static List<Vm> vmList;//虚拟机列表
	
	public GA2(List<Cloudlet> cL, List<Vm>vL) {
		super(cL.size()); 
		GA2.cloudletList = cL;
		GA2.vmList = vL;
	}
	
	@Override
	public double changeX(Chromosome chro) {//计算得分
		int[] gene = chro.getNum();
		double x;
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
		x = tempx[0];
		for(int i=0; i<5; i++) {
			if(x < tempx[i]) {
				x = tempx[i];
			}
		}
		return x;
	}

	@Override
	public double caculateY(double x) {
		// TODO Auto-generated method stub  
		return x;
	}

}