package org.cloudbus.cloudsim.examples;

/**  
*@Description: 基因遗传染色体   
*/ 

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;

public class Chromosome {
	private int[] gene;//基因序列
	private double score;//对应的函数得分
	
	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * @param size
	 * 随机生成基因序列
	 */
	public Chromosome(int size) {
		if (size <= 0) {
			return;
		}
		initGeneSize(size);
		Random random = new Random();
		for (int i = 0; i < size; i++) {
			
			//gene[i] = (int) (Math.random()*10) % 5;
			gene[i] = random.nextInt(5);
		}
	}
	
	/**
	 * 生成一个新基因
	 */
	public Chromosome() {
		
	}
	
	/**
	 * @param c
	 * @return
	 * @Author:lulei  
	 * @Description: 克隆基因
	 */
	public static Chromosome clone(final Chromosome c) {
		if (c == null || c.gene == null) {
			return null;
		}
		Chromosome copy = new Chromosome();
		copy.initGeneSize(c.gene.length);
		for (int i = 0; i < c.gene.length; i++) {
			copy.gene[i] = c.gene[i];
		}
		return copy;
	}
	
	/**
	 * @param size
	 * @Author:lulei  
	 * @Description: 初始化基因长度
	 */
	private void initGeneSize(int size) {
		if (size <= 0) {
			return;
		}
		gene = new int[size];
	}
	
	
	/**
	 * @param c1
	 * @param c2
	 * @Author:lulei  
	 * @Description: 遗传产生下一代
	 */
	public static List<Chromosome> genetic(Chromosome p1, Chromosome p2) {
		if (p1 == null || p2 == null) { //染色体有一个为空，不产生下一代
			return null;
		}
		if (p1.gene == null || p2.gene == null) { //染色体有一个没有基因序列，不产生下一代
			return null;
		}
		if (p1.gene.length != p2.gene.length) { //染色体基因序列长度不同，不产生下一代
			return null;
		}
		Chromosome c1 = clone(p1);
		Chromosome c2 = clone(p2);
		//随机产生交叉互换位置
		int size = c1.gene.length;
		int a = ((int) (Math.random() * size)) % size;
		int b = ((int) (Math.random() * size)) % size;
		int min = a > b ? b : a;
		int max = a > b ? a : b;
		//对位置上的基因进行交叉互换
		for (int i = min; i <= max; i++) {
			int t = c1.gene[i];
			c1.gene[i] = c2.gene[i];
			c2.gene[i] = t;
		}
		List<Chromosome> list = new ArrayList<Chromosome>();
		list.add(c1);
		list.add(c2);
		return list;
	}
	
	/**
	 * @param num
	 * @Author:lulei  
	 * @Description: 基因num个位置发生变异
	 */
	public void mutation(int num) {
		//允许变异
		int size = gene.length;
		Random random = new Random();
		for (int i = 0; i < num; i++) {
			//寻找变异位置
			int at = ((int) (Math.random() * size)) % size;
			//变异后的值
			//int bool = (int) (Math.random() * 10) % 5;
			int bool = random.nextInt(5);
			gene[at] = bool;
		}
	}
	
	/**
	 * @return
	 * @Author:lulei  
	 * @Description: 将基因转化为对应的数字
	 */
	public int[] getNum() {
		return gene;
	}
}
