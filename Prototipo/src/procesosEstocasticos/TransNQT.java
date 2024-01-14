/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TransNQT is part of MOP.
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

package procesosEstocasticos;

import java.util.ArrayList;
import java.util.Random;

import procEstocUtils.DistribucionNormal;
import procEstocUtils.Graficador;
import utilitarios.DirectoriosYArchivos;
import utilitarios.Tabla1V;
import utilitarios.UtilArrays;

public class TransNQT extends TransformacionVA{
	
	/**
	 * Los datos originales ordenados son x(1),...,x(N) 
	 * Al dato x(i) se le asocia la probabilidad acumulada FX(i)= i/(N+1), generando
	 * una distribucion acumulada FX cuya inversa es FXINV(y).
	 * El TR(x(i)) transformado de x(i) es y(i):= TR(x(i))=FNINV(FX(i)).
	 * FNINV es la inversa de la función FN de probabilidad acumulada normal estandar.
	 * Los datos transformados y(i) tienen distribucion empirica que se aproxima 
	 * a una normal estandar.
	 * 
	 * La transformacion inversa, dado y normal estandar es de la forma
	 * x = FXINV(FN(y))
	 * 
	 * 
	 *
	 */
	
	private double[] datosO;   // Los datos en orden creciente
	private int cantDatos;
	private double[] probs;  // las probabilidades asociadas a cada dato 
	private Tabla1V fx;   // representa la distribucion acumulada FX(x), lleva del valor de x con distribución original al conjunto 0,1
	private Tabla1V fxInv; // lleva el valor de probabilidad acumulada 0,1 a la distribución de x original
	
	public TransNQT(double[] datos) {
		super();
		datosO = utilitarios.UtilArrays.ordenaDouble(datos);
		this.cantDatos = datos.length;
		double cantDatosd = (double)cantDatos;
		probs = new double[cantDatos];
//		TODO for(int i=0; i<cantDatos; i++){
		for(int i=1; i<=cantDatos; i++){
			double id = (double)i;			
			probs[i-1] = id/(cantDatosd+1);
		}	
		fx = new Tabla1V(datosO, probs, false, false);
		fxInv = new Tabla1V(probs, datosO, true, true);
	}

	@Override
	/**
	 * A partir de una x con una distribución original devuelve una y con distribución normal estandar
	 */
	public double transformar(double x) {
		double p = fx.dameValor(x);
		return DistribucionNormal.inversacdf2(p);
	}

	@Override
	/**
	 * A partir de una y con distribución normal estandar produce una x con la distribución original
	 */
	public double inversa(double y) {
		double p = DistribucionNormal.cdf2(y);
		return fxInv.dameValor(p);
	}

	@Override
	public ArrayList<Double> dameParametros() {
		ArrayList<Double> result = UtilArrays.dameAListDDeArray(datosO);
		return result;
	}

	public int getCantDatos() {
		return cantDatos;
	}

	public void setCantDatos(int cantDatos) {
		this.cantDatos = cantDatos;
	}

	public double[] getProbs() {
		return probs;
	}

	public void setProbs(double[] probs) {
		this.probs = probs;
	}

	public static void main(String[] args){
		int ndatos = 100;
		double ndatosd = (double)ndatos;
		double[] datos = new double[ndatos];
		int nsim = 1000000;
		double[] simulados = new double[nsim];  // va a contener una densidad uniforme [0,1]
		for(int i=0; i<ndatos; i++){
			double id = (double)i;
			datos[i]=id/ndatosd;
		}
		TransNQT tr = new TransNQT(datos);
        Random n1 = new Random(); 
        double valor, valorTr;
		for(int i=0; i<nsim; i++){
			valorTr = n1.nextGaussian();
			valor = tr.inversa(valorTr);
			simulados[i]=valor;
		}
		Graficador gr = new Graficador(simulados);
		String txt = gr.textoHistograma("Prueba uniforme", gr.histogramaStd(0.0, 1.0, 102));
    	String dirArch = "G:/PLA/Pla_datos/Archivos/ModeloOp/PRUEBAS VAR Y PVA Y BIBLIOGRAFIA/PRUEBAS/pruebaNQT.xlt";
    	if(DirectoriosYArchivos.existeArchivo(dirArch)) DirectoriosYArchivos.eliminaArchivo(dirArch);
    	DirectoriosYArchivos.agregaTexto(dirArch, txt);
    	System.out.println("Termina prueba");
				
	}


	
	
}
