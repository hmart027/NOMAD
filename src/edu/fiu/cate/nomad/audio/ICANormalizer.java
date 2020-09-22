package edu.fiu.cate.nomad.audio;

import java.io.Serializable;

public class ICANormalizer implements Serializable{

	private static final long serialVersionUID = -8132178991353065469L;
	
	int chanCount;
	public double[] maxs, mins, mean, std;
	
	protected ICANormalizer(double[][] ica){
		chanCount = ica.length;
		maxs = new double[ica.length];
		mins = new double[ica.length];
		mean = new double[ica.length];
		std = new double[ica.length];
		double w = 1d/(double)ica[0].length;
		for(int i=0; i<ica.length; i++){
			maxs[i] = ica[i][0];
			mins[i] = ica[i][0];
			double sos = 0;
			for(int s=1; s<ica[0].length; s++){
				if(ica[i][s]>maxs[i]) maxs[i] = ica[i][s];
				if(ica[i][s]<mins[i]) mins[i] = ica[i][s];
				mean[i] += ica[i][s];
				sos += Math.pow(ica[i][s], 2);
			}
			std[i] = Math.sqrt((sos-mean[i]*mean[i]*w)/(double)(ica[0].length-1));
			mean[i] /= (double)ica[0].length;
		}
	}
	
	public static ICANormalizer getInstance(double[][] ica){
		if(ica==null || ica.length==0 || ica[0].length==0)
			return null;
		return new ICANormalizer(ica);
	}
	
	public double[] normalize(double[] ica){
		double[] normOutput = new double[chanCount];
		for(int i=0; i<chanCount; i++){
//			double max = maxs[i];
//			double min = mins[i];
			double c = 1d/(6d*std[i]);
			normOutput[i] = c*(ica[i]-mean[i]);
		}
		return normOutput;
	}

}
