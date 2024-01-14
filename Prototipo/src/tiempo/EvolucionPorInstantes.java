/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EvolucionPorInstantes is part of MOP.
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

/**
 * Subclase de una evolución que utiliza en su representación una lista valor,
 * instante Debe estar definido el valor para el instante 0 El valorizador esta
 * defenido de forma que en los instantes de cambio vale el nuevo valor
 * (continua por derecha)
 * 
 * @author ut602614
 *
 * @param <T>
 */
public class EvolucionPorInstantes<T> extends Evolucion<T> implements Serializable {
	private int sentidoLlamadaAnterior; // 1 si est� avanzando y -1 si est�
										// retrocediendo en el tiempo

	private Long siguienteInstanteInicial;
	private int siguienteIndiceInicial;
	private long instanteInvocacionAnterior;

	private Long siguienteInstante;
	private int siguienteIndice;
	private T valorInicial;

	private Hashtable<Long, T> valorizador; // es donde se almacena el
												// instante en que cambia y el
												// nuevo valor DEBE ESTAR
												// DEFINIDO EL INSTANTE
												// INICIAL!!!
	private ArrayList<Long> instantesOrdenados; // son los instantes
													// ordenados para recorrer y
													// extraer luego del
													// valorizador

	private boolean nomascambios;

	public EvolucionPorInstantes(Hashtable<Long, T> vals, T valorInicial, SentidoTiempo st) {
		super(st);
		this.valorizador = vals;
		this.instantesOrdenados = new ArrayList<Long>();
		generarValorizadoresInstantes();
		sentidoLlamadaAnterior = st.getSentido();
		setNomascambios(false);

		reinicializarSegunSentido();

		this.siguienteInstante = instantesOrdenados.get(siguienteIndice);
		this.valorInicial = valorInicial;
		this.valor = vals.get(siguienteInstante);
		this.siguienteInstanteInicial = this.siguienteInstante;
		this.siguienteIndiceInicial = this.siguienteIndice;

	}

	private void reinicializarSegunSentido() {
		if (sentido.getSentido() == 1) {
			siguienteIndice = 0;
			instanteInvocacionAnterior = -1;
			siguienteInstante = instantesOrdenados.get(siguienteIndice);
		} else {
			siguienteIndice = valorizador.size() - 1;
			instanteInvocacionAnterior = Integer.MAX_VALUE-1;
			siguienteInstante = instantesOrdenados.get(siguienteIndice);
		}

	}

	public void inicializarParaSimulacion() {
		this.siguienteInstante = this.siguienteInstanteInicial;
		this.siguienteIndice = this.siguienteIndiceInicial;
	}


	private void generarValorizadoresInstantes() {
		Set<Long> claves = valorizador.keySet();
		Iterator<Long> it = claves.iterator();
		while (it.hasNext()) {
			this.instantesOrdenados.add(it.next());
		}

		Collections.sort(this.instantesOrdenados);

	}

	// public T dameValor(int instante){
	// int i = 0;
	//
	// for (i = 0; i<instantesOrdenados.size();i++) {
	// if (instantesOrdenados.get(i)>instante) break;
	// }
	// return this.valorizador.get(instantesOrdenados.get(i-1));
	// }

	public ArrayList<Long> getInstantesOrdenados() {
		return instantesOrdenados;
	}

	public void setInstantesOrdenados(ArrayList<Long> instantesOrdenados) {
		this.instantesOrdenados = instantesOrdenados;
	}

	@Override
	public T getValor(long instante) {
		if (instante == instanteInvocacionAnterior)
			return valor;
	
		if (sentido.getSentido() != sentidoLlamadaAnterior
				|| sentido.getSentido() * (instante - instanteInvocacionAnterior) < 0) {
			reinicializarSegunSentido();
		} else {
			instanteInvocacionAnterior = instante;
		}
		
		sentidoLlamadaAnterior = this.sentido.getSentido();
		

		if (instante < instantesOrdenados.get(0)) {
			valor = valorInicial;	
			
			return valor;
		}
		if (instante >= instantesOrdenados.get(instantesOrdenados.size() - 1)) {
			valor = valorizador.get(instantesOrdenados.get(instantesOrdenados.size() - 1));
			return valor;
		}
		if (this.sentido.getSentido() == 1) {

			while (instante >= siguienteInstante) {
				siguienteIndice += 1;
				siguienteInstante = instantesOrdenados.get(siguienteIndice);

			}
			valor = valorizador.get(instantesOrdenados.get(siguienteIndice - 1));

		} else {

			while (instante < siguienteInstante) {
				siguienteIndice -= 1;
				siguienteInstante = instantesOrdenados.get(siguienteIndice);

			}
			valor = valorizador.get(siguienteInstante);

		}

		return valor;
	}
	
	public Long getSiguienteInstante() {
		return siguienteInstante;
	}

	public void setSiguienteInstante(Long siguienteInstante) {
		this.siguienteInstante = siguienteInstante;
	}

	public boolean isNomascambios() {
		return nomascambios;
	}

	public void setNomascambios(boolean nomascambios) {
		this.nomascambios = nomascambios;
	}

	public static void main(String[] args) {

		Hashtable<Long, Double> vals = new Hashtable<Long, Double>();
		vals.put(3L, 23.2);
		vals.put(5L, 2.2);
		vals.put(9L, 11.2);

		System.out.println("SENTIDO = 1" );
		SentidoTiempo st = new SentidoTiempo(1);
		Evolucion<Double> ev = new EvolucionPorInstantes<Double>(vals, 100.0, st);
		for (int i = 0; i <= 10; i++) {
			System.out.println("Instante=" + i + " : " + ev.getValor(i));
		}
		System.out.println("----------------------------------------------------------" );
		System.out.println("----------------------------------------------------------" );
		System.out.println();
		
		System.out.println("SENTIDO = -1" );
		st = new SentidoTiempo(-1);
		ev.setSentido(st);

		for (int i = 10; i >= 0; i--) {
			System.out.println("Instante=" + i + " : " + ev.getValor(i));
		}
		System.out.println("----------------------------------------------------------" );
		System.out.println("----------------------------------------------------------" );
		System.out.println();
		
		System.out.println("SENTIDO = 1" );		
		st = new SentidoTiempo(1);
		ev.setSentido(st);
		
		for (int i = 10; i >= 0; i--) {
			System.out.println("Instante=" + i + " : " + ev.getValor(i));
		}

		System.out.println("----------------------------------------------------------" );
		System.out.println("----------------------------------------------------------" );
		System.out.println();
		System.out.println("SENTIDO = -1" );		
		st = new SentidoTiempo(-1);
		ev.setSentido(st);
		
		for (int i = 0; i < 10; i++) {
			System.out.println("Instante=" + i + " : " + ev.getValor(i));
		}


	}

	public int getSentidoLlamadaAnterior() {
		return sentidoLlamadaAnterior;
	}

	public void setSentidoLlamadaAnterior(int sentidoLlamadaAnterior) {
		this.sentidoLlamadaAnterior = sentidoLlamadaAnterior;
	}

	public long getInstanteInvocacionAnterior() {
		return instanteInvocacionAnterior;
	}

	public void setInstanteInvocacionAnterior(int instanteInvocacionAnterior) {
		this.instanteInvocacionAnterior = instanteInvocacionAnterior;
	}

	public T getValorInicial() {
		return valorInicial;
	}

	public void setValorInicial(T valorInicial) {
		this.valorInicial = valorInicial;
	}

	public Hashtable<Long, T> getValorizador() {
		return valorizador;
	}

	public void setValorizador(Hashtable<Long, T> valorizador) {
		this.valorizador = valorizador;
	}

	@Override
	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();
		if(valorizador == null){ errores.add("EvolucionPorInstantes: valorizador vacio");}
		else {

			for (Map.Entry<Long, T> entry : valorizador.entrySet()) {
				if(entry.getValue() == null) {errores.add("EvolucionPorInstantes: valorizador vacio");}
			}

		}

		if(instantesOrdenados == null){ errores.add("EvolucionPorInstantes: instantesOrdenados vacio");}
		else {
			for (int i=0;i<instantesOrdenados.size();i++) {

				if(instantesOrdenados.get(i) == null) { errores.add("EvolucionPorInstantes: instantesOrdenados vacio");}
			}
		}

		return errores;
	}

	@Override
	public ArrayList<String> controlRango(Double min, Double max, ArrayList<String> err) {

		for (Map.Entry<Long, T> entry : valorizador.entrySet()) {
			Double va = (Double) entry.getValue();
			if(va > max  || va < min) {
				err.add("EvolucionPorInstantes: valorizador fuera de rango");
			}
		}

		return err;
	}
	@Override
	public ArrayList<String> controlRango(Integer min, Integer max, ArrayList<String> err) {
		for (Map.Entry<Long, T> entry : valorizador.entrySet()) {
			Integer va = (Integer) entry.getValue();
			if(va > max  || va < min) {
				err.add("EvolucionPorInstantes: valorizador fuera de rango");
			}
		}
		return err;
	}

}
