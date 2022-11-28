package myTest;

/**  
*@Description: 
* 遗传算法具体实现
* 该类为虚拟类，需要进行继承该类，并重写类中的方法，实现遗传算法。
*/ 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

 
public abstract class GeneticAlgorithm {
	private List<Chromosome> population = new ArrayList<Chromosome>();
	private int popSize = 40*3;//种群数量，需手动设置
	private int geneSize;//基因最大长度
	private int maxIterNum = 40*10;//最大迭代次数，需手动设置
	private double mutationRate = 0.05;//基因变异的概率，需手动设置
	private int maxMutationNum = 40/5;//最大变异步长，需手动设置
	
	private int generation = 1;//当前遗传到第几代
	
	private double bestScore;//最好得分
	private double worstScore;//最坏得分
	private double totalScore;//总得分
	private double averageScore;//平均得分
	
	//private double x = Double.POSITIVE_INFINITY; //记录历史种群中最好的X值，基因序列最好的适应值，初始化为无穷大。
	private double x; //记录历史种群中最好的X值，基因序列最好的适应值。
	private static int[] bestGenex = new int[1000];//记录历史最好的基因序列
	//private double y = Double.POSITIVE_INFINITY; //记录历史种群中最好的Y值，在CloudSim实现中y==x，初始化为无穷大。
	private double y;//记录历史种群中最好的Y值.
	private int geneI;//x y所在代数
	
	// 设置基因长度
	public GeneticAlgorithm(int geneSize) {
		this.geneSize = geneSize;
	}
	
	// 主方法，使用遗传算法计算最好基因，并返回最好基因。
	public int[] caculte() {
		// 初始化种群
		generation = 1;
		init();
		
		// 当种群代数不满足要求时，迭代执行进化步骤。
		while (generation < maxIterNum) {
			//种群遗传
			evolve();
			print();
			generation++;
		}
		
		//输出最好的基因序列结果和适应值
		System.out.print("The best Gene list is ");
		for(int i=0; i<geneSize; i++) {
			System.out.print(bestGenex[i] + "; ");
		}
		System.out.println("\nThe best socre is " + y);
		return bestGenex;
	}
	
	/**
	 * @Description: 输出结果
	 */
	private void print() {
		System.out.println("--------------------------------");
		System.out.println("the generation is:" + generation);
		System.out.println("the best y is:" + bestScore);
		System.out.println("the worst fitness is:" + worstScore);
		System.out.println("the average fitness is:" + averageScore);
		System.out.println("the total fitness is:" + totalScore);
		System.out.println("geneI:" + geneI + "\tx:" + x + "\ty:" + y);
	}
	
	
	/**
	 * @Description: 
	 * 初始化种群
	 */
	private void init() {
		// 随机生成初始化种群。
		for (int i = 0; i < popSize; i++) {
			//population = new ArrayList<Chromosome>();
			Chromosome chro = new Chromosome(geneSize);
			population.add(chro);
		}
		
		// 计算每个基因序列的得分，并计算种群的最好、最坏、平均得分。
		caculteScore();
		
		//给最好的基因序列赋初值
		//bestGenex = Arrays.copyOf(population.get(0).getNum(), geneSize);
	}
	
	/**
	 * @Description:种群进行遗传
	 */
	private void evolve() {
		List<Chromosome> childPopulation = new ArrayList<Chromosome>();
		//step1，生成下一代种群
		while (childPopulation.size() < popSize) {
			Chromosome p1 = getParentChromosome();//轮盘赌法选择可以遗传下一代的染色体。
			Chromosome p2 = getParentChromosome();//轮盘赌法选择可以遗传下一代的染色体。
			List<Chromosome> children = Chromosome.genetic(p1, p2);//对父代基因进行交叉变换。
			if (children != null) {
				for (Chromosome chro : children) {
					childPopulation.add(chro);
				}
			} 
		}
		//新种群替换旧种群
		List<Chromosome> t = population;
		population = childPopulation;
		t.clear();
		t = null;
		
		//step2，基因突变
		mutation();
		
		//step3，计算新种群的适应度
		caculteScore();
	}
	
	/**
	 * @Description: 轮盘赌法选择可以遗传下一代的染色体
	 */
	private Chromosome getParentChromosome (){
		double slice = Math.random() * totalScore;
		double sum = 0;
		for (Chromosome chro : population) {
			sum += chro.getScore();
			if (sum > slice && chro.getScore() >= averageScore) {
				return chro;
			}
		}
		return null;
	}
	
	/**
	 * @Description: 计算种群适应度
	 * 遍历种群中的每一个基因序列，计算基因序列得分，和计算种群中的最好、最坏、总得分、平均得分。
	 */
	private void caculteScore() {
		setChromosomeScore(population.get(0));
		bestScore = population.get(0).getScore();
		worstScore = population.get(0).getScore();
		totalScore = 0;
		
		for (Chromosome chro : population) {
			setChromosomeScore(chro);//计算该基因序列的得分（适应度）。
			if (chro.getScore() > bestScore) { //设置最好基因值。
				bestScore = chro.getScore();
				if (y < bestScore) {
					x = changeX(chro);
					y = bestScore;
					
					//获得最好的基因序列，并记录。
					bestGenex = Arrays.copyOf(chro.getNum(), geneSize);
					geneI = generation;
				}
			}
			if (chro.getScore() < worstScore) { //设置最坏基因值
				worstScore = chro.getScore();
			}
			totalScore += chro.getScore();
		}
		averageScore = totalScore / popSize;
		//因为精度问题导致的平均值大于最好值，将平均值设置成最好值
		averageScore = averageScore > bestScore ? bestScore : averageScore;
	}
	
	/**
	 * 基因突变
	 */
	private void mutation() {
		for (Chromosome chro : population) {
			if (Math.random() < mutationRate) { //发生基因突变
				int mutationNum = (int) (Math.random() * maxMutationNum);//根据最大变异步长，随机生成最大变异步长，进行突变。
				chro.mutation(mutationNum);
			}
		}
	}
	
	/**
	 * @Description: 设置染色体得分
	 */
	private void setChromosomeScore(Chromosome chro) {
		if (chro == null) {
			return;
		}
		double x_temp = changeX(chro);
		double y_temp = caculateY(x_temp);
		chro.setScore(y_temp);

	}
	
	/**
	 * @Description: 将二进制转化为对应的X
	 */
	public abstract double changeX(Chromosome chro);
	
	
	/**
	 * @Description: 根据X计算Y值 Y=F(X)
	 */
	public abstract double caculateY(double x);

	public void setPopulation(List<Chromosome> population) {
		this.population = population;
	}

	public void setPopSize(int popSize) {
		this.popSize = popSize;
	}

	public void setGeneSize(int geneSize) {
		this.geneSize = geneSize;
	}

	public void setMaxIterNum(int maxIterNum) {
		this.maxIterNum = maxIterNum;
	}

	public void setMutationRate(double mutationRate) {
		this.mutationRate = mutationRate;
	}

	public void setMaxMutationNum(int maxMutationNum) {
		this.maxMutationNum = maxMutationNum;
	}

	public double getBestScore() {
		return bestScore;
	}

	public double getWorstScore() {
		return worstScore;
	}

	public double getTotalScore() {
		return totalScore;
	}

	public double getAverageScore() {
		return averageScore;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
}
