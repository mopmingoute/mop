/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProbadorLPCC4 is part of MOP.
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

import java.util.ArrayList;

import utilitarios.DirectoriosYArchivos;

public class ProbadorLPCC4 {
	

	
	public static double pMin1TG = 60; // potencia mónima de un módulo TG en MW
	public static double pMax1TG = 176 ; // potencia móxima de un módulo TG en MW, se supone igual para todos los combustibles
	public static double pMin1CC = 90; // potencia minima de un módulo de TG en el CC en MW
	public static double pMax1CC = 265; // para cada combustible potencia móxima de un módulo CC en MW, se supone igual para todos los combustibles
	
	/*
	 * potencia tórmica en MW para el mónimo tócnico de un módulo 
	 * (es igual al mónimo tócnico en MW dividido el rendimiento en el mónimo tócnico) de TG y de CC. 
	 * Se supone que es igual para todos los combustibles.
	 */
	public static double potTerMintec1TG = 300;  // para 60 de potencia elóctrica 
	
	public static int cantComb = 2; // cantidad de combustibles, lugar 0 gas natural, 1 gasoil
	
	/*
	 * potencia tórmica en MW por cada MW elóctrico generado por encima del mónimo tócnico de la TG (adimensionado)
	 * y del CC para cada uno de los combustibles.
	 */
	public static double[] potTerPropTG = {1.558441558, 1.764705882};
	public static double[] potTerPropCC = {1.038961039, 1.176470588};  // lo mismo para el ciclo combinado.
			
	public static int cantHoras = 24;
	public static int cantHorasMax = 48;
	public static int nModTG = 2;
	public static int nModCV = 1;
	
	public static double[] costoTerCC = {20.0, 50.0}; // costo por MWh tórmico de energóa del ciclo combinado para cada combustible
	public static double costoAlt = 300.0;  // costo por MWh elóctrico de potencia alternativa
	public static double precioExc = 0.03; // precio de venta de los excedentes
	public static double[] costoArranqueCC = {5000, 5000, 5000};  // costo de arranque no combustible por cada rampa de un TG
	public static double costoArranqueTG = 5000;
	public static double energMaxCombCC = 3000.0;  // energóa móxima en MWh del combustible primario
	
	public static double epsilon = 0.01; 
	
	public static int cantArranques = 3;   // cantidad de tipos de arranques
	
	public static int[] durRampa = {4, 2, 2}; // cantidad de horas de rampa en que no se alzanca potencia móxima
	
	// aumento de potencia en cada hora de la rampa del CC con una turbina en ciclo en rampa
	public static double[][] dPR1CC = {{0, 35, 26, 138}, {0, 91}, {0, 91} }; 
	
	public static double[]  PRDr = {199, 91, 91};
	
	// aumento de potencia tórmica en cada hora de la rampa del CC con una turbina en ciclo en rampa
	public static double[][] dPTerR = {{0, 175, 29, 238 },{0, 202}, {0, 202}};   //
	
	public static int[] xinicial = {0, 0};  // estado inicial de CC de cada turbina puede ser 0 o 1
	
	public static ArrayList<Integer> tgsDisp; 
	public static int[] disponibilidad = {1, 1};
	public static int cantTGDisp;
	
	public static int iniManiana = 6;
	public static int iniMediodia = 11;
	public static int iniTarde = 14;
	public static int iniNoche = 19;
	public static int iniValle = 23;
	public static int demValle = 500;
	public static int demManiana = 800;
	public static int demMediodia = 100;
	public static int demTarde = 100;
	public static int demNoche = 1400;
	
	
	public static double[] demanda;
	
	/**
	 * Las potencias elóctricas durante las rampas son potRh
	 * Las potencias elóctricas como TG son potTGh
	 * Las potencias elóctricas como CC son potCCh
	 * 
	 * emin1h,.....eminKh potencias tórmicas de cada combustible 1 al K destinadas a cubrir en la hora h la 
	 * energóa tórmica del mónimo tócnico, tanto en ciclo abierto como cerrado
	 * 
	 * eprop1h,....epropKh potencias tórmicas de cada combustible por encima de la energóa tórmica
	 * del mónimo tócnico
	 * 
	 * etot1h,....etotKh potencias tórmicas totales de cada combustible
	 * 
	 * 
	 * xh h=0,..cantHoras, binarias que valen 1 si el CC estó en rógimen normal
	 * ynh, n=1,..cantArranques * cantArranquesPorTipo, binarias que valen 1 si se estó en rampa de arranque
	 * zTGh h=0,..cantHoras, enteras que valen la cantidad de módulos de TG arrancados 
	 * 
	 * @param args
	 */
	
	public static void main(String[] args) {
		
		String dirSalidas = "G:/PLA/Pla_datos/Archivos/ModeloOp/ModeladoCicloCombinado/EjemploLPSolve/prob4.lp";
		if(DirectoriosYArchivos.existeArchivo(dirSalidas)) DirectoriosYArchivos.eliminaArchivo(dirSalidas);
		
		StringBuilder sb = new StringBuilder();
		
		// Carga demanda
		double[] demanda = new double[cantHorasMax];
		
		
		for(int h=0; h<iniManiana; h++){
			demanda[h] = demValle;			
		}
		
		for(int h=iniManiana; h<iniMediodia; h++){
			demanda[h] = demManiana;
		}
		
		for(int h=iniMediodia; h<iniTarde; h++){
//			demanda[h] = demMediodia;
			demanda[h] = 0.0;
		}

		for(int h=iniTarde; h<iniNoche; h++){
			demanda[h] = demTarde;
		}

		for(int h=iniNoche; h<iniValle; h++){
			demanda[h] = demNoche;
		}		
		
		for(int h=iniValle; h<24; h++){
			demanda[h] = demValle;
		}		
		
		
		for(int h=24; h<iniManiana+24; h++){
			demanda[h] = demValle;			
		}
		
		for(int h=iniManiana+24; h<iniMediodia+24; h++){
			demanda[h] = demManiana;
		}
		
		for(int h=iniMediodia+24; h<iniTarde+24; h++){
			demanda[h] = demMediodia;
		}

		for(int h=iniTarde+24; h<iniNoche+24; h++){
			demanda[h] = 0;
		}

		for(int h=iniNoche+24; h<cantHorasMax; h++){
			demanda[h] = 500;
		}	
		
		for(int h=0; h<cantHoras; h++){
			System.out.println(demanda[h]);
		}

		int cuenta = 1; // serviró para llevar la cuenta de TGs disponibles en todos los casos necesarios		
		
		// Creación de la lista de TGs disponibles		
		tgsDisp = new ArrayList<Integer>(); 
		for(int i=0; i< nModTG; i++){
			if(disponibilidad[i]==1) tgsDisp.add(i+1);
		}
		
		
		// Objetivo
		sb.append("//		Objetivo \n");
		
		// Costo del CC excepto arranque
		sb.append("min: ");
		for(int h=0; h<cantHoras; h++){
			for(int k=1; k<=cantComb; k++){
				double coef = costoTerCC[k-1];
				sb.append(coef + " terTot" + k + h );			
				sb.append(" + ");	
			}
		}			
		
		// Costo de arranque del CC 
		for(Integer n: tgsDisp){
			for(int r=1; r<= cantArranques; r++){
				double coef = costoArranqueCC[r-1]; 
				sb.append(coef + " ytot" + n + r);
				sb.append(" + ");	
			}			
		}
		
		// Costo de arranque de TGs 
		for(Integer n: tgsDisp){
			double coef = costoArranqueTG; 
			sb.append(coef + " atot" + n + " + ");
		}		
		
		// Costo de alternativa
		for(int h=0; h<cantHoras; h++){
			double coef = costoAlt;
			sb.append(coef + " potAlt" + h );			
			if(h<cantHoras-1) sb.append(" + ");	
		}			
		
		// Beneficio por venta de excedentes
		for(int h=0; h<cantHoras; h++){
			double coef = precioExc;
			sb.append(" - " + coef + " potExc" + h );			
		}			
			
		sb.append(";");	
		sb.append("\n");
		
		
	
		
		cantTGDisp = tgsDisp.size();
		
		// Turbinas en ciclo iniciales y cota por funcionamiento el ciclo de vapor

		sb.append("//		Turbinas en ciclo iniciales y cota por funcionamiento del ciclo de vapor\n");
		for(int h=0; h<cantHoras; h++){
			for(Integer n: tgsDisp){
				if(xinicial[n-1]==0 && h<durRampa[0] || nModCV==0){
					sb.append("x" + n + h + " <= 0");
					sb.append(";\n");
				}else{
					sb.append("x" + n + h + " <= 1");
					sb.append(";\n");
				}
			}
		}


		
		
		//		Una TG no puede estar al mismo tiempo en mós de uno de los estados siguientes: en alguna rampa, en rógimen del ciclo combinado o en ciclo abierto.
		//		Para cada TG n, para h=0, NH-1:
		//		Suma en r(y(n,r,h)) + x(n,h) + z(n,h) <= 1
	
		sb.append("//		Una TG no puede estar al mismo tiempo en mas de uno de los estados \n");
		for(Integer n: tgsDisp){
			for(int h=0; h<cantHoras; h++){				
				for(int r=1; r<=cantArranques; r++){
					sb.append("y"+ n + r + h + " + ");
				}
				sb.append("x"+ n + h + " + ");
				sb.append("z"+ n + h);
				sb.append(" <= 1 ;");
				sb.append("\n");
			}
		}
		
		
		//		La entrada en rógimen de CC de una turbina debe estar precedida por una rampa.
		//		Para todo n, para h=durRampa[0],ó,NH-1
		//		Para r=1,óR tal que h>= Dr
		//		[ suma en r (suma en j=Dr,ó,1 (y(n,r,h-j))) ] / Dr >= x(n,h) - x(n,h-1)
		
		sb.append("//		La entrada en regimen de CC de una turbina debe estar precedida por alguna rampa \n");
		for(Integer n: tgsDisp){		
			double coef;
			for(int h=durRampa[0]; h<cantHoras; h++){
				boolean haycoef = false;
				for(int r = 1; r<=cantArranques; r++){
					int dur = durRampa[r-1];
					if(h>=dur){
						haycoef = true;					
						for(int j=h-dur; j<=h-1; j++){					
							coef = (double)1/dur;
							sb.append(coef + " ");
							sb.append("y"+ n + r + j);
							sb.append(" + ");					
						}
					}
				}
				if(haycoef==true){
					sb.append(" x" + n + (h-1) + " - x" + n + h);
					sb.append(" >= 0");
					sb.append(";");
					sb.append("\n");	
				}
			}
		}		
		
		
		//		No hay mós de una rampa de cada clase de cada móquina
		//		Para todo n, para todo r
		//		suma en h=0,ó23 (y (n,r,h) <= Dr
		sb.append("//		No hay mós de una rampa de cada clase de cada móquina \n");
		for(Integer n: tgsDisp){
			for(int r = 1; r<=cantArranques; r++){
				for(int h=0; h< cantHoras; h++){
					sb.append("y" + n + r + h);
					if(h<cantHoras-1) sb.append(" + ");
				}
				sb.append(" <= " + durRampa[r-1]);
				sb.append(";");
				sb.append("\n");	
			}
		}
		
		
		
		
		
		// No puede haber a la vez alguna TG en alguna rampa y alguna TG en rógimen CC
		sb.append("//		No puede haber a la vez alguna TG en alguna rampa y alguna TG en regimen CC \n");
		for(int h=0; h<= cantHoras; h++){
			for(Integer n: tgsDisp){
				for(Integer m: tgsDisp){
					if(n!=m){
						for(int r=1; r<=cantArranques; r++){
							sb.append("y" + n + r + h);
							sb.append(" + ");
						}
						sb.append("x" + m + h + " + ");
						sb.append("z" + m + h + " <= 1");
						sb.append(";");
						sb.append("\n");	
					}
				}
			}
		}
		
		
		//		Una turbina no puede comenzar la rampa r+1 antes de haber terminado la rampa r.
		//		Para todo n, para r=1,ó,R-1, para h=0,ó,NH-1:
		//		Suma en horas j=0,ó,h ( y(n,r,j) ) >= Dr * y(n,r+1,h+1)
		sb.append("//		Una turbina no puede comenzar la rampa r+1 antes de haber terminado la rampa r \n");
		for(Integer n: tgsDisp){
			int iniH = 0;
			for(int r=1; r<cantArranques; r++){				
				double dur = durRampa[r-1];				
				for(int h=iniH; h<cantHoras; h++){
					for(int j=0; j<h; j++){						
						double coef = (double)(1/dur);
						sb.append(coef + " y" + n + r + j);
						if(j != h-1) sb.append(" + ");
					}
					sb.append(" - y" + n + (r+1) + h);
					sb.append(" >= 0");
					sb.append(";");
					sb.append("\n");
				}					
			}
		}		
		
		

		
		// La rampa r dura no mós de durRampa horas
		//		Para todo n, para r=1,ó,R
		//				Para h=0,ó,NH-1-Dr
		//				suma en j=0,ó,Dr (y(n,r,h-j) <= Dr
		sb.append("//		La rampa r dura no mas de durRampa horas \n");

		for(Integer n: tgsDisp){	
			for(int r=1; r<=cantArranques; r++){
				int dur = durRampa[r-1];
				for(int h=0; h<cantHoras-dur; h++){				
					for(int j=0; j<=dur; j++){
						sb.append("y"+ + n + r + (h+j));
						if(j<dur) sb.append(" + ");
					}
					sb.append(" <= " + dur);
					sb.append(";");
					sb.append("\n");	
				}
			}
		}
		

		
		

		
		
		//		Potencia total del ciclo en la hora h
		//		Para todo h
		//		suma en n(suma en r(potnRrh)) + potCCh +  potTGh = poth   
		//		(suma de potencias de rampa, ciclo combinado y ciclo abierto de la turbina n)
		sb.append("//		Potencia total del ciclo en la hora h \n");
		for(int h=0; h<cantHoras; h++){
			for(Integer n: tgsDisp){
				for(int r=1; r<= cantArranques; r++){
					sb.append("pot" + n + "R" + r + h);
					sb.append(" + ");
				}				
			}			
			sb.append("potCC" + h + " + potTG" + h + " - pot" + h);
			sb.append(" = 0;");
			sb.append("\n");						
		}
		
		
		//		Potencias de rampa r
		//		Para todo n, para todo h, para todo r
		//
		//		potnRrh <= suma en j=1,ó,Dr (y(n,r,h-j) * dPR1CCrj)           (potencia en rampa r)
		//		potnRrh <= PRDr * y(n,r,h)    
		//		(si no estó en rampa r, la potencia de rampa r es 0, la restricción no estó activa en la propia rampa r porque estó dominada por la restricción anterior)
		sb.append("//		Potencias de rampa r incrementos acumulados\n");
		for(Integer n: tgsDisp){
			for(int r=1; r<= cantArranques; r++){
				for(int h=0; h<cantHoras; h++){
					sb.append("pot" + n + "R" + r + h );					
					for(int j=0; j< durRampa[r-1]; j++){						
						if(h-j>=0){
							sb.append(" - ");
							double coef = dPR1CC[r-1][j];
							sb.append(coef + " y" + n + r + (h-j));
						}
					}	
					sb.append(" <= 0;");
					sb.append("\n");				
				}
					
			}
		}
		sb.append("//		Potencias de rampa r cota hora final de rampa\n");
		for(Integer n: tgsDisp){
			for(int r=1; r<= cantArranques; r++){
				for(int h=0; h<cantHoras; h++){
					sb.append("pot" + n + "R" + r + h + " - ");			
					sb.append(PRDr[r-1] + " y" + n + r + h);
					sb.append(" <= 0;");
					sb.append("\n");		
				}		
			}
		}
		
		//		Potencias de ciclo combinado con las turbinas en el ciclo en rógimen
		//		Para todo h
		//		potCCh = suma en n( x(n,h) * pMin1CC) + suma en k(terCCkh/potTerPropCCk)
		sb.append("//		Potencias de ciclo combinado con las turbinas en el ciclo en rógimen \n");
		for(int h=0; h<cantHoras; h++){
			sb.append("potCC" + h + " - ");
			for(Integer n: tgsDisp){
				sb.append(pMin1CC + " x" + n + h + " - ");				
			}
			for(int k=1; k<=cantComb; k++){
				double coef = (double)(1/potTerPropCC[k-1]);
				sb.append(coef + " terCC" + k + h);
				if(k<cantComb) sb.append(" - ");
			}	
			sb.append(" = 0;");		
			sb.append("\n");		
		}
		
		
		//		Potencia por encima del mónimo tócnico móxima en ciclo combinado
		//		Para todo h
		//		suma en k(terCCkh/potTerPropCC ) <=  suma en n( x(n,h)*( pMax1CC- pMin1CC) )
		sb.append("//		Potencia por encima del minimo tecnico maxima en ciclo combinado \n");
		for(int h=0; h<cantHoras; h++){
			for(int k=1; k<= cantComb; k++){
				double coef = 1/potTerPropCC[k-1];
				sb.append(coef + " terCC" + k + h);
				if(k<cantComb) sb.append(" + ");
			}
			cuenta = 1;
			sb.append(" - ");
			for(Integer n: tgsDisp){
				double coef = pMax1CC- pMin1CC;
				sb.append(coef + " x" + n + h);
				if(cuenta<cantTGDisp) sb.append(" - ");
				cuenta ++;
			}
			sb.append(" <= 0.0;");
			sb.append("\n");						
		}		
		
		
		//		Potencias de las turbinas en ciclo abierto
		//		Para todo h
		//		potTGh = suma en n( z(n,h) * pMin1TG) + suma en k(terkTGh/potTerPropTGkh)
		sb.append("//		Potencias de las turbinas en ciclo abierto \n");
		for(int h=0; h<cantHoras; h++){
			sb.append("potTG" + h + " - ");
			for(Integer n: tgsDisp){
				sb.append(pMin1TG + " z" + n + h + " - ");				
			}
			for(int k=1; k<=cantComb; k++){
				double coef = (double)(1/potTerPropTG[k-1]);
				sb.append(coef + " terTG" + k + h);
				if(k<cantComb) sb.append(" - ");
			}	
			sb.append(" = 0;");		
			sb.append("\n");		
		}
		
		
		//		Potencia por encima del mónimo tócnico móxima en ciclo abierto
		//		Para todo h
		//		suma en k(terTGkh/potTerPropTG ) <=  suma en n( x(n,h)*( pMax1TG- pMin1TG) )
		sb.append("//		Potencia por encima del minimo tecnico maxima en ciclo combinado \n");
		for(int h=0; h<cantHoras; h++){
			for(int k=1; k<= cantComb; k++){
				double coef = 1/potTerPropTG[k-1];
				sb.append(coef + " terTG" + k + h);
				if(k<cantComb) sb.append(" + ");
			}
			cuenta = 1;
			sb.append(" - ");
			for(Integer n: tgsDisp){
				double coef = pMax1TG- pMin1TG;
				sb.append(coef + " z" + n + h);
				if(cuenta<cantTGDisp) sb.append(" - ");
				cuenta ++;
			}
			sb.append(" <= 0.0;");
			sb.append("\n");						
		}		
		
		//		Consumo de combustible de los mónimos tócnicos
		//		Para todo h
		//		suma en k (terMinkh) = suma en n( z(n,h) + x(n,h) ) * potTerMintec1TG
		sb.append("//		Consumo de combustible de los mónimos tócnicos \n");

		for(int h=0; h<cantHoras; h++){
			for(int k=1; k<=cantComb; k++){
				sb.append("terMin" + k + h);
				if(k<cantComb) sb.append(" + ");
			}
			sb.append(" - ");
			cuenta = 1;
			for(Integer n: tgsDisp){				
				sb.append(potTerMintec1TG + " z" + n + h + " - ");
				sb.append(potTerMintec1TG + " x" + n + h);
				if(cuenta < cantTGDisp) sb.append(" - ");
				cuenta ++;
			}
			sb.append(" = 0");
			sb.append(";");		
			sb.append("\n");		
		}
		
		
		
		//		Consumo de combustible en rampas
		//		Para todo h
		//		suma en k (terRamkh) = suma en n(suma en r (suma en j=1,ó,Dr (yn r h-j * dPTerRr j)))
		sb.append("//		Consumo de combustible en rampas \n");
		for(int h=0; h<cantHoras; h++){
			for(int k=1; k<=cantComb; k++){
				sb.append("terRam" + k + h);
				if(k<cantComb) sb.append(" + ");
			}

			for(Integer n: tgsDisp){				
				for(int r=1; r<= cantArranques; r++){
					for(int j=0; j< durRampa[r-1]; j++){
						if(h-j>=0){
							sb.append(" - ");
							double coef = dPTerR[r-1][j];
							sb.append(coef + " y" + n + r + (h-j));							
						}
					}
				}								
			}
			sb.append(" = 0");
			sb.append(";");		
			sb.append("\n");		
		}		
		
		
		
		//		Consumo total de cada combustible
		//		Para todo k , para todo h
		//		terTotkh = terMinkh + terRamkh + terTGkh + terCCkh
		sb.append("//		Consumo total de cada combustible \n");
		for(int k=1; k<=cantComb; k++){
			for(int h=0; h<cantHoras; h++){
				sb.append("terTot" + k + h + " - ");
				sb.append("terMin" + k + h + " - ");
				sb.append("terTG" + k + h + " - ");
				sb.append("terRam" + k + h + " - ");
				sb.append("terCC" + k + h +  " = 0;");	
				sb.append("\n");		
			}
		}
		
		
		// Cota superior del consumo del combustible primario
		
		for(int h=0; h<cantHoras; h++){
			sb.append("terTot" + "1" + h);
			if(h<cantHoras-1) sb.append(" + ");			
		}
		sb.append(" <= " + energMaxCombCC + ";\n");

		// Restricciones de demanda incluso potencia alternativa
		sb.append("//		Restricciones de demanda \n");
		for(int h=0; h<cantHoras; h++){
			sb.append("pot" + h + " + " + "potAlt" + h + " - potExc" + h + " = " + demanda[h]);	
			sb.append(";");
			sb.append("\n");	
		}					
	
		// Cuenta de arranques 
		sb.append("//		Cuenta de arranques CC\n");
		for(Integer n: tgsDisp){
			for(int r=1; r<=cantArranques; r++){				
				for(int h=0; h<cantHoras; h++){
					sb.append("y" + n + r + h);		
					if(h<cantHoras - 1) sb.append(" + ");
				}
				double coef = durRampa[r-1];
				sb.append(" - " + coef + " ytot" + n + r + " = 0;");
				sb.append("\n");	
			}
		}
		
		// Cuenta de arranques TG
		sb.append("//		Cuenta de arranques TG\n");
		for(Integer n: tgsDisp){			
			for(int h=0; h<cantHoras; h++){
					sb.append("a" + n + h);		
					if(h<cantHoras - 1) sb.append(" + ");
			}
			sb.append(" - atot" + n + " = 0;");
			sb.append("\n");	
		}		
		
		// Declara variables binarias x y z para cada TG
		sb.append("//		Declara variables enteras x para cada TG\n");		
		sb.append("int ");		
		cuenta = 1;
		for(Integer n: tgsDisp){
			for(int h=0; h<cantHoras; h++){
				if(nModCV>0) sb.append("x"+ n + h);
				if(h<cantHoras-1 || cuenta < cantTGDisp) sb.append(" , ");	
			}
			cuenta ++;
		}	
		sb.append(";\n");				
		
		sb.append("//		Declara variables binarias z para cada TG\n");
		sb.append("bin ");	
		cuenta = 1;
		for(Integer n: tgsDisp){
			for(int h=0; h<cantHoras; h++){
				sb.append("z"+ n + h);
				if(h<cantHoras-1 || cuenta < cantTGDisp) sb.append(" , ");	
			}
			cuenta ++;
		}	
		sb.append(";\n");
		
		sb.append("//		Declara variables binarias y para cada TG\n");
		
		sb.append("bin ");		
		if(nModCV>0){
			cuenta = 1;
			for(Integer n: tgsDisp){
				for(int h=0; h<cantHoras; h++){
					for(int r=1; r<= cantArranques; r++){
						sb.append("y"+ n + r + h);
						if(h<cantHoras-1 || cuenta < cantTGDisp || r<cantArranques) sb.append(" , ");	
					}				
				}
				cuenta ++;
			}	
			sb.append(";\n");
		}
		
		
		sb.append("//		Declara variables binarias de arranque a para cada TG\n");
		sb.append("bin ");
		cuenta = 1;
		for(Integer n: tgsDisp){
			for(int h=0; h< cantHoras; h++){
				sb.append("a"+ n + h);
				if(cuenta < cantTGDisp || h<cantHoras-1) sb.append(" , ");	
			}	
			cuenta ++;
		}
		sb.append(";\n");		
		
		
		sb.append("//		Declara variables enteras de arranques totales de TG y CC\n");		
		sb.append("int ");
		cuenta = 1;
		for(Integer n: tgsDisp){
			sb.append("atot" + n + " , ");
			for(int r=1; r<= cantArranques; r++){
				sb.append("ytot"+ n + r);
				if(cuenta < cantTGDisp || r<cantArranques) sb.append(" , ");	
			}
			cuenta ++;
		}
		sb.append(";\n");		
		
		String texto = sb.toString();
		
		DirectoriosYArchivos.agregaTexto(dirSalidas, texto);
		System.out.println("fin de impresión lp");
		

	}
	

}
