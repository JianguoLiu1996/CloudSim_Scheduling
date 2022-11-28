package myTest;

import java.util.Random;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Collections;
import java.util.Comparator;
import org.cloudbus.cloudsim.Log;

public class Test {
	public Test (){
		
	}
	public static void main(String[] args) {
		long[] length = new long[10];
		long[] fileSize = new long[10]; 
		Random random = new Random();
		random.setSeed(10000L);
		for(int i = 0 ; i < 10 ; i ++) {
			length[i] = random.nextInt(4000) + 1000;
			Log.printLine(length[i]+'	');
        }
		random.setSeed(5000L);
		Log.printLine("文件长度："+'\n');
		for(int i = 0 ; i < 10 ; i ++) {
			fileSize[i] = random.nextInt(20000) + 10000;
			Log.printLine(fileSize[i]+'	');
        }
	}

}
