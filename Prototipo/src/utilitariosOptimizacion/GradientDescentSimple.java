/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * GradientDescentSimple is part of MOP.
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

package utilitariosOptimizacion;

public class GradientDescentSimple {
	
	private FuncionEvaluable funcion;  // objeto que evalúa una función
	private int iter; // número de iteración
	private double alfaIni;
	private double alfa;  // alfa(t) = alfaIni*(1/t)**beta
	private double beta; 
	private double[] argsIni;
	private double[] incArgsIni; // los incrementos iniciales
	private double epsilonParada; // si la función baja menos de este valor se detiene la iteración
	private int maxiter; // cantidad máxima de iteraciones
	private double valorOptimo; // valor del óptimo
	private double[] argsOptimo; // valores de las variables en el óptimo
	private int sentido;  // -1 si se está minimizando y 1 si se está maximizando
	
	
	
	
	
	public GradientDescentSimple(FuncionEvaluable funcion, int iter, double alfaIni, double beta,
			double[] argsIni, double[] incArgsIni, double epsilonParada, int maxiter, int sentido) {
		super();
		this.funcion = funcion;
		this.iter = iter;
		this.alfaIni = alfaIni;
		this.beta = beta;
		this.argsIni = argsIni;
		this.incArgsIni = incArgsIni;
		this.epsilonParada = epsilonParada;
		this.maxiter = maxiter;
		this.sentido = sentido;
	}


	/**
	 * Devuelve true si se alcanzó la tolerancia de epsilon parada y false si paró por
	 * cantidad de iteraciones.
	 */
	public boolean optimizar(){
		int cantVars = argsIni.length;
		iter = 1;
		double ultimoF = -sentido*Double.MAX_VALUE;
		double deltaF = Double.MAX_VALUE;   // valor absoluto de la variación de objetivo
		double[] args = argsIni;
		double[] incArgs = incArgsIni;
		while (iter<maxiter && deltaF>epsilonParada){
			double[] grad = gradiente(funcion, args, incArgs);
			alfa = alfaIni/(Math.pow(iter, beta));
			for(int iv = 0; iv<cantVars; iv++){
				args[iv] += sentido*alfa*grad[iv];
			}
			double valorF = funcion.valor(args);
			deltaF = Math.abs(valorF - ultimoF);
			ultimoF = valorF;
			iter++;
		}
		if(deltaF<epsilonParada) return true;
		return false;
	}
	
	
	/**
	 * Calcula el gradiente por los incrementos medios al variar los argumentos los argumentos
	 * @param f
	 * @param args  valores de las variables en el punto en el que se calcula el gradiente
	 * @param incrArgs  incrementos de cada argumento para calcular los incrementos de la función
	 * @return
	 */
	public double[] gradiente(FuncionEvaluable f, double[] args, double[] incrArgs){
		double valorBase = f.valor(args);		
		int cantVars = args.length;
		double[] incMed = new double[cantVars];
		for(int iv=0; iv<cantVars; iv++){
			double[] argsiv = args.clone();
			argsiv[iv] += incrArgs[iv];
			incMed[iv] = (funcion.valor(argsiv) - funcion.valor(args))/incrArgs[iv];
 		}
		return incMed;		
	}
	

}
