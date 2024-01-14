/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * PruebaVAR is part of MOP.
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

package pruebasClases;

import matrices.Oper;
import procEstocUtils.DistribucionNormal;
import utilitarios.DirectoriosYArchivos;

public class PruebaVAR {
	
	
	
	private static double[][] A1  =
	{{0.633977322487328,	0.09753497866555183,	0.13495198380187806	},
	{0.0652313484345528,	0.6857676696595791,	0.03548241094816311},	
	{0.0476440979489933,	0.013867958795817117,	0.8053694493247168	}};
	
	private static double[][] B =
	{{-0.6375290494088056,	-0.6538058578234035,	-0.4075470665281593	},
	{-0.7132291687217662,	0.30084111736311875,	0.6330867041631568},	
	{-0.29130888081959133,	0.6942856201733647,	-0.6581083600563995	}};
	private static double[][] D  =   
	{{0.6038352602192679,	0.0,	0.0	},
	{0.0,	0.25131874267795706,	0.0},	
	{0.0,	0.0,	0.3200564321694259}};	
	
	public static void main(String[] args){
		
		String archSal = "G:/PLA/Pla_datos/Archivos/ModeloOp/PRUEBAS VAR Y PVA Y BIBLIOGRAFIA/ESTIMACION/Salidas/varAportes/pruebaTonta.txt";
		double[][] DRaiz = new double[3][3];
		DRaiz[0][0] = Math.sqrt(D[0][0]);
		DRaiz[1][1] = Math.sqrt(D[1][1]);
		DRaiz[2][2] = Math.sqrt(D[2][2]);
		double[] y0 = new double[3];
		
		double[] y = new double[3];
		double[] e = new double[3];
		
		y = y0;
		StringBuilder sb = new StringBuilder();
		for(int t=0; t<10000; t++){
			
			e[0] = DistribucionNormal.inversacdf2(Math.random());
			e[1] = DistribucionNormal.inversacdf2(Math.random());
			e[2] = DistribucionNormal.inversacdf2(Math.random());
			
			double[] aux1 = Oper.matPorVector(DRaiz, e);
			double[] aux2 = Oper.matPorVector(B, aux1);
			double[] aux3 = Oper.matPorVector(A1, y);
			
			y = Oper.sumaVectores(aux2, aux3);
			sb.append(e[0] + "  " + e[1] + "  " + e[2] + " " + y[0] + "  " + y[1] + "  " + y[2]);
			sb.append("\n");
		}
		
		if(DirectoriosYArchivos.existeArchivo(archSal)) DirectoriosYArchivos.eliminaArchivo(archSal);
		DirectoriosYArchivos.agregaTexto(archSal, sb.toString());
	}
	


}
