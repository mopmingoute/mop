/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ProbadorLPCC5 is part of MOP.
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

package pruebasCicloCombinado;


import java.util.ArrayList;

import utilitarios.DirectoriosYArchivos;

public class ProbadorLPCC5 {
	

	
	public static double pMin1TG = 60; // potencia mínima de un módulo TG en MW
	public static double pMax1TG = 176 ; // potencia máxima de un m�dulo TG en MW, se supone igual para todos los combustibles
	public static double pMin1CC = 90; // potencia minima de un módulo de TG en el CC en MW
	public static double pMax1CC = 265; // para cada combustible potencia máxima de un módulo CC en MW, se supone igual para todos los combustibles
	
	/*
	 * potencia térmica en MW para el mínimo técnico de un módulo 
	 * (es igual al mínimo técnico en MW dividido el rendimiento en el mínimo técnico) de TG y de CC. 
	 * Se supone que es igual para todos los combustibles.
	 */
	public static double potTerMintec1TG = 300;  // para 60 de potencia eléctrica 
	
	public static int cantComb = 2; // cantidad de combustibles, lugar 0 gas natural, 1 gasoil
	
	/*
	 * potencia térmica en MW por cada MW eléctrico generado por encima del mínimo técnico de la TG (adimensionado)
	 * y del CC para cada uno de los combustibles.
	 */
	public static double[] potTerPropTG = {1.558441558, 1.764705882};
	public static double[] potTerPropCC = {1.038961039, 1.176470588};  // lo mismo para el ciclo combinado.
			
	public static int cantHoras = 24;
	public static int cantHorasMax = 48;
	public static int nModTG = 2;
	public static int nModCV = 1;
	
	public static double[] costoComb = {20.0, 50.0}; // costo por MWh térmico de energía del ciclo combinado para cada combustible
	public static double costoAlt = 300.0;  // costo por MWh eléctrico de potencia alternativa
	public static double precioExc = 0.03; // precio de venta de los excedentes
	public static double[] costoArranqueCC = {5000, 5000, 5000};  // costo de arranque no combustible por cada rampa de un TG
	public static double costoArranqueTG = 5000;
	public static double[] energMaxCombCC = {3000.0, 100000};  // energía máxima en MWh de los combustibles
	
	public static double epsilon = 0.01; 
	
	public static int cantArranques = 2;   // cantidad de rampas sucesivas del CC (R en los comentarios)
	
	public static int[] durRampa = {4, 2}; // cantidad de horas de rampa en que no se alzanca potencia máxima (en los comentarios Dr)
	
	// aumento de potencia en cada hora de la rampa del CC con una turbina en ciclo en rampa
	public static double[][] dPR1CC = {{0, 35, 26, 138}, {0, 91}}; 
	
	// potencia en la última hora de la rampa
	public static double[]  PRDr = {199, 91};
	
	// aumento de potencia térmica en cada hora de la rampa del CC de una turbina en ciclo en rampa
	public static double[][] dPTerR = {{0, 175, 29, 238 },{0, 202}, {0, 202}};   
	
	// potencia térmica en cada hora de la rampa del CC de una turbina en ciclo de rampa
	public static double[][] pTerR = {{0, 175, 204, 442 },{0, 202}, {0, 202}}; 
	
	// energía térmica total de cada tipo de rampa en MWh
	public static double[] enTerR = {821, 202};
	
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
	 * Las potencias eléctricas durante las rampas son potRh
	 * Las potencias eléctricas como TG son potTGh
	 * Las potencias eléctricas como CC son potCCh
	 * 
	 * emin1h,.....eminKh potencias térmicas de cada combustible 1 al K destinadas a cubrir en la hora h la 
	 * energía térmica del mínimo técnico, tanto en ciclo abierto como cerrado
	 * 
	 * eprop1h,....epropKh potencias térmicas de cada combustible por encima de la energía térmica
	 * del mínimo técnico
	 * 
	 * etot1h,....etotKh potencias térmicas totales de cada combustible
	 * 
	 * 
	 * xnh, n=1,.,nModTG, h=0,..cantHoras, binarias que valen 1 si la turbina está combinada en régimen normal
	 * ynrh, n=1,.,nModTG, r=1,.cantArranques * cantArranquesPorTipo, h horas;  binarias que valen 1 si se está en rampa de arranque
	 * znh, n=1,...nModTG, h=0,..cantHoras, enteras que valen la cantidad de módulos de TG arrancados 
	 * 
	 * @param args
	 */
	
	public static void main(String[] args) {
		
		String dirSalidas = "G:/PLA/Pla_datos/Archivos/ModeloOp/ModeladoCicloCombinado/EjemploLPSolve/CasoProb5-12mayo/prob5-12mayo-V2.lp";
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
		System.out.println("Costo unitario de arranque en CC  \n" + costoArranqueCC[0] + "\n" +
				costoArranqueCC[1] +  "\n" +costoArranqueCC[2]);
		System.out.println("Costo unitario de arranque en TG\n" + costoArranqueTG);	
		System.out.println("Energía maxima en MWh del combustible primario\n" + energMaxCombCC[0]);
		System.out.println("Energía maxima en MWh del combustible secundario\n" + energMaxCombCC[1]);

		int cuenta = 1; // servirá para llevar la cuenta de TGs disponibles en todos los casos necesarios		
		
		// Creación de la lista de TGs disponibles		
		tgsDisp = new ArrayList<Integer>(); 
		for(int i=0; i< nModTG; i++){
			if(disponibilidad[i]==1) tgsDisp.add(i+1);
		}
		
		
		// Objetivo
		sb.append("//		Objetivo \n");
		
		// Costo de combustible 
		sb.append("min: ");
		for(int k=1; k<=cantComb; k++){
			double coef = costoComb[k-1];
			sb.append(coef + " terTot" + k);			
			sb.append(" + ");	
		}
		
		
		// Costo de arranque del CC 
		for(Integer n: tgsDisp){
			for(int r=1; r<= cantArranques; r++){
				double coef = costoArranqueCC[r-1]; 
				sb.append(coef + " ytot" + n + r);
				sb.append(" + ");	
			}			
		}
		
		// Costo de arranque de TGs  (atotn es la cantidad de arranques de la turbina n)
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

		// HAY UNA POSIBILIDA DE FORZAR EL ESTADO FINAL DEL CICLO SI SE USA UNA VARIABLE DE CONTROL DE
		
		
		//		Una TG no puede estar al mismo tiempo en más de uno de los estados siguientes: en alguna rampa, en régimen del ciclo combinado o en ciclo abierto.
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
		
		
		//		La entrada en régimen de CC de una turbina debe estar precedida por una rampa.
		//		Para todo n, para h=durRampa[0],NH-1
		//		Para r=1,R tal que h>= Dr
		//		 suma en r (y(n,r,h-Dr))  >= x(n,h) - x(n,h-1)
				
		sb.append("//		La entrada en regimen de CC de una turbina debe estar precedida por alguna rampa \n");
		for(Integer n: tgsDisp){		
			double coef;
			for(int h=durRampa[0]; h<cantHoras; h++){
				for(int r = 1; r<=cantArranques; r++){
					int dur = durRampa[r-1];
					sb.append("y" + n + r + (h-dur));	
					sb.append(" + ");
				}
				sb.append("x" + n + (h-1) + " - x" + n + h);
				sb.append(" >= 0");
				sb.append(";");
				sb.append("\n");	
			}
		}		
		
		
		
		// 		Una vez terminada una rampa de arranque, la turbina pasa al funcionamiento del CC
		//		Para todo n, para h=durRampa[0],NH-1
		//		 x(n,h) >= suma en r (- y(n,r,h)  + y(n,r,h-1) )
		sb.append("//		Una vez terminada una rampa de arranque, la turbina pasa al funcionamiento del CC \n");		
		for(Integer n: tgsDisp){		
			for(int h=durRampa[0]; h<cantHoras; h++){
				sb.append("x" + n + h);
				for(int r = 1; r<=cantArranques; r++){	
					sb.append(" - y" + n + r + h + " + y" + n + r + (h-1));
				}
				sb.append(" >= 0");
				sb.append(";");
				sb.append("\n");					
			}
		}
		
	
		//		No hay más de una rampa de cada clase de cada máquina
		//		Para todo n, para todo r
		//		suma en h=0,23 (y (n,r,h) ) <= Dr
		sb.append("//		No hay más de una rampa de cada clase de cada máquina \n");
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
		
		
		//	No puede haber a la vez alguna TG en alguna rampa r y alguna otra TG en régimen CC o en otra rampa r’.
		//	Para todo h, para todo par ordenado (n, m) con m distinto de n, de turbinas a gas:
		//	Suma en r(y(n r h)) + x(m,h) + suma en r’distinto de r (y(m,r’,h) <= 1

		sb.append("//  No puede haber a la vez alguna TG en alguna rampa y alguna otra TG en regimen CC o en otra rampa distinta\n");
		for(int h=0; h<= cantHoras; h++){
			for(Integer n: tgsDisp){
				for(int r=1; r<=cantArranques; r++){
					sb.append("y" + n + r + h);
					sb.append(" + ");				
					for(Integer m: tgsDisp){
						if(n!=m){
							for(int r2=1; r<=cantArranques; r++){
								if(r2!=r){
									sb.append("y" + m + r2 + h);
									sb.append(" + ");
								}
							}
	//						sb.append("x" + m + h + " + ");
	//						sb.append("z" + m + h + " <= 1");  // OJOJOJOJOJO ACA m ESTA OPERANDO EN CICLO ABIERTO 
							sb.append("x" + m + h + " <= 1");  
							sb.append(";");
							sb.append("\n");	
						}
					}
				}
			}
		}
		
		
		//		Una turbina no puede comenzar la rampa r+1 antes de haber terminado la rampa r.
		//		Para todo n, para r=1,R-1, para h=0,NH-1:
		//		Suma en horas j=0,h ( y(n,r,j) ) >= Dr * y(n,r+1,h+1)
		sb.append("//		Una turbina no puede comenzar la rampa r+1 antes de haber terminado su rampa r \n");
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
		
		

		
		// La rampa r dura no más de durRampa horas
		//		Para todo n, para r=1,R
		//				Para h=0,NH-1-Dr
		//				suma en j=0,Dr (y(n,r,h-j) <= Dr
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
		
		// La rampa r no dura menos de durRampa horas
		// Para todo n, para todo r
		// 		Para  h=0,NH-1-Dr
		//			(Dr-1)y(n,r,h)-(Dr-1)y(n,r,h+1)+y(n,r,h+2)...+y(n,r,h+Dr-1)
		sb.append("//		La rampa r dura no menos de durRampa horas \n");

		for(Integer n: tgsDisp){	
			for(int r=1; r<=cantArranques; r++){
				for(int h=0; h<cantHoras-durRampa[r-1]; h++){			
					sb.append((durRampa[r-1]-1) + " y" + n + r + h);
					sb.append(" - " + (durRampa[r-1]-1) + " y" + n + r + (h+1));
					for(int j=1; j<durRampa[r-1]; j++){
						sb.append(" + " + " y" + n + r + (h+1+j));
					}
					sb.append(" >= 0;");
					sb.append("\n");	
				}
			}
		}
		// La primera hora no puede ser de rampa seguida de no rampa
		for(Integer n: tgsDisp){	
			sb.append("y" + n + "10 - y" + n + "11 <= 0;\n");	
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
		//		potnRrh <= suma en j=1,�,Dr (y(n,r,h-j) * dPR1CCrj)           (potencia en rampa r)
		//		potnRrh <= PRDr * y(n,r,h)    
		//		(si no est� en rampa r, la potencia de rampa r es 0, la restricci�n no est� activa en la propia rampa r porque est� dominada por la restricci�n anterior)
		sb.append("// Potencias de rampa r incrementos acumulados\n");
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
		
		//		Potencias de ciclo combinado por encima del mintec con las turbinas en el ciclo en régimen
		//		Para todo h
		//		potCCh = suma en n( x(n,h) * pMin1CC) + suma en k(terCCkh/potTerPropCCk)
		sb.append("//		Potencias de ciclo combinado por encima del mintec con las turbinas en el ciclo en regimen \n");
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
		
		
		//		Potencia por encima del mínimo técnico máxima en ciclo combinado
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
		
		
		//		Potencias de las turbinas en ciclo abierto por encima del mintec
		//		Para todo h
		//		potTGh = suma en n( z(n,h) * pMin1TG) + suma en k(terkTGh/potTerPropTGkh)
		sb.append("//		Potencias de las turbinas en ciclo abierto por encima del mintec \n");
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
		
		
		//		Potencia por encima del mínimo técnico máxima en ciclo abierto
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
		
		//		Energía térmica de los mínimos técnicos
		//		Para todo h
		//		suma en k (terMinkh) = suma en n( z(n,h) + x(n,h) ) * potTerMintec1TG
		sb.append("//		Energia termica de los minimos tecnicos \n");

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
		
		
		
		//		Energía térmica en rampas
		//		Para todo h
		// 		suma en k (terRamk) = suma en n(suma en r (ytot(n,r)))* enerTerRamr 
		sb.append("//		Energía termica en rampas  \n");

		for(int k=1; k<=cantComb; k++){
			sb.append("terRam" + k);
			if(k<cantComb) sb.append(" + ");
		}
		for(Integer n: tgsDisp){				
			for(int r=1; r<= cantArranques; r++){
				sb.append(" - " + enTerR[r-1]);
				sb.append(" ytot" + n + r);
			}								
		}
		sb.append(" = 0");
		sb.append(";");		
		sb.append("\n");		
					
		//		Consumo total de cada combustible
		//		Para todo k , 
		//		terTotk = terRamk + suma en h(terMinkh + terTGkh + terCCkh)
		sb.append("//		Consumo total de cada combustible \n");
		for(int k=1; k<=cantComb; k++){
			sb.append("terTot" + k);
			sb.append(" - terRam" + k);
			for(int h=0; h<cantHoras; h++){				
				sb.append(" - terMin" + k + h);
				sb.append(" - terTG" + k + h);
				sb.append(" - terCC" + k + h );
			}
			sb.append(" = 0;");	
			sb.append("\n");		
		}
		
		
		// Cota superior del consumo de los combustibles
		
		for(int k=1; k<=cantComb; k++){
			sb.append("terTot" + k);
			sb.append(" <= " + energMaxCombCC[k-1] + ";\n");
		}


		// Restricciones de demanda incluso potencia alternativa
		sb.append("//		Restricciones de demanda \n");
		for(int h=0; h<cantHoras; h++){
			sb.append("pot" + h + " + " + "potAlt" + h + " - potExc" + h + " = " + demanda[h]);	
			sb.append(";");
			sb.append("\n");	
		}					
	
		// Cuenta de arranques del CC
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
			for(int h=1; h<cantHoras; h++){    // se supone que no hay arranque en hora 0
					sb.append("a" + n + h);	
					sb.append(" - z" + n + h);	
					sb.append(" + z" + n + (h-1));	
					sb.append(" >= 0;");
					sb.append("\n");	
			}
		}		
		
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
		System.out.println("fin de impresion lp");
		

	}
	

}

