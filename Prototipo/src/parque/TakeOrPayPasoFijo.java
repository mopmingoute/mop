/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * TakeOrPayPasoFijo is part of MOP.
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

package parque;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import datatypes.DatosTakeOrPayPasoFijo;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosSalidaPaso;
import logica.CorridaHandler;
import pizarron.PizarronRedis;
import procesosEstocasticos.ProcesoEstocastico;
import tiempo.Evolucion;
import utilitarios.Constantes;
import utilitarios.ParReales;
import utilitarios.Recta;
/**
 * Clase que representa el tipo de contrato take or pay fijo
 * @author ut602614
 *
 */
public class TakeOrPayPasoFijo {
	
	private String nombre;
	/**
	 * Los peróodos del contrato pueden haber comenzado mucho tiempo antes
	 * del instante en que se invocan los mótodos, o comenzar despuós de ese instante. 
	 */
	private int cantPeriodos; // cantidad de peróodos del contrato, los peróodos se comienzan a numerar en 0
	private ArrayList<Double> caudalTopPeriodo;  // caudal top aplicable en el paso, expresado en unidades por hora
	private ArrayList<Double> caudalMaxPasoPeriodo; // caudal medio móximo en el paso en cada peróodo 
	private ArrayList<Integer> instanteInicioPeriodo;  // inicio en segundos del peróodo respecto al inicio de la corrida
	private ArrayList<Integer> instanteFinPeriodo;   // ódem fin del peróodo
	// Los peróodos son cerrados por derecha (-----|
	// Despuós del instante final del óltimo peróodo sólo se puede usar gas prepago con el caudalMaxPasoPeriodo y gratis
	// Antes del instante inicial del primer peróodo no se puede usar gas
	
	private int cantPeriodosMU;  // cantidad de peróodos de make up
	private Evolucion<Double> costoFijoUnitTop;  // costo fijo del top expresado en USD/(unidad.hora)
	/**
	 * Los tres costos siguientes EXCLUYEN EL COSTO DE OPORTUNIDAD DEL BIEN QUE SALE DEL VALOR DE BELLMAN
	 */
	private Evolucion<Double> costoVarHastaTop;  // costo variable USD/unid por consumir hasta el top en el paso, usualmente cero
	private Evolucion<Double> costoVarSobreTopNoPrepago; // costo variable USD/unid por consumir por encima del top en el paso si el bien consumido no es prepago
	private Evolucion<Double> costoVarPrepago; // costo variable USD/unid por consumir prepago 
	
//	/**
//	 * Volómenes prepagados del bien en peróodos anteriores, hay cantPeriodosMU+1 valores,
//	 * uno para el paso corriente y cantPeriodosMU para peróodos anteriores.
//	 * En cualquier instante t del peróodo p, en get(i) estó el volumen pagado y no consumido
//	 * en el peróodo p-i. Asó por ejemplo get(0) tiene el volumen que se lleva prepagado
//	 * en el peróodo corriente p, get(1) tiene el volumen prepago en p-1, etc.
//	 */
//	private double[] volumenesPrepagos;
	/**
	 * Los valores al inicio de la corrida de los peróodos.
	 * Si el instante inicial de la corrida pertenece al peróodo 2 y cantPeriodosMU
	 * es igual a 4, volumenesPrepagosIniciales tiene valores (prepago en lo que va del peróodo2, prepago peróodo1, 0, 0)
	 */
	private double[] volumenesPrepagosIniciales; 
	
	private Double carryForward;   // NO SE USA POR AHORA 
	
	/**
	 * Indices de postes que pertenecen a distintos subconjuntos, postes numerados de 0 en adelante
	 * primer óndice recorre los subconjuntos
	 * segundo óndice recorre los postes del subconjunto
	 */
	
	private int cantSubconjuntos; 
	private ArrayList<Evolucion<Double>> caudalMaxSub; // caudal medio móximo en el subconjunto de postes, primer óndice recorre los subconjuntos
	private Hashtable<Integer, Integer> subconjuntoDelPoste; // dado el poste devuelve el subconjunto asociado
	
	private int periodoUltimaLlamada = 0;  // periodo de la ultima llamada al mótodo periodoDeInstante
	private int sentido;  // sentido en el que busca en periodoDeInstante; en optimización sentido = -1, en simulación = 1.
	
	
	/**
	 * OBSERVACIóN GENERAL:
	 * Para tener en cuenta que el bien tiene costos variables distintos por tramos,
	 * en el ComportamientoDespacho hay que usar rectas para el costo, suponiendo que el costo total es convexo
	 * La clase proporciona los coeficientes de esas rectas en USD/unidad
	 */
	
	
	
	public TakeOrPayPasoFijo(DatosTakeOrPayPasoFijo dtop){
		
		// TODO: ACA VA LA CONSTRUCCIóN DE TODO
		
	
		// Se verifica que los peróodos tienen todos la misma duración menos el primero
		
	
			
	}
	
	
	
	/**
	 * Devuelve las rectas de costo variable r1, r2, ....que cumplen que
	 * costo variable total =: CVT >= ri, para i=1,2,....
	 * Las rectas (a,b) son de la forma a*x + b, donde x es el caudal medio de gas empleado en el peróodo
	 * En el objetivo se incluye la variable CVT
	 * 
	 * ATENCIóN: OBSóRVESE QUE EL TOP DE UN PASO TIENE EL COSTO DE OPORTUNIDAD
	 * DEL PREPAGO DEL PERóODO CORRIENTE
	 * 
	 * ATENCIóN: LA RESTRICCIóN DE CAUDAL MóXIMO VA APARTE, POR EL MóTODO devuelveCaudalMax
	 * 
	 * REQUIERE LA CONVEXIDAD DE LA FUNCIóN DE COSTOS Y NO EMPLEA VARIABLES ENTERAS
	 * SE USA JUNTO AL MóTODO devuelveCaudalMax
	 * Si la superficie por encima de las rectas no es convexa detiene la ejecución
	 * @param instante instante final del paso
	 * @param durpaso duración del paso en segundos
	 * @param volPrepago volumenes prepagos de peróodo corriente, t-1, t-2, etc. en unidades del bien
	 * @param valorPrepago valores o costos de oportunidad en USD/unid de los prepagos corriente, t-1, etc.
	 * @return
	 */
	public ArrayList<Recta> devuelveRectasCostoVarPasoConvex(long instante, int durpaso, double[] volPrepago, double[] valorPrepago){
		ArrayList<Recta> result = new ArrayList<Recta>();
		int per = cargaPeriodoDeInstante(instante);
		double cVPrepago = costoVarPrepago.getValor(instante);
		double durHoras = durpaso/Constantes.SEGUNDOSXHORA;
		if(per<0){
			Recta r = new Recta(0,0); // esta recta es irrelevante porque el caudal móximo seró 0
			result.add(r);
		}else if(per==cantPeriodos){
			// Ya terminó el top y sólo puede usarse bien prepagado
			double y0 = 0; // corte de la recta con el eje vertical
			for(int im=cantPeriodosMU; im>=0; im--){
				if(volPrepago[im]>0){
					Recta r = new Recta(valorPrepago[im]+cVPrepago, y0);
					y0 += volPrepago[im]*(valorPrepago[im]+cVPrepago);
					result.add(r);
				}
			}
		}else{
			// El peróodo corresponde a la vigencia del top
			double caudalTop = caudalTopPeriodo.get(per);
			double cVHastaTop = costoVarHastaTop.getValor(instante);
			double cVSobreTopNoPre = costoVarSobreTopNoPrepago.getValor(instante);
			Recta r = new Recta(cVHastaTop+valorPrepago[0], 0);
			result.add(r);
			double y0 = (cVHastaTop+valorPrepago[0])*caudalTop*durHoras; // corte de la recta con el eje vertical
			for(int im=cantPeriodosMU; im>=0; im--){
				if(volPrepago[im]>0){
					r = new Recta(valorPrepago[im]+cVPrepago, y0);
					y0 += volPrepago[im]*(valorPrepago[im]+cVPrepago);
					result.add(r);
				}
			}
			r = new Recta(cVSobreTopNoPre, y0);	
			result.add(r);
		}	
		// Se chequea convexidad de las rectas, alcanza ver que las pendientes son crecientes
		double pendAnt = -Double.MAX_VALUE;
		for(Recta r: result){
			if(r.getA()<pendAnt){
				System.out.println("No convexidad en take or pay paso fijo " + nombre + " en instante " + instante);
				if (CorridaHandler.getInstance().isParalelo()){
					//PizarronRedis pp = new PizarronRedis();
					//pp.matarServidores();
				}
				System.exit(1);
			}
			pendAnt = r.getA();
		}
		return result;
	}
	
	
	/**
	 * Devuelve el caudal móximo promedio de gas en el peróodo que puede usarse
	 * 
	 * @param instante instante final del paso
	 * @param volPrepagoDisp total de volumen de bien prepago sumando todos los peróodos de make up
	 */
	public double devuelveCaudalMax(long instante, int durpaso, double volPrepagoDisp){
		int per = cargaPeriodoDeInstante(instante);
		double caudalMax = caudalMaxPasoPeriodo.get(per);
		double durHoras = durpaso/Constantes.SEGUNDOSXHORA;
		double caudalPrepago = volPrepagoDisp/durHoras;
		if(per<0){
			return 0.0;
		}else if(per==cantPeriodos){
			if(volPrepagoDisp==0){
				return 0.0;
			}else{
				return Math.min(caudalPrepago, caudalMax);
			}
		}else{
			// el peróodo corresponde a la vigencia del top		
			return caudalMax;
		}
	}

	
	/**
	 * Devuelve los pares (qMaxn - caudal maximo del tramo [unidad/hora], cvn - costo variable del tramo [USD/unidad]) 
	 * para cada uno de los n=1,2...,N tramos de la función de costos variables de uso del bien.
	 * NO REQUIERE LA CONVEXIDAD DE LA FUNCIóN DE COSTOS PERO SI EL EMPLEO DE VARIABLES BINARIAS
	 * 
	 * ATENCIóN: LA RESTRICCIóN DE CAUDAL MóXIMO VA APARTE, POR EL MóTODO devuelveCaudalMax
	 * Las variables binarias x1, x2,...xN valen 0 si el tramo no se emplea en absoluto 
	 * y 1 si se emplea en algón grado
	 * Si los caudales en unid/hora son q1, q2,...qN de cada tramo
	 * las restricciones que se deberón incluir en el problema lineal son
	 * qtotal = q1 + q2 + ... qN
	 * x1>=x2>=x3>=.....>=xN
	 * qn<=xn*qMaxn
	 * 
	 * El costo a incluir en el objetivo es 
	 * suma en n (qn*cvn)
	 * 
	 * @param instante instante final del paso
	 * @param durpaso duración en segundos
	 * @param volPrepagoDisp prepago total de todos los peróodos incluso el corriente en unidades
	 * 
	 */
	public ArrayList<ParReales>devuelveParesQmaxCvarNoConvex(int instante, int durpaso, double[] volPrepago, double[] valorPrepago){
		ArrayList<ParReales> result = new ArrayList<ParReales>();
		int per = cargaPeriodoDeInstante(instante);
		double durHoras = durpaso/Constantes.SEGUNDOSXHORA;
		double cVHastaTop = costoVarHastaTop.getValor(instante);
		double cVPrepago = costoVarPrepago.getValor(instante);
		double cVSobreTopNoPre = costoVarSobreTopNoPrepago.getValor(instante);		
		if(per<0){
			ParReales r = new ParReales(0, Double.MAX_VALUE);
			result.add(r);
		}else if(per==cantPeriodos){
			for(int im=cantPeriodosMU; im>=0; im--){
				if(volPrepago[im]>0){
					ParReales r = new ParReales(volPrepago[im]/durHoras, valorPrepago[im]+cVPrepago);
					result.add(r);
				}
			}
		}else{
			// El peróodo corresponde a la vigencia del top
			double caudalTop = caudalTopPeriodo.get(per);
			cVHastaTop = costoVarHastaTop.getValor(instante);
			cVSobreTopNoPre = costoVarSobreTopNoPrepago.getValor(instante);
			ParReales r = new ParReales(caudalTop, cVHastaTop+valorPrepago[0]);
			result.add(r);
			for(int im=cantPeriodosMU; im>=0; im--){
				if(volPrepago[im]>0){
					r = new ParReales(volPrepago[im]/durHoras, valorPrepago[im]+cVPrepago);
					result.add(r);
				}
			}	
			result.add(r);							
		}		
		return result;		
	}
	
	
	/**
	 * Devuelve el caudal móximo del paso en unidades por hora
	 * ATENCIóN QUE SI EL CONTRATO TERMINó SE TOMA PARA
	 * EL GAS PREPAGO EL CAUDAL DEL óLTIMO PERóODO
	 * @param instante
	 * @return
	 */
	public double caudalMaxPaso(int instante){
		int p = cargaPeriodoDeInstante(instante);
		if(p<0) return 0;
		if(p==cantPeriodos) return caudalMaxPasoPeriodo.get(cantPeriodos-1);	
		// se estó en alguno de los peróodos
		return caudalMaxPasoPeriodo.get(p);		
	}
	
	
	/**
	 * Devuelve el caudal móximo en el poste ip en unidades por hora
	 * @param instante
	 * @return 
	 */
	public double caudalMaxPoste(int instante, int ip){
		cargaPeriodoDeInstante(instante);
		int sub = subconjuntoDelPoste.get(ip);
		Evolucion<Double> ev = caudalMaxSub.get(sub);
		return ev.getValor(instante);
	}
	
	
	/**
	 * Devuelve el ordinal empezando de cero del peróodo correspondiente al instante
	 * entre 0 y cantPeriodos-1, o cantPeriodos si ya terminó el óltimo peróodo.
	 * El peróodo queda cargado en periodoUltimaLlamada
	 * 
	 * @param instante
	 * @return -1 si no empezó el peróodo 0
	 * @return cantPeriodos si terminó el óltimo peróodo
	 * @return el ordinal empezando en 0 del peróodo
	 */
	public int cargaPeriodoDeInstante(long instante){
		if(instante>instanteFinPeriodo.get(cantPeriodos-1)) return cantPeriodos;
		if(instante<=instanteInicioPeriodo.get(0)) return -1;
		int p=periodoUltimaLlamada;		
		while(instante<=instanteInicioPeriodo.get(p)  ||
				instante>instanteFinPeriodo.get(p) ){
			p = p + sentido;	
		}
		periodoUltimaLlamada = p;
		return p;		
	}
	
	
	/**
	 * Calcula los volómenes prepagos como resultado del despacho en un paso de tiempo
	 * y que valdrón en el paso siguiente al corriente.
	 * @param instIniPaso instante inicial del paso corriente de simulación u optimización.
	 * @param instFinPaso instante final del paso corriente
	 * @volumenUsado volumen usado en el paso corriente en unidades del bien
	 * SE CONVIENE POR SIMPLIFICACIóN QUE UN PASO PERTENECE AL PERóODO
	 * DEL INSTANTE FINAL DEL PASO
	 * instFinPaso instante final del paso
	 * durPaso duración en segundos
	 * double volumenUsado en el paso en unidades del bien
	 */
	public double[] calculaNuevosVolumenesPrepagos(long instFinPaso, int durpaso, double volumenUsado, double[] volPrepagosAnteriores){
		int per = cargaPeriodoDeInstante(instFinPaso);
		double[] nuevosPrepagos = new double[cantPeriodosMU+1];
		double durHoras = durpaso/Constantes.SEGUNDOSXHORA;
		double caudalTop = caudalTopPeriodo.get(per); 
		if(volumenUsado<caudalTop*durHoras){
			volPrepagosAnteriores[per] = volPrepagosAnteriores[per] + caudalTop*durHoras-volumenUsado;
		}
		if(instFinPaso == instanteFinPeriodo.get(per)){
			// con el paso corriente termina un peróodo y se actualizan los volómenes
			nuevosPrepagos[0] = 0.0;
			for(int iper=0; iper<=cantPeriodosMU; iper++){
				nuevosPrepagos[1+iper] = volPrepagosAnteriores[iper];				
			}
		}
		return nuevosPrepagos;
	}
	
	

	
	
	/**
	 * Calcula los costos directos del paso, no incluye el costo de oportunidad
	 * si incluye el costo del top.
	 * de los recursos
	 * @param instanteFinal
	 * @para caudalMedioPaso en unidades por hora
	 * @param volPrepago en unidades
	 * @para durpaso en segundos
	 * @return
	 */
	public double calculaCostoPaso(long instanteFinal, double caudalMedioPaso, double volPrepago, int durpaso){
		double costo = 0.0;
		int per = cargaPeriodoDeInstante(instanteFinal);
		double durHoras = durpaso/Constantes.SEGUNDOSXHORA;
		double caudalTop = caudalTopPeriodo.get(per);
		double cVHastaTop = costoVarHastaTop.getValor(instanteFinal);
		double cVPrepago = costoVarPrepago.getValor(instanteFinal);
		double cVSobreTopNoPre = costoVarSobreTopNoPrepago.getValor(instanteFinal);	
		double caudalPrepago = volPrepago/durHoras;
		if(per>=0 && per<cantPeriodos){
			costo += caudalTop*costoVarHastaTop.getValor(instanteFinal)*durHoras;
			costo += cVHastaTop*Math.min(caudalMedioPaso, caudalTop)*durHoras;
			costo += cVPrepago*Math.max(0, Math.min(caudalPrepago, caudalMedioPaso-caudalTop))*durHoras;
			return costo;
		}
		if(per==0) return 0.0;
		if(per==cantPeriodosMU) return caudalMedioPaso*cVSobreTopNoPre*durHoras;
		// nunca se pasa por acó
		return 0.0;
	}

	public ArrayList<Double> getCaudalTopPeriodo() {
		return caudalTopPeriodo;
	}

	public void setCaudalTopPeriodo(ArrayList<Double> caudalTopPeriodo) {
		this.caudalTopPeriodo = caudalTopPeriodo;
	}

	public ArrayList<Double> getCaudalMaxPasoPeriodo() {
		return caudalMaxPasoPeriodo;
	}

	public void setCaudalMaxPasoPeriodo(ArrayList<Double> caudalMaxPasoPeriodo) {
		this.caudalMaxPasoPeriodo = caudalMaxPasoPeriodo;
	}

	public ArrayList<Integer> getInstanteInicioPeriodo() {
		return instanteInicioPeriodo;
	}

	public void setInstanteInicioPeriodo(ArrayList<Integer> instanteInicioPeriodo) {
		this.instanteInicioPeriodo = instanteInicioPeriodo;
	}

	public ArrayList<Integer> getInstanteFinPeriodo() {
		return instanteFinPeriodo;
	}

	public void setInstanteFinPeriodo(ArrayList<Integer> instanteFinPeriodo) {
		this.instanteFinPeriodo = instanteFinPeriodo;
	}

	public int getCantPeriodosMU() {
		return cantPeriodosMU;
	}

	public void setCantPeriodosMU(int cantPeriodosMU) {
		this.cantPeriodosMU = cantPeriodosMU;
	}

	public Evolucion<Double> getCostoFijoUnitTop() {
		return costoFijoUnitTop;
	}

	public void setCostoFijoUnitTop(Evolucion<Double> costoFijoUnitTop) {
		this.costoFijoUnitTop = costoFijoUnitTop;
	}

	public Evolucion<Double> getCostoVarHastaTop() {
		return costoVarHastaTop;
	}

	public void setCostoVarHastaTop(Evolucion<Double> costoVarHastaTop) {
		this.costoVarHastaTop = costoVarHastaTop;
	}

	public Evolucion<Double> getCostoVarSobreTopNoPrepago() {
		return costoVarSobreTopNoPrepago;
	}

	public void setCostoVarSobreTopNoPrepago(Evolucion<Double> costoVarSobreTopNoPrepago) {
		this.costoVarSobreTopNoPrepago = costoVarSobreTopNoPrepago;
	}

	public Double getCarryForward() {
		return carryForward;
	}

	public void setCarryForward(Double carryForward) {
		this.carryForward = carryForward;
	}


	public ArrayList<Evolucion<Double>> getCaudalMaxSub() {
		return caudalMaxSub;
	}

	public void setCaudalMaxSub(ArrayList<Evolucion<Double>> caudalMaxSub) {
		this.caudalMaxSub = caudalMaxSub;
	}



	public String getNombre() {
		return nombre;
	}



	public void setNombre(String nombre) {
		this.nombre = nombre;
	}



	public int getCantPeriodos() {
		return cantPeriodos;
	}



	public void setCantPeriodos(int cantPeriodos) {
		this.cantPeriodos = cantPeriodos;
	}



	public Evolucion<Double> getCostoVarPrepago() {
		return costoVarPrepago;
	}



	public void setCostoVarPrepago(Evolucion<Double> costoVarPrepago) {
		this.costoVarPrepago = costoVarPrepago;
	}






	public double[] getVolumenesPrepagosIniciales() {
		return volumenesPrepagosIniciales;
	}



	public void setVolumenesPrepagosIniciales(double[] volumenesPrepagosIniciales) {
		this.volumenesPrepagosIniciales = volumenesPrepagosIniciales;
	}



	public int getCantSubconjuntos() {
		return cantSubconjuntos;
	}



	public void setCantSubconjuntos(int cantSubconjuntos) {
		this.cantSubconjuntos = cantSubconjuntos;
	}



	public Hashtable<Integer, Integer> getSubconjuntoDelPoste() {
		return subconjuntoDelPoste;
	}



	public void setSubconjuntoDelPoste(Hashtable<Integer, Integer> subconjuntoDePoste) {
		this.subconjuntoDelPoste = subconjuntoDePoste;
	}



	public int getPeriodoUltimaLlamada() {
		return periodoUltimaLlamada;
	}



	public void setPeriodoUltimaLlamada(int periodoUltimaLlamada) {
		this.periodoUltimaLlamada = periodoUltimaLlamada;
	}



	public int getSentido() {
		return sentido;
	}



	public void setSentido(int sentido) {
		this.sentido = sentido;
	}



	

	
	
	
	
	
	


}
