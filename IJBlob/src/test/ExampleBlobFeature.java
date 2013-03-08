package test;

import ij.blob.*;

public class ExampleBlobFeature extends CustomBlobFeature {

	public double myFancyFeature(Integer a, Float b){
		double feature = b*getBlob().getEnclosedArea()*a;
		return feature;
	}
	
	public int mySecondFancyFeature(Integer a, Double b){
		int feature = (int)(b*getBlob().getEnclosedArea()*a);
		return feature;
	}
	
	public int myThirdFancyFeature(){
		return 5;
	}
	
	
}
