/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EvolucionDeterministica is part of MOP.
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

package tiempo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import utilitarios.Constantes;
import datatypes.Pair;

/**
 * Evolución determinóstica OJO!!! PROBABLEMENTE SE TIRE ESTA CLASE Y PARTE DE SU LóGICA SE HAGA EN LA PRESENTACIóN, REPRESENTANDO 
 * EN UNA EVOLUCIóN POR INSTANTES LO NECESARIO
 * @author ut602614
 * 
 *
 */

public class EvolucionDeterministica extends Evolucion<Double> implements Serializable{
	private ArrayList<Pair<Integer, Double>> determinante; //contiene los valores Fijos
	private ArrayList<Pair<Integer, Double>> periodicaAnual; //contiene los cambios periodicos anualmente
	private Hashtable<Integer, Double> valorizador;	// es donde se almacena el instante en que cambia y el nuevo valor
	private ArrayList<Integer> instantesOrdenados; // son los instantes ordenados para recorrer y extraer luego del valorizador
	
	public EvolucionDeterministica(
			ArrayList<Pair<Integer, Double>> determinante,
			ArrayList<Pair<Integer, Double>> periodicaAnual, SentidoTiempo st) {
		super(st);
		this.determinante = determinante;
		this.periodicaAnual = periodicaAnual;
		this.valorizador = new Hashtable<Integer,Double>();
		this.instantesOrdenados = new ArrayList<Integer>();
		generarValorizadoreInstantes();		
	}

	
	private void generarValorizadoreInstantes() {
		for(Pair<Integer, Double> p: determinante) {
			this.valorizador.put(p.first, p.second);
			this.instantesOrdenados.add(p.first);
		}
		
		for(Pair<Integer, Double> p: periodicaAnual) {
			Double valor = this.valorizador.get(p.first);
			if (this.valorizador.containsKey(p.first)) {
				valor+=p.second;				
			} else {
				valor=p.second;
			}
			this.valorizador.put(p.first, valor);
			this.instantesOrdenados.add(p.first);
		}
		
		Collections.sort(this.instantesOrdenados);
	}

	
	@Override
	public void inicializarParaSimulacion() {
		// DELIBERADAMENTE VACóO
		
	}



	//debe estar definido el instante inicial como instante 0
	public Double dameValor(Integer instante){
		int i = 0;
		while (this.instantesOrdenados.get(i) > instante) {
			i++;
		}
		return this.valorizador.get(i-1);
	}
	
//	public Double dameValor(PasoTiempo paso){
//		Double valorInicial = dameValor(paso.getInstanteInicial());
//		Double valorFinal= dameValor(paso.getInstanteInicial());
//		if (Math.abs(valorInicial - valorFinal) < Constantes.EPSILONCOEF) {
//			return valorFinal;
//		}
//		//TODO: ACó VA LA INTERPOLACIóN
//		return null;
//	}
	
	public ArrayList<Pair<Integer, Double>> getDeterminante() {
		return determinante;
	}

	public void setDeterminante(ArrayList<Pair<Integer, Double>> determinante) {
		this.determinante = determinante;
	}

	public ArrayList<Pair<Integer, Double>> getPeriodicaAnual() {
		return periodicaAnual;
	}

	public void setPeriodicaAnual(ArrayList<Pair<Integer, Double>> periodicaAnual) {
		this.periodicaAnual = periodicaAnual;
	}


	public ArrayList<Integer> getInstantesOrdenados() {
		return instantesOrdenados;
	}


	public void setInstantesOrdenados(ArrayList<Integer> instantesOrdenados) {
		this.instantesOrdenados = instantesOrdenados;
	}


	@Override
	public Double getValor(long instante) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();

		if(determinante == null) { errores.add( "Evolucion Deterministica: determinante vacio"); }
		else {
			determinante.forEach((d) -> {
				if (d.first == null) {
					errores.add("Evolucion Deterministica: determinante vacio");
				}
				if (d.second == null) {
					errores.add("Evolucion Deterministica: determinante vacio");
				}
			});
		}
		if(periodicaAnual == null) { errores.add( "Evolucion Deterministica: periodicaAnual vacio"); }
		else {
			periodicaAnual.forEach((d) -> {
				if (d.first == null) {
					errores.add("Evolucion Deterministica: periodicaAnual vacio");
				}
				if (d.second == null) {
					errores.add("Evolucion Deterministica: periodicaAnual vacio");
				}
			});
		}

		if(valorizador == null) { errores.add( "Evolucion Deterministica: valorizador vacio"); }
		else {
			valorizador.forEach((k,v) -> { if(v == null) errores.add("Evolucion Deterministica: valorizador vacio"); } );
		}

		if(instantesOrdenados == null) { errores.add( "Evolucion Deterministica: instantesOrdenados vacio"); }
		else {
			instantesOrdenados.forEach((v) -> { if(v == null) errores.add("Evolucion Deterministica: instantesOrdenados vacio"); } );
		}

		return errores;
	}



	public ArrayList<String> controlRango(Double min, Double max, ArrayList<String> err ) {
		//TODO control Rango en Evolucion Deterministica
		return err;

	}

	@Override
	public ArrayList<String> controlRango(Integer min, Integer max, ArrayList<String> err) {
		//TODO control Rango en Evolucion Deterministica
		return null;
	}

}
