/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * AcumuladorCompDesp is part of MOP.
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

package compdespacho;

import java.util.Hashtable;

import datatypesProblema.DatosObjetivo;
import datatypesProblema.DatosRestriccion;
import datatypesProblema.DatosSalidaProblemaLineal;
import datatypesProblema.DatosVariableControl;
import logica.CorridaHandler;
import parque.Acumulador;
import utilitarios.Constantes;

public class AcumuladorCompDesp extends GeneradorCompDesp {
	private Acumulador ac;
	private Double factorUso;
	private Double potMax;
	private Double potMaxAlmac;
	private Double energMax;
	private double[] potObligatoria; // Potencia obligatoria a inyectar por problemas de red en cada poste
	private boolean hayPotObligatoria;
	private double costoFallaPOblig;
	private DatosSalidaProblemaLineal ultimoResultado;

	private Double energAlmacIni;

	private Double energAlmacIniPoste; // Para comportamiento CierraPaso con Balance Cronológico
	private double valEnerg;
	private Integer cantModDisp;
	public static String compPasoPorDefecto;
	private String[] npotAlmacp;
	private String[] npotAux;
	private String nombreRestriccionBalEnergia;
	private String[] nEnerAlmacp;
	private Double energAlmacFinPaso;

	public Double getEnergAlmacFinPaso() {
		return energAlmacFinPaso;
	}

	public AcumuladorCompDesp(Acumulador ac) {
		super();
		this.setParticipante(ac);
		this.ac = ac;
		this.parametros = new Hashtable<String, String>();
		ultimoResultado = new DatosSalidaProblemaLineal();

	}

	public void crearVariablesControl() {

//		ordinalPrimeraVariable = ordinalACargar;

		// crear potp
		for (int ip = 0; ip < participante.getCantPostes(); ip++) {
			String compPaso = parametros.get(Constantes.COMPPASO);

			String nombre = generarNombre("pot", Integer.toString(ip));

			Double cotaMax = cantModDisp * potMax;
			// if (compPaso.equalsIgnoreCase(Constantes.ACUCIERRAPASO)) {
			// cotaMax = null;}
			DatosVariableControl nv = new DatosVariableControl(nombre, Constantes.VCCONTINUA, Constantes.VCPOSITIVA,
					null, cotaMax);
			this.variablesControl.put(nombre, nv);
			getNpotp()[ip] = nombre;
			String nombrePotAlm = generarNombre("potAlmac", Integer.toString(ip));

			Double cotaMaxAlmac = cantModDisp * potMaxAlmac;
			if (potObligatoria[ip] > 0.0)
				cotaMaxAlmac = 0.0;

			nv = new DatosVariableControl(nombrePotAlm, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, null,
					cotaMaxAlmac);
			this.variablesControl.put(nombrePotAlm, nv);
			getNpotAlmacp()[ip] = nombrePotAlm;
			if (compPaso.equalsIgnoreCase(Constantes.ACUBALANCECRONOLOGICO)) {
				String nombreEnergiaAcum = generarNombre("energAcum", Integer.toString(ip));
				nv = new DatosVariableControl(nombreEnergiaAcum, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, 0.0,
						energMax);
				this.variablesControl.put(nombreEnergiaAcum, nv);
				getnEnerAlmacp()[ip] = nombreEnergiaAcum;
			}
			// Variable auxiliar para cargar restricción de distribución
			String nombreAux = generarNombre("auxRestDist", Integer.toString(ip));
			nv = new DatosVariableControl(nombreAux, Constantes.VCCONTINUA, Constantes.VCPOSITIVA, null,
					cotaMax * costoFallaPOblig);
			getNpotAux()[ip] = nombreAux;
			this.variablesControl.put(nombreAux, nv);

		}

	}

	private DatosRestriccion cargarRestModeloCierraPaso(int operador) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		DatosRestriccion nr = new DatosRestriccion();
		nr.setNombre(generarNombre("cierreEnergóa"));
		for (int p = 0; p < ac.getCantPostes(); p++) {
			nr.agregarTermino(getNpotp()[p], (double) ((double) ac.getDuracionPostes(p)
					/ ac.getRendIny().getValor(instanteActual) / Constantes.SEGUNDOSXHORA));
			nr.agregarTermino(getNpotAlmacp()[p], -(double) ((double) ac.getDuracionPostes(p)
					* ac.getRendAlmac().getValor(instanteActual) / Constantes.SEGUNDOSXHORA));
		}
		nr.setTipo(operador);
		nr.setSegundoMiembro(0.0);
		return nr;
	}

	private DatosRestriccion cargarRestModeloBalanceCronologicoPoste(int operador, int poste) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		DatosRestriccion nr = new DatosRestriccion();
		nr.setNombre(generarNombre("BalancePoste" + poste));
		nr.agregarTermino(getnEnerAlmacp()[poste + 1], 1.0); // energía almacenada en MWh
		nr.agregarTermino(getnEnerAlmacp()[poste], -1.0);
		nr.agregarTermino(getNpotp()[poste], (double) ((double) ac.getDuracionPostes(poste)
				/ ac.getRendIny().getValor(instanteActual) / Constantes.SEGUNDOSXHORA));
		nr.agregarTermino(getNpotAlmacp()[poste], -(double) ((double) ac.getDuracionPostes(poste)
				* ac.getRendAlmac().getValor(instanteActual) / Constantes.SEGUNDOSXHORA));
		nr.setTipo(operador);
		nr.setSegundoMiembro(0.0);
		return nr;
	}

	private DatosRestriccion cargarRestPotInyectadaObligatoria(int poste, double potObligatoria) {
		DatosRestriccion nr = new DatosRestriccion();
		nr.setNombre(generarNombre("PotObligatoria_" + poste));
		nr.agregarTermino(getNpotAux()[poste], 1.0);
		nr.agregarTermino(getNpotp()[poste], costoFallaPOblig * ac.getDuracionPostes(poste) / Constantes.SEGUNDOSXHORA);
		nr.setTipo(Constantes.RESTMAYOROIGUAL);
		nr.setSegundoMiembro(
				costoFallaPOblig * potObligatoria * ac.getDuracionPostes(poste) / Constantes.SEGUNDOSXHORA);
		return nr;
	}

	private DatosRestriccion cargarRestAlmacIniPaso(int operador, double energAlmacIniP) {

		DatosRestriccion nr = new DatosRestriccion();
		nr.setNombre(generarNombre("energAlmacIniPaso"));
		nr.agregarTermino(this.getnEnerAlmacp()[0], 1.0);
		nr.setTipo(operador);
		nr.setSegundoMiembro(energAlmacIniP);
		return nr;
	}

	private DatosRestriccion cargarRestAlmacFinPaso(int operador, double almacFinP) {
		// la energía almacenada en el último paso debe ser igual a la inicial
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		DatosRestriccion nr = new DatosRestriccion();
		nr.setNombre(generarNombre("energAlmacFinPaso"));
		int ultPoste = ac.getCantPostes() - 1;
		nr.agregarTermino(this.getnEnerAlmacp()[ultPoste], 1.0);
		nr.agregarTermino(this.getNpotAlmacp()[ultPoste], (double) ((double) ac.getDuracionPostes(ultPoste)
				* ac.getRendAlmac().getValor(instanteActual) / Constantes.SEGUNDOSXHORA));
		nr.agregarTermino(this.getNpotp()[ultPoste], -(double) ((double) ac.getDuracionPostes(ultPoste)
				/ ac.getRendIny().getValor(instanteActual) / Constantes.SEGUNDOSXHORA));
		nr.setTipo(operador);
		nr.setSegundoMiembro(almacFinP);
		return nr;
	}

	private DatosRestriccion cargarRestMaxAlmac(int operador, double energMax) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		DatosRestriccion nr = new DatosRestriccion();
		nr.setNombre(generarNombre("maxAlmacPaso"));
		for (int p = 0; p < ac.getCantPostes(); p++) {

			nr.agregarTermino(getNpotAlmacp()[p], (double) ((double) ac.getDuracionPostes(p)
					* ac.getRendAlmac().getValor(instanteActual) / Constantes.SEGUNDOSXHORA));
		}
		nr.setTipo(operador);
		nr.setSegundoMiembro(energMax * factorUso);

		return nr;
	}

	private DatosRestriccion cargarRestMaxIny(int operador, double energMax) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		DatosRestriccion nr = new DatosRestriccion();
		nr.setNombre(generarNombre("maxInyPaso"));
		for (int p = 0; p < ac.getCantPostes(); p++) {

			nr.agregarTermino(getNpotp()[p], (double) ((double) ac.getDuracionPostes(p)
					/ ac.getRendIny().getValor(instanteActual) / Constantes.SEGUNDOSXHORA));
		}
		nr.setTipo(operador);
		nr.setSegundoMiembro(energMax * factorUso);

		return nr;
	}

	private DatosRestriccion cargarRestEnergMax(int operador, Double energMaxima) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		DatosRestriccion nr = new DatosRestriccion();
		nr.setNombre(generarNombre("energMax"));
		for (int p = 0; p < ac.getCantPostes(); p++) {
			nr.agregarTermino(getNpotp()[p],
					-(double) ((double) ac.getDuracionPostes(p) / ac.getRendIny().getValor(instanteActual))
							/ Constantes.SEGUNDOSXHORA);
			nr.agregarTermino(getNpotAlmacp()[p],
					(double) ((double) ac.getDuracionPostes(p) * ac.getRendAlmac().getValor(instanteActual))
							/ Constantes.SEGUNDOSXHORA);

		}
		nr.setTipo(operador);
		nr.setSegundoMiembro(-energAlmacIni + energMaxima);

		return nr;
	}

	private DatosRestriccion cargarRestEnergMin(int operador, Double energMin) {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		DatosRestriccion nr = new DatosRestriccion();
		nr.setNombre(generarNombre("energMin"));
		for (int p = 0; p < ac.getCantPostes(); p++) {
			nr.agregarTermino(getNpotp()[p],
					-(double) ((double) ac.getDuracionPostes(p) / ac.getRendIny().getValor(instanteActual))
							/ Constantes.SEGUNDOSXHORA);
			nr.agregarTermino(getNpotAlmacp()[p],
					(double) ((double) ac.getDuracionPostes(p) * ac.getRendAlmac().getValor(instanteActual))
							/ Constantes.SEGUNDOSXHORA);

		}
		nr.setTipo(operador);

		nr.setSegundoMiembro(-energAlmacIni + energMin);

		return nr;
	}

	public void cargarRestricciones() {
		String compPaso = parametros.get(Constantes.COMPPASO);

		if (compPaso.equalsIgnoreCase(Constantes.ACUMULTIPASO)) {

			for (int p = 0; p < ac.getCantPostes(); p++) {
				DatosRestriccion nr = cargarRestEnergMax(Constantes.RESTMENOROIGUAL, energMax);
				this.restricciones.put(nr.getNombre(), nr);
			}
		} else if (compPaso.equalsIgnoreCase(Constantes.ACUBALANCECRONOLOGICO)) {
			/**
			 * Restricción para el poste 0, se iguala a la energía inicial
			 */
			DatosRestriccion nr = cargarRestAlmacIniPaso(Constantes.RESTIGUAL, energAlmacIniPoste);
			this.restricciones.put(nr.getNombre(), nr);

			/**
			 * Restricciones para los postes intermedios (1 a total -1), incluye que el
			 * poste final es igual al anterior
			 */
			for (int ip = 0; ip < ac.getCantPostes() - 1; ip++) {
				nr = cargarRestModeloBalanceCronologicoPoste(Constantes.RESTIGUAL, ip);
				this.restricciones.put(nr.getNombre(), nr);
			}

			/**
			 * Restricción para igualar postes intermedios (1 a total -1), incluye que al
			 * incio del poste final, se le suma y se le resta el intercambio con la red, y
			 * el fin del poste final coincide con el final del paso (que es el incio del
			 * paso siguiente).
			 */
			nr = cargarRestAlmacFinPaso(Constantes.RESTIGUAL, this.energAlmacFinPaso);

			for (int p = 0; p < ac.getCantPostes(); p++) {
				nr = cargarRestEnergMin(Constantes.RESTMAYOROIGUAL, 0.0);
				this.restricciones.put(nr.getNombre(), nr);
			}
		} else if (compPaso.equalsIgnoreCase(Constantes.ACUCIERRAPASO)) {
			DatosRestriccion nr = cargarRestModeloCierraPaso(Constantes.RESTIGUAL);
			this.restricciones.put(nr.getNombre(), nr);
			nr = cargarRestMaxAlmac(Constantes.RESTMENOROIGUAL, energMax);
			this.restricciones.put(nr.getNombre(), nr);
			nr = cargarRestMaxIny(Constantes.RESTMENOROIGUAL, energMax);
			this.restricciones.put(nr.getNombre(), nr);

		} else if (compPaso.equalsIgnoreCase(Constantes.ACUBALANCECRONOLOGICO)) {
			for (int ip = 0; ip < ac.getCantPostes() - 1; ip++) {
				DatosRestriccion nr = cargarRestModeloBalanceCronologicoPoste(Constantes.RESTIGUAL, ip);
				this.restricciones.put(nr.getNombre(), nr);
			}
			DatosRestriccion nr = cargarRestAlmacIniPaso(Constantes.RESTIGUAL, energAlmacIniPoste);
			this.restricciones.put(nr.getNombre(), nr);
			nr = cargarRestAlmacFinPaso(Constantes.RESTIGUAL, energAlmacIniPoste);
			this.restricciones.put(nr.getNombre(), nr);
		}
		// Restricción de Inyección Obligatoria para cada poste por problemas de
		// Trasmisión o Distribución
		if (hayPotObligatoria) {
			for (int ip = 0; ip < ac.getCantPostes() - 1; ip++) {
				DatosRestriccion nr = cargarRestPotInyectadaObligatoria(ip, potObligatoria[ip]);
				this.restricciones.put(nr.getNombre(), nr);
			}
		}
	}

	private DatosObjetivo cargarObjetivoValEnerg() {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();

		double pasosPorAnio = Constantes.SEGUNDOSXANIO / this.ac.getDuracionPaso();
		double tasaAnual = 0.08;
		double factorAnual = 1 / (1 + tasaAnual);
		double factorDescuentoPaso = Math.pow(factorAnual, 1 / pasosPorAnio);

		double raizFac = Math.pow(factorDescuentoPaso, 0.5);
		double valEnerg = this.valEnerg * raizFac;
		String compPaso = parametros.get(Constantes.COMPPASO);
		DatosObjetivo no = new DatosObjetivo();
		if (compPaso.equalsIgnoreCase(Constantes.ACUMULTIPASO)) {

			for (int p = 0; p < ac.getCantPostes(); p++) {
				no.agregarTermino(getNpotp()[p], (double) ((double) ac.getDuracionPostes(p)
						/ ac.getRendIny().getValor(instanteActual) * valEnerg / Constantes.SEGUNDOSXHORA));
				no.agregarTermino(getNpotAlmacp()[p], -(double) ((double) ac.getDuracionPostes(p)
						* ac.getRendAlmac().getValor(instanteActual) * valEnerg / Constantes.SEGUNDOSXHORA));
			}

		}
		return no;
	}

	public void contribuirObjetivo() {
		long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
		this.objetivo.contribuir(cargarObjetivoValEnerg());
		DatosObjetivo costo = new DatosObjetivo();
		for (int p = 0; p < ac.getCantPostes(); p++) {
			costo.agregarTermino(this.npotp[p], ac.getCostoVariable().getValor(instanteActual)
					* (double) ac.getDuracionPostes(p) / Constantes.SEGUNDOSXHORA);
			costo.agregarTermino(this.npotAlmacp[p], ac.getCostoVariable().getValor(instanteActual)
					* (double) ac.getDuracionPostes(p) / Constantes.SEGUNDOSXHORA);
		}
		if (hayPotObligatoria) {
			for (int p = 0; p < ac.getCantPostes(); p++) {
				costo.agregarTermino(this.npotAux[p], 1.0);
			}
		}

		costo.setTerminoIndependiente(ac.getCostoFijo().getValor(instanteActual));
		this.objetivo.contribuir(costo);
	}

	public double getPotIny(DatosSalidaProblemaLineal resultados, int p) {

		if (this.npotAlmacp == null || this.npotp == null)
			return 0;
		if (resultados.getSolucion().get(this.npotp[p]) == null)
			return 0;
		return resultados.getSolucion().get(this.npotp[p]);

	}

	public double getPotAcum(DatosSalidaProblemaLineal resultados, int p) {
		if (this.npotAlmacp == null || this.npotp == null)
			return 0;
		if (resultados.getSolucion().get(this.npotAlmacp[p]) == null)
			return 0;
		return resultados.getSolucion().get(this.npotAlmacp[p]);

	}

	public double calcularEnergIncremental() {
		double enerInyTot = 0;
		double enerAcumTot = 0;
		for (int i = 0; i < ac.getCantPostes(); i++) {
			enerInyTot += getPotIny(this.ultimoResultado, i) * ac.getDuracionPostes(i);
			enerAcumTot += getPotAcum(this.ultimoResultado, i) * ac.getDuracionPostes(i);
		}
		return (enerAcumTot - enerInyTot) / Constantes.SEGUNDOSXHORA;
	}

	public Acumulador getAc() {
		return ac;
	}

	public void setAc(Acumulador ac) {
		this.ac = ac;
	}

	public Double getPotMax() {
		return potMax;
	}

	public void setPotMax(Double potMax) {
		this.potMax = potMax;
	}

	public Double getPotMaxAlmac() {
		return potMaxAlmac;
	}

	public void setPotMaxAlmac(Double potMaxAlmac) {
		this.potMaxAlmac = potMaxAlmac;
	}

	public Double getEnergAlmacIni() {
		return energAlmacIni;
	}

	public void setEnergAlmacIni(Double energAlmacIni) {
		this.energAlmacIni = energAlmacIni;
	}

	public double getValEnerg() {
		return valEnerg;
	}

	public void setValEnerg(double valEnerg) {
		this.valEnerg = valEnerg;
	}

	public Integer getCantModDisp() {
		return cantModDisp;
	}

	public void setCantModDisp(Integer cantModDisp) {
		this.cantModDisp = cantModDisp;
	}

	public String[] getNpotAlmacp() {
		return npotAlmacp;
	}

	public void setNpotAlmacp(String[] npotAlmacp) {
		this.npotAlmacp = npotAlmacp;
	}

	public Double getEnergMax() {
		return energMax;
	}

	public void setEnergMax(Double energMax) {
		this.energMax = energMax;
	}

	public DatosSalidaProblemaLineal getUltimoResultado() {
		return ultimoResultado;
	}

	public void setUltimoResultado(DatosSalidaProblemaLineal ultimoResultado) {
		this.ultimoResultado = ultimoResultado;
	}

	public static String getCompPasoPorDefecto() {
		return compPasoPorDefecto;
	}

	public static void setCompPasoPorDefecto(String compPasoPorDefecto) {
		AcumuladorCompDesp.compPasoPorDefecto = compPasoPorDefecto;
	}

	public Double getFactorUso() {
		return factorUso;
	}

	public void setFactorUso(Double factorUso) {
		this.factorUso = factorUso;
	}

	public String getNombreRestriccionBalEnergia() {
		return nombreRestriccionBalEnergia;
	}

	public void setNombreRestriccionBalEnergia(String nombreRestriccionBalEnergia) {
		this.nombreRestriccionBalEnergia = nombreRestriccionBalEnergia;
	}

	public String[] getnEnerAlmacp() {
		return nEnerAlmacp;
	}

	public void setnEnerAlmacp(String[] nEnerAlmacp) {
		this.nEnerAlmacp = nEnerAlmacp;
	}

	public Double getEnergAlmacIniPoste() {
		return energAlmacIniPoste;
	}

	public void setEnergAlmacIniPoste(Double energAlmacIniPoste) {
		this.energAlmacIniPoste = energAlmacIniPoste;
	}

	public double[] getPotObligatoria() {
		return potObligatoria;
	}

	public void setPotObligatoria(double[] potObligatoria) {
		this.potObligatoria = potObligatoria;
	}

	public boolean isHayPotObligatoria() {
		return hayPotObligatoria;
	}

	public void setHayPotObligatoria(boolean hayPotObligatoria) {
		this.hayPotObligatoria = hayPotObligatoria;
	}

	public double getCostoFallaPOblig() {
		return costoFallaPOblig;
	}

	public void setCostoFallaPOblig(double costoFallaPOblig) {
		this.costoFallaPOblig = costoFallaPOblig;
	}

	public String[] getNpotAux() {
		return npotAux;
	}

	public void setNpotAux(String[] npotAux) {
		this.npotAux = npotAux;
	}

	public void setEnergAlmacFinPaso(Double valor) {
		this.energAlmacFinPaso = valor;

	}

}
