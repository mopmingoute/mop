/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Postizador is part of MOP.
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

package simulacion;

import java.util.ArrayList;

import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import persistencia.PersistenciaHandler;
import datatypes.DatosPostizacion;
import datatypes.ParIndexado;
import tiempo.PasoTiempo;
import utilitarios.Constantes;

public class Postizador {
	private int cantPasos;
	private PasoTiempo ptiempo;
	private ArrayList<Double> referencia;
	private ArrayList<ParIndexado> referenciaIndexada;

	private ArrayList<ArrayList<Integer>> coleccionNumpos; // todos los numpos de la corrida desde el paso inicial al
															// final
	private int[] interPorPoste;
	private int[] durPos;
	private int cantPos;
	private int numPaso;
	private String ruta;
	private boolean externa;
	private boolean clusterizar;
	private int kmin;
	private int kmax;

	public Postizador(DatosPostizacion dp) {
		coleccionNumpos = dp.getColnumpos();
	}

	public Postizador() {

	}

	public Postizador(int cantPasos, boolean externa) {
		this.cantPasos = cantPasos;
		this.externa = externa;
//		coleccionNumpos = new ArrayList<ArrayList<Integer>>();
//		for(int i=0; i<cantPasos; i++){
//			coleccionNumpos.add(new ArrayList<Integer>());
//		}
	}

	public PostizacionPaso postizar() {
		if (externa)
			return devolverExterna();
		else {
			if (clusterizar && !ptiempo.getBloque().isCronologico()) {
				return calcularInternaClustering();
			} else {
				return calcularInterna();
			}

		}
	}

	private PostizacionPaso calcularInternaClustering() {

		ArrayList<Integer> numpos = new ArrayList<Integer>();
		for (int i = 0; i < referencia.size(); i++) {
			numpos.add(0);
		}

		referenciaIndexada = new ArrayList<ParIndexado>();
		for (int i = 0; i < referencia.size(); i++)
			referenciaIndexada.add(new ParIndexado(referencia.get(i), i));
		Collections.sort(referenciaIndexada);

		/**
		 * K-MEANS CLUSTERING
		 */
		boolean converge = false;
		int k = this.kmax;
		Hashtable<Integer, Double> centrosIniciales = new Hashtable<Integer, Double>();
		Hashtable<Integer, Double> centrosActuales = new Hashtable<Integer, Double>();
		Hashtable<Integer, ArrayList<ParIndexado>> clustersActuales = new Hashtable<Integer, ArrayList<ParIndexado>>();
		int cantInstantesMuestreo = referencia.size();
		double tamanioTrozo = (referenciaIndexada.get(cantInstantesMuestreo - 1).first
				- referenciaIndexada.get(0).first) / k;
		for (int i = 0; i < k; i++) {
			centrosIniciales.put(i, referenciaIndexada.get(0).first + tamanioTrozo * i + tamanioTrozo / 2);
			clustersActuales.put(i, new ArrayList<ParIndexado>());
		}
		centrosActuales = (Hashtable<Integer, Double>) centrosIniciales.clone();
		double epsilon = 0.0001;
		int j = 0;
		while (!converge) {
			j++;
			for (int i = 0; i < k; i++) {
				clustersActuales.get(i).clear();
			}
			for (ParIndexado p : referenciaIndexada) {
				int centroMasCercano = 0;
				double menorDistancia = 1000000;
				for (int i = 0; i < k; i++) {
					double dist = distancia(p, centrosIniciales.get(i));
					if (dist < menorDistancia) {
						centroMasCercano = i;
						menorDistancia = dist;
					}
				}
				clustersActuales.get(centroMasCercano).add(p);
			}

			centrosIniciales = (Hashtable<Integer, Double>) centrosActuales.clone();
			centrosActuales.clear();
			for (int i = 0; i < k; i++) {
				double baricentro = 0;
				for (ParIndexado p : clustersActuales.get(i)) {
					baricentro += p.first;
				}
				if (clustersActuales.get(i).size() != 0)
					centrosActuales.put(i, baricentro / clustersActuales.get(i).size());
				else
					centrosActuales.put(i, centrosIniciales.get(i));
			}
			converge = chequearConvergencia(centrosActuales, centrosIniciales, epsilon);
			centrosIniciales = (Hashtable<Integer, Double>) centrosActuales.clone();
		}

		durPos = new int[k];
		for (int i = 0; i < k; i++) {
			int dur = 0;
			if (clustersActuales.get(i).size() == 0) {
				double menorDistancia = 100000;
				int centroMasCercano = 0;
				int indiceMasCercano = 0;
				
				for (int w = 0; w < k; w++) {
					int indice = 0;
					for (ParIndexado p : clustersActuales.get(w)) {
						double dist = distancia(p, centrosIniciales.get(i));						
						if (dist < menorDistancia) {
							centroMasCercano = w;
							menorDistancia = dist;
							indiceMasCercano = indice;
						}
						indice++;
					}					
				}
				ParIndexado aCambiar = clustersActuales.get(centroMasCercano).get(indiceMasCercano);
				clustersActuales.get(i).add(aCambiar);
			}
			for (ParIndexado p : clustersActuales.get(i)) {
				numpos.set(p.second, i + 1);
				dur++;
			}
			durPos[i] = dur * Constantes.SEGUNDOSXHORA;
		}
		interPorPoste = new int[k];
		for (int i = 0; i < k; ++i) {
			interPorPoste[i] = durPos[i] / ptiempo.getBloque().getIntervaloMuestreo();
		}
		PostizacionPaso resultado = new PostizacionPaso(ptiempo,
				ptiempo.getDuracionPaso() / ptiempo.getIntervaloMuestreo(), numpos, interPorPoste, k, durPos);

		ptiempo.getBloque().setCantPostes(k);
		ptiempo.getBloque().setDuracionPostes(durPos);

		resultado.setCantPos(k);
		resultado.setNumpos(numpos);
		return resultado;
	}

	private boolean chequearConvergencia(Hashtable<Integer, Double> centrosActuales,
			Hashtable<Integer, Double> centrosIniciales, double epsilon) {

		for (Integer i : centrosActuales.keySet()) {
			double minimo = 100000;
			for (Integer j : centrosIniciales.keySet()) {
				minimo = Math.min(minimo, distancia(centrosActuales.get(i), centrosIniciales.get(j)));
			}
			if (minimo > epsilon)
				return false;
		}
		return true;
	}

	private double distancia(Double a, Double b) {
		return Math.abs(a - b);
	}

	private double distancia(ParIndexado p, Double b) {
		return distancia(p.first, b);
	}

	private PostizacionPaso devolverExterna() {
		determinarDurPosCantPosInterPos();
		PostizacionPaso postizacion = new PostizacionPaso(ptiempo,
				ptiempo.getDuracionPaso() / ptiempo.getIntervaloMuestreo(), coleccionNumpos.get(numPaso), interPorPoste,
				cantPos, durPos);
		return postizacion;
	}

	private void determinarDurPosCantPosInterPos() {
		ArrayList<Integer> numpos = coleccionNumpos.get(numPaso);

		cantPos = 0;
		for (Integer i : numpos) {
			if (i > cantPos) {
				cantPos = i;
			}
		}
		interPorPoste = new int[cantPos];

		for (Integer i : numpos) {
			interPorPoste[i - 1] += 1;
		}

		durPos = new int[cantPos];
		for (int i = 0; i < cantPos; i++) {
			durPos[i] = interPorPoste[i] * ptiempo.getIntervaloMuestreo();
		}
	}

	public void cargarExterna(GregorianCalendar inicio) {
		PersistenciaHandler ph = PersistenciaHandler.getInstance();
		DatosPostizacion datos = ph.leerPostizacionExterna(ruta, inicio);
		this.coleccionNumpos = datos.getColnumpos();
		/** TODO:TENER EN CUENTA QUE HAY QUE VALIDAR FECHAS */

	}

	/**
	 * @return
	 */
	private PostizacionPaso calcularInterna() {
		PostizacionPaso resultado = new PostizacionPaso(ptiempo,
				ptiempo.getDuracionPaso() / ptiempo.getIntervaloMuestreo(), null, interPorPoste, cantPos, durPos);

		ArrayList<Integer> numpos = new ArrayList<Integer>();
		for (int i = 0; i < referencia.size(); i++) {
			numpos.add(0);
		}
		if (!ptiempo.getBloque().isCronologico()) {
			referenciaIndexada = new ArrayList<ParIndexado>();
			for (int i = 0; i < referencia.size(); i++)
				referenciaIndexada.add(new ParIndexado(referencia.get(i), i));
			Collections.sort(referenciaIndexada);

			int numPosAux = cantPos;
			int cantRecorridos = 0;
			for (ParIndexado p : referenciaIndexada) {
				numpos.set(p.second, numPosAux);
				cantRecorridos++;
				if (cantRecorridos >= interPorPoste[numPosAux - 1]) {
					numPosAux--;
					cantRecorridos = 0;
				}
			}

		} else {
			int iNumpos = 0;
			for (int i = 0; i < interPorPoste.length; i++) {
				for (int j = 0; j < interPorPoste[i]; j++) {
					numpos.set(iNumpos, i + 1);
					iNumpos++;
				}
			}
		}
		resultado.setCantPos(this.cantPos);
		resultado.setNumpos(numpos);
		return resultado;
//		cargar la coleccionNumpos (i el paso de tiempo)
	}

	public PasoTiempo getPtiempo() {
		return ptiempo;
	}

	public void setPtiempo(PasoTiempo ptiempo) {
		this.ptiempo = ptiempo;
	}

	public Integer getCantidadPostes(PasoTiempo pasoActual) {
		return pasoActual.getBloque().getCantPostes();
	}

	public ArrayList<Double> getReferencia() {
		return referencia;
	}

	public void setReferencia(ArrayList<Double> referencia) {
		this.referencia = referencia;
	}

	public int[] getInterPorPoste() {
		return interPorPoste;
	}

	public void setInterPorPoste(int[] interPorPoste) {
		this.interPorPoste = interPorPoste;
	}

	public int getCantPos() {
		return cantPos;
	}

	public void setCantPos(int cantPos) {
		this.cantPos = cantPos;
	}

	public String getRuta() {
		return ruta;
	}

	public void setRuta(String ruta) {
		this.ruta = ruta;
	}

	public boolean isExterna() {
		return externa;
	}

	public void setExterna(boolean externa) {
		this.externa = externa;
	}

	public ArrayList<ParIndexado> getReferenciaIndexada() {
		return referenciaIndexada;
	}

	public void setReferenciaIndexada(ArrayList<ParIndexado> referenciaIndexada) {
		this.referenciaIndexada = referenciaIndexada;
	}

	public ArrayList<ArrayList<Integer>> getColeccionNumpos() {
		return coleccionNumpos;
	}

	public void setColeccionNumpos(ArrayList<ArrayList<Integer>> coleccionNumpos) {
		this.coleccionNumpos = coleccionNumpos;
	}

	public int getNumPaso() {
		return numPaso;
	}

	public void setNumPaso(int numPaso) {
		this.numPaso = numPaso;
	}

	public int[] getDurPos() {
		return durPos;
	}

	public void setDurPos(int[] durPos) {
		this.durPos = durPos;
	}

	public int getCantPasos() {
		return cantPasos;
	}

	public void setCantPasos(int cantPasos) {
		this.cantPasos = cantPasos;
	}

	public boolean isClusterizar() {
		return clusterizar;
	}

	public void setClusterizar(boolean clusterizar) {
		this.clusterizar = clusterizar;
	}

	public int getKmin() {
		return kmin;
	}

	public void setKmin(int kmin) {
		this.kmin = kmin;
	}

	public int getKmax() {
		return kmax;
	}

	public void setKmax(int kmax) {
		this.kmax = kmax;
	}

}
