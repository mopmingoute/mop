/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DistribucionNormal is part of MOP.
 *
 * MOP is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * MOP is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MOP. If
 * not, see <https://www.gnu.org/licenses/>.
 */

package procEstocUtils;

import utilitarios.Tabla1V;

public class DistribucionNormal {
	
//  En parte bajada de  https://introcs.cs.princeton.edu/java/21function/Gaussian.java.html
//	Copyright � 2000�2017, Robert Sedgewick and Kevin Wayne.
//	Last updated: Fri Oct 20 14:12:12 EDT 2017.
		
	
	/******************************************************************************
	 *  Compilation:  javac Gaussian.java
	 *  Execution:    java Gaussian x mu sigma
	 *
	 *  Function to compute the Gaussian pdf (probability density function)
	 *  and the Gaussian cdf (cumulative density function)
	 *
	 *  % java Gaussian 820 1019 209
	 *  0.17050966869132111
	 *
	 *  % java Gaussian 1500 1019 209
	 *  0.9893164837383883
	 *
	 *  % java Gaussian 1500 1025 231
	 *  0.9801220907365489
	 *
	 *  The approximation is accurate to absolute error less than 8 * 10^(-16).
	 *  Reference: Evaluating the Normal Distribution by George Marsaglia.
	 *  http://www.jstatsoft.org/v11/a04/paper
	 *
	 ******************************************************************************/

	private static double[] x;
	private static double[] probAcumDesde0;
	private static Tabla1V tablacdf; 
	private static Tabla1V tablaInversaCdf;
	
	static{
		x = new double[410];
		double xi = 0.0;
		for(int i=0; i<410; i++){
			x[i] = xi;
			xi += 0.01;
		}
		
		// Tabla de probabilidad acumulada a partir de x=0, en la normal estandar, valores entre -4.9 y 4.9
		probAcumDesde0 = new double[]  // valores desde 0 a 4 cada 0.01 tomados de https://www.itl.nist.gov/div898/handbook/eda/section3/eda3671.htm				
			{
					0,0.00399,0.00798,0.01197,0.01595,0.01994,0.02392,0.0279,0.03188,0.03586
					,0.03983,0.0438,0.04776,0.05172,0.05567,0.05962,0.06356,0.06749,0.07142,0.07535
					,0.07926,0.08317,0.08706,0.09095,0.09483,0.09871,0.10257,0.10642,0.11026,0.11409
					,0.11791,0.12172,0.12552,0.1293,0.13307,0.13683,0.14058,0.14431,0.14803,0.15173
					,0.15542,0.1591,0.16276,0.1664,0.17003,0.17364,0.17724,0.18082,0.18439,0.18793
					,0.19146,0.19497,0.19847,0.20194,0.2054,0.20884,0.21226,0.21566,0.21904,0.2224
					,0.22575,0.22907,0.23237,0.23565,0.23891,0.24215,0.24537,0.24857,0.25175,0.2549
					,0.25804,0.26115,0.26424,0.2673,0.27035,0.27337,0.27637,0.27935,0.2823,0.28524
					,0.28814,0.29103,0.29389,0.29673,0.29955,0.30234,0.30511,0.30785,0.31057,0.31327
					,0.31594,0.31859,0.32121,0.32381,0.32639,0.32894,0.33147,0.33398,0.33646,0.33891
					,0.34134,0.34375,0.34614,0.34849,0.35083,0.35314,0.35543,0.35769,0.35993,0.36214
					,0.36433,0.3665,0.36864,0.37076,0.37286,0.37493,0.37698,0.379,0.381,0.38298
					,0.38493,0.38686,0.38877,0.39065,0.39251,0.39435,0.39617,0.39796,0.39973,0.40147
					,0.4032,0.4049,0.40658,0.40824,0.40988,0.41149,0.41308,0.41466,0.41621,0.41774
					,0.41924,0.42073,0.4222,0.42364,0.42507,0.42647,0.42785,0.42922,0.43056,0.43189
					,0.43319,0.43448,0.43574,0.43699,0.43822,0.43943,0.44062,0.44179,0.44295,0.44408
					,0.4452,0.4463,0.44738,0.44845,0.4495,0.45053,0.45154,0.45254,0.45352,0.45449
					,0.45543,0.45637,0.45728,0.45818,0.45907,0.45994,0.4608,0.46164,0.46246,0.46327
					,0.46407,0.46485,0.46562,0.46638,0.46712,0.46784,0.46856,0.46926,0.46995,0.47062
					,0.47128,0.47193,0.47257,0.4732,0.47381,0.47441,0.475,0.47558,0.47615,0.4767
					,0.47725,0.47778,0.47831,0.47882,0.47932,0.47982,0.4803,0.48077,0.48124,0.48169
					,0.48214,0.48257,0.483,0.48341,0.48382,0.48422,0.48461,0.485,0.48537,0.48574
					,0.4861,0.48645,0.48679,0.48713,0.48745,0.48778,0.48809,0.4884,0.4887,0.48899
					,0.48928,0.48956,0.48983,0.4901,0.49036,0.49061,0.49086,0.49111,0.49134,0.49158
					,0.4918,0.49202,0.49224,0.49245,0.49266,0.49286,0.49305,0.49324,0.49343,0.49361
					,0.49379,0.49396,0.49413,0.4943,0.49446,0.49461,0.49477,0.49492,0.49506,0.4952
					,0.49534,0.49547,0.4956,0.49573,0.49585,0.49598,0.49609,0.49621,0.49632,0.49643
					,0.49653,0.49664,0.49674,0.49683,0.49693,0.49702,0.49711,0.4972,0.49728,0.49736
					,0.49744,0.49752,0.4976,0.49767,0.49774,0.49781,0.49788,0.49795,0.49801,0.49807
					,0.49813,0.49819,0.49825,0.49831,0.49836,0.49841,0.49846,0.49851,0.49856,0.49861
					,0.49865,0.49869,0.49874,0.49878,0.49882,0.49886,0.49889,0.49893,0.49896,0.499
					,0.49903,0.49906,0.4991,0.49913,0.49916,0.49918,0.49921,0.49924,0.49926,0.49929
					,0.49931,0.49934,0.49936,0.49938,0.4994,0.49942,0.49944,0.49946,0.49948,0.4995
					,0.49952,0.49953,0.49955,0.49957,0.49958,0.4996,0.49961,0.49962,0.49964,0.49965
					,0.49966,0.49968,0.49969,0.4997,0.49971,0.49972,0.49973,0.49974,0.49975,0.49976
					,0.49977,0.49978,0.49978,0.49979,0.4998,0.49981,0.49981,0.49982,0.49983,0.49983
					,0.49984,0.49985,0.49985,0.49986,0.49986,0.49987,0.49987,0.49988,0.49988,0.49989
					,0.49989,0.4999,0.4999,0.4999,0.49991,0.49991,0.49992,0.49992,0.49992,0.49992
					,0.49993,0.49993,0.49993,0.49994,0.49994,0.49994,0.49994,0.49995,0.49995,0.49995
					,0.49995,0.49995,0.49996,0.49996,0.49996,0.49996,0.49996,0.49996,0.49997,0.49997
					,0.49997,0.49997,0.49997,0.49997,0.49997,0.49997,0.49998,0.49998,0.49998,0.49998


				};
		boolean extrapola = true;
		boolean noextrapola = false;
		boolean equiespaciada = true;
		
		/**
		 * tablacdf devuelve a partir del x>0 la probabilidad acumulada entre 0 y x
		 * en una normal estandar
		 */
		tablacdf = new Tabla1V(x, probAcumDesde0, extrapola, equiespaciada);
		
		/**
		 * tablaInversaCdf devuelve a partir de una probabilidad >0.5 el valor de x>0
		 * tal que F(x) = 0.5 en una normal estandar
		 * Commo los puntos en el eje de probabilidad no estan equiespaciados su 
		 * uso es mas lento que el de tablacdf
		 */
		equiespaciada = false;
		tablaInversaCdf = new Tabla1V(probAcumDesde0, x, noextrapola, equiespaciada);
		
	}
	

//    // return pdf(x) = standard Gaussian pdf
//    public static double pdf(double x) {
//        return Math.exp(-x*x / 2) / Math.sqrt(2 * Math.PI);
//    }
//
//    // return pdf(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
//    public static double pdf(double x, double mu, double sigma) {
//        return pdf((x - mu) / sigma) / sigma;
//    }
//
//    // return cdf(z) = standard Gaussian cdf using Taylor approximation
//    public static double cdf(double z) {
//        if (z < -8.0) return 0.0;
//        if (z >  8.0) return 1.0;
//        double sum = 0.0, term = z;
//        for (int i = 3; sum + term != sum; i += 2) {
//            sum  = sum + term;
//            term = term * z * z / i;
//        }
//        return 0.5 + sum * pdf(z);
//    }
//
//    // return cdf(z, mu, sigma) = Gaussian cdf with mean mu and stddev sigma
//    public static double cdf(double z, double mu, double sigma) {
//        return cdf((z - mu) / sigma);
//    } 
//
//    // Compute z such that cdf(z) = y via bisection search
//    public static double inversa(double y) {
//        return inversa(y, 0.00000001, -8, 8);
//    } 
//
//    // bisection search
//    private static double inversa(double y, double delta, double lo, double hi) {
//        double mid = lo + (hi - lo) / 2;
//        if (hi - lo < delta) return mid;
//        if (cdf(mid) > y) return inversa(y, delta, lo, mid);
//        else              return inversa(y, delta, mid, hi);
//    }


    /** 
     * Devuelve dist. normal acumulada para el argumento x
     * en una normal estandar
     * 
     * @param x
     * @return
     */
    public static double cdf2(double x){
    	if(x<0) return 0.5 - tablacdf.dameValor(-x);
    	if(x>0) return 0.5 + tablacdf.dameValor(x);
    	return 0.5;
    }
    
    /**
     * Dada una probabilidad acumulada p, devuelve el argumento de x
     * tal que F(x) = p en una normal estandar
     * @param p
     * @return
     */
    public static double inversacdf2(double p){
    	if(p>0.5) return tablaInversaCdf.dameValor(p-0.5);
    	double comp = 0.5 - p;
    	return -tablaInversaCdf.dameValor(comp);	
    }
    

    // test client
//	    public static void main(String[] args) {
//	        double z     = 0.0;
//	        double mu    = 0.0;
//	        double sigma = 1.0;
//	        System.out.println(cdf(z, mu, sigma));
//	        double y = cdf(z);
//	        System.out.println(inversa(y));
//	    }
    
    public static void main(String[] args){
    	System.out.println("Distribución normal acumulada");
    	double x = -5.0;
    	for(int i=0; i<100; i++){
    		double delta = 0.1;
    		double cdf = cdf2(x);
    		System.out.println("x " + x  + " cdf = " + cdf);
    		x += delta;
    	}
    	System.out.println("Distribución normal acumulada inversa");
    	x = 0.0;
    	for(int i=0; i<1001; i++){
    		double delta = 0.001;
    		double cdf = inversacdf2(x);
    		System.out.println("x " + x  + " cdf = " + cdf);
    		x += delta;
    	}
    	
    }





}
