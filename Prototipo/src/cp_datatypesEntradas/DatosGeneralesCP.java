/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosGeneralesCP is part of MOP.
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

package cp_datatypesEntradas;

public class DatosGeneralesCP {
	
	private String instIniCPS; // el instante inicial en formato aaaa/mm/dd/hh:mm:ss
	
	private long instIniEntrada; // el long asociado a instIniCPS
	
	
	/**
	 * El poste del día inicial en el que empieza el horizonte CP, con los postes del día 
	 * empezando en cero. Por ejemplo si los postes son de una hora y la corrida CP arranca a las
	 * 12 del mediodía, posteIniDia = 12.
	 * Se carga en el método DespachoProgramacionEstocastica.leerdatosGenerales()
	 */
	private int posteIniDia;  
	
	
	/**
	 * El instante inicial redondeado al inicio del poste posteIniDia del día inicial
	 */
	private long instIniCP;  
	
	private int cantPosDia;  // cantidad de postes por día
	private int cantPosHora; // cantidad de postes por hora
	private int dur1Pos; // duración de cada poste en segundos
	private int cantPostes; // cantidad total de postes del horizonte
	
	private int cantDias;   // cantidad de días del horizonte de estudio
	private int cantEtapas;  // cada etapa es un conjunto de postes consecutivos en los que no cambia el escenario
	private int[] cantPostesEtapas;  // cantidad de postes de cada etapa
	private int[] cantEscEtapa;  // cantidad de escenarios de cada etapa
	private int[] posIniEtapa; // poste inicial de cada etapa
	private int[] posFinEtapa; // poste final

	private boolean usaSoloEsc0;  // si es true usa solo el escenario 0 del grafo de escenarios, volviendo el problema determinístico
	private String tipoVBellman;  // incrementos o hiperplanos
	private boolean usaHip;  // se carga a partir de la entrada del participante ConstructorHiperplanosCP
	private String resolvedorLP;  // MOP u otro
	
	// DATOS PARA OBTENER LOS HIPERPLANOS DE LA FUNCIÓN DE VALOR AL FIN DEL HORIZONTE CP
	private int[] ordinalVEPrefijadas;
	private Double[] valorVEPrefijadas;
	private int cantVEPrefijadas;
	

	public String getInstIniCPS() {
		return instIniCPS;
	}


	public void setInstIniCPS(String instIniCPS) {
		this.instIniCPS = instIniCPS;
	}

	public long getInstIniCP() {
		return instIniCP;
	}


	public void setInstIniCP(long instIniCP) {
		this.instIniCP = instIniCP;
	}


	
	
	public int getPosteIniDia() {
		return posteIniDia;
	}


	public void setPosteIniDia(int posteIniDia) {
		this.posteIniDia = posteIniDia;
	}


	public int getCantPosDia() {
		return cantPosDia;
	}


	public void setCantPosDia(int cantPosDia) {
		this.cantPosDia = cantPosDia;
	}


	
	
	public int getCantPosHora() {
		return cantPosHora;
	}


	public void setCantPosHora(int cantPosHora) {
		this.cantPosHora = cantPosHora;
	}

	

	public int getDur1Pos() {
		return dur1Pos;
	}


	public void setDur1Pos(int dur1Pos) {
		this.dur1Pos = dur1Pos;
	}


	public int getCantDias() {
		return cantDias;
	}


	public void setCantDias(int cantDias) {
		this.cantDias = cantDias;
	}

	
	

	public int getCantPostes() {
		return cantPostes;
	}


	public void setCantPostes(int cantPostes) {
		this.cantPostes = cantPostes;
	}


	public int getCantEtapas() {
		return cantEtapas;
	}


	public void setCantEtapas(int cantEtapas) {
		this.cantEtapas = cantEtapas;
	}


	public int[] getCantPostesEtapas() {
		return cantPostesEtapas;
	}


	public void setCantPostesEtapas(int[] cantPostesEtapas) {
		this.cantPostesEtapas = cantPostesEtapas;
	}



	public boolean isUsaSoloEsc0() {
		return usaSoloEsc0;
	}


	public void setUsaSoloEsc0(boolean usaSoloEsc0) {
		this.usaSoloEsc0 = usaSoloEsc0;
	}


	public int[] getPosIniEtapa() {
		return posIniEtapa;
	}


	public void setPosIniEtapa(int[] posIniEtapa) {
		this.posIniEtapa = posIniEtapa;
	}


	public int[] getPosFinEtapa() {
		return posFinEtapa;
	}


	public void setPosFinEtapa(int[] posFinEtapa) {
		this.posFinEtapa = posFinEtapa;
	}


	public int[] getCantEscEtapa() {
		return cantEscEtapa;
	}


	public void setCantEscEtapa(int[] cantEscEtapa) {
		this.cantEscEtapa = cantEscEtapa;
	}


	public String getResolvedorLP() {
		return resolvedorLP;
	}


	public void setResolvedorLP(String resolvedorLP) {
		this.resolvedorLP = resolvedorLP;
	}


	public boolean isUsaHip() {
		return usaHip;
	}


	public void setUsaHip(boolean usaHip) {
		this.usaHip = usaHip;
	}


	public long getInstIniEntrada() {
		return instIniEntrada;
	}


	public void setInstIniEntrada(long instIniEntrada) {
		this.instIniEntrada = instIniEntrada;
	}


	public String getTipoVBellman() {
		return tipoVBellman;
	}


	public void setTipoVBellman(String tipoVBellman) {
		this.tipoVBellman = tipoVBellman;
	}


	public int[] getOrdinalVEPrefijadas() {
		return ordinalVEPrefijadas;
	}


	public void setOrdinalVEPrefijadas(int[] ordinalVEPrefijadas) {
		this.ordinalVEPrefijadas = ordinalVEPrefijadas;
	}




	public Double[] getValorVEPrefijadas() {
		return valorVEPrefijadas;
	}


	public void setValorVEPrefijadas(Double[] valorVEPrefijadas) {
		this.valorVEPrefijadas = valorVEPrefijadas;
	}


	public int getCantVEPrefijadas() {
		return cantVEPrefijadas;
	}


	public void setCantVEPrefijadas(int cantVEPrefijadas) {
		this.cantVEPrefijadas = cantVEPrefijadas;
	}
	
	
	
	
	
}
