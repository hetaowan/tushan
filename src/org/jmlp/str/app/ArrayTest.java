package org.jmlp.str.app;

public class ArrayTest {

	public void testArr()
	{
		double[] a=new double[2];
		a[0]=1;
		a[1]=2;
		change(a);
		System.out.println("a[0]="+a[0]);
		System.out.println("a[1]="+a[1]);
	}
	
	public void change(double[] arr)
	{
		for(int i=0;i<arr.length;i++)
		{
			arr[i]=arr[i]+1;
		}
	}
	
	public static void main(String[] args)
	{
		ArrayTest atest=new ArrayTest();
		atest.testArr();
	}
	
}
