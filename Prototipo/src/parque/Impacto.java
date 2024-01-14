/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Impacto is part of MOP.
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

import compdespacho.HidraulicoCompDesp;
import compdespacho.ImpactoCompDesp;
import compgeneral.HidraulicoComp;
import compgeneral.ImpactoComp;
import compsimulacion.HidraulicoCompSim;
import compsimulacion.ImpactoCompSim;
import cp_compdespProgEst.CicloCombCompDespPE;
import cp_compdespProgEst.ImpactoCompDespPE;
import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesSalida.DatosBarraSP;
import datatypesSalida.DatosHidraulicoSP;
import datatypesSalida.DatosImpactoSP;
import datatypesSalida.DatosSalidaPaso;
import logica.CorridaHandler;
import procesosEstocasticos.ProcesoEstocastico;
import tiempo.Evolucion;
import utilitarios.Constantes;
import utilitarios.Funcion1V;

/**
 * Clase que representa el el impacto generado
 * 
 * @author ut602614
 *
 */
public class Impacto extends Participante {

//	private String unidad;
	private String nombre;
	private Evolucion<Double> costoUnit;
	private Evolucion<Double> limite;
	private ArrayList<Participante> involucrados; /* Lista de participantes que contribuyen al impacto */
	private static ArrayList<String> atributosDetallados;
	private int tipoImpacto;

	private ImpactoCompDesp compD;
	private ImpactoCompSim compS;
	private ImpactoComp compG;
	private boolean porPoste;
	private boolean porUnidadTiempo;
	private Evolucion<Boolean> activo;

	public Impacto(String nombre, Evolucion<Boolean> activo, Evolucion<Double> costoUnit, Evolucion<Double> limite,
			boolean porPoste, ArrayList<Participante> involucrados, int tipoImpacto, boolean porUnidadTiempo) {
		super();
		this.nombre = nombre;
		this.setActivo(activo);
		this.costoUnit = costoUnit;
		this.limite = limite;
		this.involucrados = involucrados;
		this.tipoImpacto = tipoImpacto;
		this.setPorUnidadTiempo(porUnidadTiempo);
		this.setPorPoste(porPoste);
		compD = new ImpactoCompDesp(this);
		compG = new ImpactoComp(this, compD, compS);
		compS = new ImpactoCompSim(this, compD, compG);
		compG.setCompS(compS);

		compS.setCompgeneral(compG);
		compS.setCompdespacho(compD);
		compG.setCompSimulacion(compS);
		compG.setCompDespacho(compD);
		this.setCompDesp(compD);
		this.setCompGeneral(compG);
		this.setCompSimulacion(compS);
		compD.setParticipante(this);
		compS.setParticipante(this);
		compG.setParticipante(this);

	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Evolucion<Double> getCostoUnit() {
		return costoUnit;
	}

	public void setCostoUnit(Evolucion<Double> costoUnit) {
		this.costoUnit = costoUnit;
	}

	public Evolucion<Double> getLimite() {
		return limite;
	}

	public void setLimite(Evolucion<Double> limite) {
		this.limite = limite;
	}

	public ArrayList<Participante> getInvolucrados() {
		return involucrados;
	}

	public void setInvolucrados(ArrayList<Participante> involucrados) {
		this.involucrados = involucrados;
	}

	@Override
	public void actualizarParaProximoPaso(DatosSalidaProblemaLineal salidaIter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void inicializarParaEscenario() {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<ProcesoEstocastico> getProcesosOptim() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void asignaVAOptim() {
		// DELIBERADAMENTE EN BLANCO

	}

	@Override
	public void asignaVASimul() {
		// DELIBERADAMENTE EN BLANCO

	}

	public static ArrayList<String> getAtributosDetallados() {
		return atributosDetallados;
	}

	public static void setAtributosDetallados(ArrayList<String> atributosDetallados) {
		Impacto.atributosDetallados = atributosDetallados;
	}

	public int getTipoImpacto() {
		return tipoImpacto;
	}

	public void setTipoImpacto(int tipoImpacto) {
		this.tipoImpacto = tipoImpacto;
	}

	@Override
	public void aportarImpacto(Impacto i, DatosObjetivo costo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void guardarResultadoPaso(DatosSalidaPaso resultadoPaso, DatosSalidaProblemaLineal salidaUltimaIter,
			String fase, long instante) {
		instante = this.getSimPaso().getCorrida().getLineaTiempo().pasoActual().getInstanteFinal();
		DatosImpactoSP imp = null;
		if (this.activo.getValor(instante)) {
			double[] costoPostes = null;
			double magnitudPaso = 0.0;
			double[] magniutdPostes = null;
			double multiplicador = 1.0;
			double costoPaso = this.compS.calculaCostoPaso(salidaUltimaIter);
			if (this.isPorPoste()) {
				magniutdPostes = new double[this.getCantPostes()];
				costoPostes = new double[this.getCantPostes()];

				for (int p = 0; p < this.getCantPostes(); p++) {
					if (this.activo.getValor(instante)) {
						if (this.isPorUnidadTiempo() || this.getTipoImpacto() == Constantes.HIDRO_CAUDAL_ECOLOGICO
								|| this.getTipoImpacto() == Constantes.HIDRO_VERTIMIENTO_EXTERNO)
							multiplicador = this.getDuracionPostes(p);
						if (this.getActivo().getValor(instante)) {
							try {
								costoPostes[p] = this.getCostoUnit().getValor(instante) / Constantes.M3XHM3
										* salidaUltimaIter.getSolucion()
												.get(this.compD.generarNombre("exceso", Integer.toString(p)))
										* multiplicador;
							} catch (Exception e) {
								int pp = 0;

							}
							magniutdPostes[p] = salidaUltimaIter.getSolucion()
									.get(this.compD.generarNombre("exceso", Integer.toString(p))) / Constantes.M3XHM3
									* multiplicador;
							if (this.getTipoImpacto() == Constantes.HIDRO_CAUDAL_ECOLOGICO
									|| this.getTipoImpacto() == Constantes.HIDRO_VERTIMIENTO_EXTERNO)
								magniutdPostes[p] *= Constantes.M3XHM3 / multiplicador;
							magnitudPaso += magniutdPostes[p];
						}
					} else {
						costoPostes[p] = 0.0;
						magniutdPostes[p] = 0.0;
						magnitudPaso += magniutdPostes[p];
					}

				}
			} else {
				// if (this.isPorUnidadTiempo()) multiplicador = 1.0; //this.getDuracionPaso();

				if (this.isPorUnidadTiempo() || this.getTipoImpacto() == Constantes.HIDRO_CAUDAL_ECOLOGICO
						|| this.getTipoImpacto() == Constantes.HIDRO_VERTIMIENTO_EXTERNO)
					multiplicador = this.getDuracionPaso();
				if (this.getActivo().getValor(instante)) {
					magnitudPaso = salidaUltimaIter.getSolucion().get(this.compD.generarNombre("exceso"))
							/ Constantes.M3XHM3 * multiplicador;
					if (this.getTipoImpacto() == Constantes.HIDRO_CAUDAL_ECOLOGICO
							|| this.getTipoImpacto() == Constantes.HIDRO_VERTIMIENTO_EXTERNO)
						magnitudPaso *= Constantes.M3XHM3 / multiplicador;
				} else {
					magnitudPaso = 0.0;
				}

			}
			imp = new DatosImpactoSP(this.getNombre(), costoPaso, costoPostes, magnitudPaso, magniutdPostes);
		} else {
			double[] costoPostes = null;
			double magnitudPaso = 0.0;
			double[] magniutdPostes = null;
			double multiplicador = 1.0;

			if (this.isPorPoste()) {
				magniutdPostes = new double[this.getCantPostes()];
				costoPostes = new double[this.getCantPostes()];

				for (int p = 0; p < this.getCantPostes(); p++) {
					costoPostes[p] = 0.0;
					magniutdPostes[p] = 0.0;
					magnitudPaso += magniutdPostes[p];
				}

				imp = new DatosImpactoSP(this.getNombre(), 0.0, costoPostes, magnitudPaso, magniutdPostes);
			}

		}
		resultadoPaso.agregarImpacto(imp);
	}

	@Override
	public Double aportarCostoImpacto(Impacto impacto, DatosSalidaProblemaLineal salidaUltimaIter) {
		// TODO Auto-generated method stub
		return 0.0;
	}

	@Override
	public Hashtable<String, DatosRestriccion> cargarRestriccionesImpacto(Impacto impacto) {
		// TODO Auto-generated method stub
		return new Hashtable<String, DatosRestriccion>();
	}

	public boolean isPorPoste() {
		return porPoste;
	}

	public void setPorPoste(boolean porPoste) {
		this.porPoste = porPoste;
	}

	public boolean isPorUnidadTiempo() {
		return porUnidadTiempo;
	}

	public void setPorUnidadTiempo(boolean porUnidadTiempo) {
		this.porUnidadTiempo = porUnidadTiempo;
	}

	public Evolucion<Boolean> getActivo() {
		return activo;
	}

	public void setActivo(Evolucion<Boolean> activo) {
		this.activo = activo;
	}

	@Override
	public void crearCompDespPE() {
		ImpactoCompDespPE compDespPE = new ImpactoCompDespPE();
		this.setCompDespPE(compDespPE);
		compDespPE.setCompDesp(compD);
		compDespPE.setParticipante(this);
		
	}
	
	
}
