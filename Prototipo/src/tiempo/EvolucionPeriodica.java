/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EvolucionPeriodica is part of MOP.
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
import java.util.*;


public class EvolucionPeriodica<T> extends Evolucion<T> implements Serializable{
	private EvolucionPorInstantes<T> determinante;
	private int periodo;	
	private int cantPeriodos;
	private EvolucionPorInstantes<T> definicionPeriodo;
	
	public EvolucionPeriodica(GregorianCalendar tiempoInicial, EvolucionPorInstantes<T> definicionPeriodo, int periodo, int cantPeriodos, SentidoTiempo sentido) {
		super(sentido);
		this.setPeriodo(periodo);
		this.setCantPeriodos(cantPeriodos);
		this.definicionPeriodo = definicionPeriodo;
		long instanteInicioEv = definicionPeriodo.getInstantesOrdenados().get(0);
		this.definicionPeriodo.setValorInicial(definicionPeriodo.getValorizador().get(instanteInicioEv));
		Hashtable<Long, T> paraDeterminante = (Hashtable<Long, T>)definicionPeriodo.getValorizador().clone();
		ArrayList<Long> instantesOrdenados = definicionPeriodo.getInstantesOrdenados();
		
		
		long horasASumar = instanteInicioEv/3600;		
		
		GregorianCalendar ref = (GregorianCalendar)tiempoInicial.clone();
		ref.add(GregorianCalendar.HOUR,(int)horasASumar);
		long instanteNuevo;
		for (int i = 1; i < cantPeriodos; i++) {
			ref.add(periodo, 1);
			//System.out.println(ref.getTime());
			instanteNuevo = restarFechas(ref, tiempoInicial);
			paraDeterminante.put(instanteNuevo, definicionPeriodo.getValorInicial());
			for (Long inst: instantesOrdenados) {
				paraDeterminante.put(instanteNuevo+inst, definicionPeriodo.getValorizador().get(inst));
			}
			
		}
		setDeterminante(new EvolucionPorInstantes<T>(paraDeterminante,definicionPeriodo.getValorInicial(), sentido));
		//this.imprimir();
	}

	public EvolucionPorInstantes<T> getDefinicionPeriodo() {
		return definicionPeriodo;
	}

	public void setDefinicionPeriodo(EvolucionPorInstantes<T> definicionPeriodo) {
		this.definicionPeriodo = definicionPeriodo;
	}

	@Override
	public T getValor(long instante) {
		
		return determinante.getValor(instante);
	}
//	@Override
//	public T getValor(){
//		return determinante.getValor();
//	}

	@Override
	public void inicializarParaSimulacion() {
		// TODO Auto-generated method stub
		
	}

	private int restarFechas(GregorianCalendar fFin, GregorianCalendar fIni){
		return (int) ((fFin.getTimeInMillis()-fIni.getTimeInMillis())/1000);
	}

	public EvolucionPorInstantes<T> getDeterminante() {
		return determinante;
	}

	public void setDeterminante(EvolucionPorInstantes<T> determinante) {
		this.determinante = determinante;
	}
	public static void main(String[] args) {

		Hashtable<Long, Double> vals = new Hashtable<Long, Double>();
		vals.put(10L, 23.2);
		vals.put(10000000L, 2.2);
		vals.put(20000000L, 11.2);

		
		SentidoTiempo st = new SentidoTiempo(1);
		Evolucion<Double> ev = new EvolucionPorInstantes<Double>(vals, 100.0, st);		
		EvolucionPeriodica<Double> ev2 = new EvolucionPeriodica<Double>(new GregorianCalendar(2000, Calendar.JANUARY, 1), (EvolucionPorInstantes<Double>) ev, Calendar.YEAR, 10, st);
		
		System.out.println("Instante=1 : " + ev2.getValor(1));
		for (Long inst: ev2.getDeterminante().getInstantesOrdenados()) {
			System.out.println("Instante=" + inst + " : " + ev2.getValor(inst));
		}
		System.out.println("----------------------------------------------------------" );
		System.out.println("----------------------------------------------------------" );
		System.out.println();
		System.out.println(Calendar.DAY_OF_YEAR);
		

	}
	
	public void imprimir() {
		System.out.println("Instante=1 : " + this.getValor(1));
		
		for (Long inst: this.getDeterminante().getInstantesOrdenados()) {
			System.out.println("Instante=" + inst + " : " + this.getValor(inst));
		}
		System.out.println("----------------------------------------------------------" );
		System.out.println("----------------------------------------------------------" );
		System.out.println();
		System.out.println(Calendar.DAY_OF_YEAR);
	}
	

	public int getPeriodo() {
		return periodo;
	}

	public void setPeriodo(int periodo) {
		this.periodo = periodo;
	}

	public int getCantPeriodos() {
		return cantPeriodos;
	}

	public void setCantPeriodos(int cantPeriodos) {
		this.cantPeriodos = cantPeriodos;
	}

	@Override
	public ArrayList<String> controlDatosCompletos() {

		ArrayList<String> errores = new ArrayList<>();

		if(periodo == 0) { errores.add("Evolucion Periodica: periodo vacio"); }
		if(cantPeriodos == 0) { errores.add("Evolucion Periodica: cantPeriodos vacio"); }
		if(definicionPeriodo == null || definicionPeriodo.controlDatosCompletos().size() > 0){ errores.add("Evolucion Periodica: determinante vacio"); }

		return errores;
	}

	@Override
	public ArrayList<String> controlRango(Double min, Double max, ArrayList<String> err) {
		return this.getDefinicionPeriodo().controlRango(min, max, err);
	}

	@Override
	public ArrayList<String> controlRango(Integer min, Integer max, ArrayList<String> err) {
		return this.getDefinicionPeriodo().controlRango(min, max, err);
	}

}
