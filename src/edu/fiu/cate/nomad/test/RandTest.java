package edu.fiu.cate.nomad.test;

import java.util.Random;

public class RandTest {

	public static void main(String[] args) {
		Random rand=new Random(7+3);
		System.out.println("Page "+(rand.nextInt(123)+1) +", col "+(rand.nextInt(10)+1)+", row "+(rand.nextInt(4)+1));	

	}

}
