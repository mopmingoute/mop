/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * PEDisponibilidadGeometrica is part of MOP.
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

package procesosEstocasticos;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Hashtable;


import tiempo.Evolucion;
import tiempo.PasoTiempo;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;
import estado.VariableEstado;

/**
 * TIEMPO DE ARREGLO ALEATORIO, AL MENOS DE UN PASO
 * ================================================ 
 * Representa un proceso estocástico de disponibilidad con tiempo de espera antes de la rotura y antes
 * del arreglo geométrico Se preserva el tiempo medio de arreglo y la
 * disponibilidad promedio. No se conserva la distribución de probabilidad de la
 * disponibilidad en un período prolongado, sino sólo la media.
 * 
 * Los módulos se rompen o arreglan sólo cuando se invoca el método
 * producirRealizacion
 * 
 * Distribución geométrica X número de ensayo en el que se obtiene el primer
 * éxito, p es la probabilidad de éxito
 * 
 * Prob(X=k) = (1-p)**(k-1) * p ; para k = 1, 2, 3, ..... E(X) = 1/p
 * 
 * El tiempo de rotura es el número de ensayo en el que se rompe. Dado que se
 * arregló, al menos esta disponible un período antes de romperse
 * 
 * El tiempo de arreglo es el número de ensayo en el que se arregla. Dado que se
 * rompió, al menos está roto un período antes de arreglarse.
 * 
 * Los parámetros son: 
 * Ta dato leído: tiempo medio de arreglo expresado en días
 * d dato leído: disponibilidad media de un módulo (adimensionado)
 * 
 * Taua tiempo medio de duración de arreglos expresado en pasos (en realidad es
 * la cantidad adimensionada) Taua = Ta *(NSegDía / NSegPaso)
 * 
 * SEGUNDOSXDIA cantidad de segundos de un día, una constante del programa
 * DeltaT duración del paso del proceso estocástico
 * 
 * r probabilidad de rotura al cabo de un intervalo deltaT=NSegPaso (paso de
 * tiempo del proceso estocástico), que está expresado en segundos 
 * a probabilidad de arreglo al cabo de un intervalo deltaT=NSegPaso (paso de
 * tiempo del proceso estocástico) 
 * Ambas son las probabilidades que se emplean
 * para sortear la rotura o arreglo al inicio de cada paso, que es el "éxito" de
 * la distribución geométrica.
 * 
 * a = 1/Taua; Si resultase a>1 incluso por Taua = 0, NO FUNCIONA ESTE PROCESO.
 * SE TRANSFORMA EN UN PROCESO DE ROTURAS INDEPENDIENTE ENTRE PASOS CON r = 1-d
 * 
 * La disponibilidad media d es el cociente 
 * (tiempo medio disponible)/(tiempo medio disponible + tiempo medio roto) =
 * d = (1/r) / [ (1/r) + taua ]
 * 
 * De acá se despeja el r
 *
 * Se considerarán las probabilidades del intervalo relevante, esto es entre
 * instanteCorrienteFinal y el instante invocado En cada invocación la
 * probabilidad de rotura r se calcula de la siguiente manera: Si d>0 r =
 * (1-d)/(Taua*d)
 * 
 * Si d=0, SE TRANSFORMA EN UN PROCESO DE ROTURAS INDEPENDIENTE ENTRE PASOS CON
 * r = 1-d = 1
 * 
 * donde d es la disponibilidad media
 * 
 * ROTURAS INDEPENDIENTES ENTRE PASOS ta=-1
 * =========================================
 * 
 * Esto por convención quiere decir que en cada paso se sortea la disponibilidad
 * con probabilidad de rotura r=1-d, independientemente del paso anterior.
 * 
 * 
 * 
 * 
 */

public class PEDisponibilidadGeometrica extends ProcesoEstocastico {
	private Evolucion<Double> dispMedia;
	private Evolucion<Double> tMedioArreglo;
	private Evolucion<Integer> cantMod; // cantidad de módulos instalados del
										// recurso según el instante de tiempo
	private Evolucion<Integer> cantModMant; // cantidad de módulos en
											// mantenimiento del
	// recurso seg�n el instante de tiempo
	private VariableAleatoria cantDisponibles;
	private boolean primeraInvocacion;
	private String nombreRecurso;
	private Integer cantDispIni;
	private long instanteInicialPrimerPaso;
	private long instanteFinalPrimerPaso;

	public PEDisponibilidadGeometrica(String nombre, String nombreRecurso, Evolucion<Double> dispMedia,
			Evolucion<Double> tMedioArreglo, Evolucion<Integer> cantMod, Evolucion<Integer> cantModMant,
			VariableAleatoria cantDisponibles, int cantInnovaciones, int cantDispIni,
			long instanteInicialPrimerPaso, long instanteFinalPrimerPaso) {
		super();
		this.setNombre(nombre);
		this.dispMedia = dispMedia;
		this.tMedioArreglo = tMedioArreglo;
		this.cantMod = cantMod;
		this.cantDisponibles = cantDisponibles;
		this.setCantModMant(cantModMant);
		primeraInvocacion = true;
		this.setCantidadInnovaciones(cantInnovaciones);
		this.nombreRecurso = nombreRecurso;
		this.cantDispIni = cantDispIni;
		this.instanteInicialPrimerPaso = instanteInicialPrimerPaso;
		this.instanteFinalPrimerPaso = instanteFinalPrimerPaso;
		this.setCantVA(1);  // TODO OJO, CAMBIO DEL 20/3/2021
		this.setUsoOptimizacion(true);
		this.setUsoSimulacion(true);

	}

	/** En la entrada cantDisp de la realización se devuelve la suma */

	private void producirRealizacionOptim(long instante) {
				
		Double dispMediaActual = dispMedia.getValor(instante);
		Double tMedioArregloActual = tMedioArreglo.getValor(instante);
		Double nSegPaso = (double) this.getOptimizadorPaso().getDuracionPaso();
		Double tauA = tMedioArregloActual * (Constantes.SEGUNDOSXDIA / nSegPaso);

		Double r = (1 - dispMediaActual) / (tauA * dispMediaActual);
		Double a = 1 / tauA;

		ArrayList<GeneradorDistUniforme> genAleat = this.getGeneradoresAleatorios();
		int suma = 0;
		VariableAleatoria valeat;
		for (int i = 0; i < cantMod.getValor(instante) - cantModMant.getValor(instante); i++) {

			valeat = this.getVariablesAleatorias().get(i);
			Integer valorViejo = valeat.getValor().intValue();
			Integer valorNuevo = valorViejo;
			
			r = 1 - dispMediaActual;
			double innovacion = genAleat.get(i).generarValor();
			if (innovacion < r)
				valorNuevo = 0;
			else
				valorNuevo = 1;
			
			valeat.setValor(valorNuevo.doubleValue());
			suma += valorNuevo;
		}

		this.cantDisponibles.setValor((double)suma);
		
	}


	public void producirRealizacion(long instante) {
		if (optim) {
			
			producirRealizacionOptim(instante);

		} else {
			if (instante <= instanteFinalPrimerPaso && instante >= instanteInicialPrimerPaso) {
				int cantInstIni = cantMod.getValor(instante);
				this.cantDisponibles.setValor((double)(Math.min(cantDispIni, cantInstIni)));
				this.setInstanteCorrienteInicial(instanteInicialPrimerPaso);  
				this.setInstanteCorrienteFinal(instanteFinalPrimerPaso);
				primeraInvocacion = false;
			} else {
				if (instante > this.getInstanteCorrienteFinal()) {
					this.setInstanteCorrienteInicial(this.getInstanteCorrienteFinal());
					int nSegPaso =  (this.getSimuladorPaso().getDuracionPaso());
					//DirectoriosYArchivos.agregaTexto("d:\\detalleGeneradoresAleat.txt","SEGUNDOS PASO: " + Integer.toString(nSegPaso));
					this.setInstanteCorrienteFinal(this.getInstanteCorrienteFinal() + nSegPaso);
					if (!primeraInvocacion) {
						boolean procesoSimple = false;
						Double dispMediaActual = dispMedia.getValor(instante);
						Double tMedioArregloActual = tMedioArreglo.getValor(instante);						
						Double tauA = tMedioArregloActual * (Constantes.SEGUNDOSXDIA / (double)nSegPaso);

						Double r = (1 - dispMediaActual) / (tauA * dispMediaActual);
						Double a = 1 / tauA;

						if (a >= 1 || tMedioArregloActual == -1 || dispMediaActual == 0)
							procesoSimple = true;

						ArrayList<GeneradorDistUniforme> genAleat = this.getGeneradoresAleatorios();
						int suma = 0;
						VariableAleatoria valeat;
						for (int i = 0; i < cantMod.getValor(instante) - cantModMant.getValor(instante); i++) {							
							valeat = this.getVariablesAleatorias().get(i);
							Integer valorViejo = valeat.getValor().intValue();
							String texto = "VALORVIEJOI: " + i + " : " + valorViejo;
							//DirectoriosYArchivos.agregaTexto("d:\\detalleGeneradoresAleat.txt", texto);
							Integer valorNuevo = valorViejo;
							double innovacion = 0.0;
							if (!procesoSimple) {
								innovacion = genAleat.get(i).generarValor();
								texto = "PROCESOCOMPLICADO: " + this.getNombre() + "INNOVACION: " + innovacion;
								//DirectoriosYArchivos.agregaTexto("d:\\detalleGeneradoresAleat.txt", texto);	
								if (valorViejo == 0) {
									if (innovacion < a)
										valorNuevo = 1;
								} else if (valorViejo == 1) {
									if (innovacion < r)
										valorNuevo = 0;
								}
							} else {
								r = 1 - dispMediaActual;
								innovacion = genAleat.get(i).generarValor();
								texto = "PROCESOSIMPLE: " + this.getNombre() + "INNOVACION: " + innovacion;
								//DirectoriosYArchivos.agregaTexto("d:\\detalleGeneradoresAleat.txt", texto);	
								if (innovacion < r)
									valorNuevo = 0;
								else
									valorNuevo = 1;
							}
//							/**
//							 *  TODO ESCRITURA PARA DEBUG	
//							 */
//							System.out.println("innovacion " + innovacion);
							
							valeat.setValor(valorNuevo.doubleValue());
							suma += valorNuevo;
						}						
						this.cantDisponibles.setValor((double)suma);

					} 
					
				}
			}
		}
		
		String texto = "PROCESO: " + this.getNombre() + " : " +  this.cantDisponibles.getValor() + " DISPMEDIA: "+ dispMedia.getValor(instante) +" TMEDIOARREGLO: " + tMedioArreglo.getValor(instante);
		//DirectoriosYArchivos.agregaTexto("d:\\detalleGeneradoresAleat.txt", texto);	
	}
	
	

	public void prepararPasoOptim(int cantSort) {
		int cantViejaGA = this.getVariablesAleatorias().size();
		if (cantMod.getValor(this.getInstIniPasoOptim()) > this.getVariablesAleatorias().size()) {
			int nuevas_VA = cantMod.getValor(this.getInstIniPasoOptim()) - this.getVariablesAleatorias().size();
			for (int i = 0; i < nuevas_VA; i++) {
				VariableAleatoria modI = new VariableAleatoria();
				modI.setNombre(this.nombreRecurso + "modDisp_" + i);
				modI.setMuestreada(false);
				modI.setValor(1.0);

				this.getVariablesAleatorias().add(modI);
				this.getNombresVarsAleatorias().add(modI.getNombre());

			}
			setCantidadInnovaciones(cantMod.getValor(this.getInstIniPasoOptim()));

			for (int i = cantViejaGA; i < this.getCantidadInnovaciones(); ++i) {
				this.getGeneradoresAleatorios()
						.add(new GeneradorDistUniformeLCXOr(
								generarInnovacionInicial(this.getSemGeneral(), this.getNombresVarsAleatorias().get(i),
										this.getInicioSorteos(), this.getEscenario(), i)));
				this.getInnovacionesCorrientes().add(this.getGeneradoresAleatorios().get(i).generarValor());
			}
		}
	}

	public void prepararPaso() {
		int cantViejaGA = this.getVariablesAleatorias().size();
		if (cantMod.getValor(this.getInstanteCorrienteInicial()) > this.getVariablesAleatorias().size()) {
			int nuevas_VA = cantMod.getValor(this.getInstanteCorrienteInicial()) - this.getVariablesAleatorias().size();
			for (int i = 0; i < nuevas_VA; i++) {
				VariableAleatoria modI = new VariableAleatoria();
				modI.setNombre(this.nombreRecurso + "modDisp_" + i);
				modI.setMuestreada(false);
				modI.setValor(1.0);

				this.getVariablesAleatorias().add(modI);
				this.getNombresVarsAleatorias().add(modI.getNombre()); 

			}
			setCantidadInnovaciones(cantMod.getValor(this.getInstanteCorrienteInicial()));

			for (int i = cantViejaGA; i < this.getCantidadInnovaciones(); ++i) {
				this.getGeneradoresAleatorios()
						.add(new GeneradorDistUniformeLCXOr(
								generarInnovacionInicial(this.getSemGeneral(), this.getNombresVarsAleatorias().get(i),
										this.getInicioSorteos(), this.getEscenario(), i)));
				this.getInnovacionesCorrientes().add(this.getGeneradoresAleatorios().get(i).generarValor());
			}
		}
	}
	
	@Override
	public void alimentarPronosticoEnAutoregresivos(long instante) {
		// Deliberadamente en blanco
	}

	@Override
	public void inicializar(Semilla semGeneral, GregorianCalendar inicioSorteos, GregorianCalendar inicioCorrida,
			int escenario) {
		super.inicializar(semGeneral, inicioSorteos, inicioCorrida, escenario);
		
		int cantdisp = cantDispIni;
		for (VariableAleatoria vat: this.getVariablesAleatorias()) {
			if (cantdisp > 0) {
				vat.setValor(1.0);
				cantdisp--;
			} else {
				vat.setValor(0.0);
			}
		}
		primeraInvocacion = true;		
		
		PasoTiempo pasoInicial = this.getSimuladorPaso().getCorrida().getLineaTiempo().getLinea().get(0);
		this.instanteInicialPrimerPaso = pasoInicial.getInstanteInicial(); 
		this.instanteFinalPrimerPaso = this.instanteInicialPrimerPaso+ this.getSimuladorPaso().getCorrida().getDuracionPaso();
	}

//	@Override
//	public Hashtable<String, VariableEstado> dameEstadoAgregado(ArrayList<String> nomVarEstado, String modoReduccion) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	public Evolucion<Double> getDispMedia() {
		return dispMedia;
	}

	public void setDispMedia(Evolucion<Double> dispMedia) {
		this.dispMedia = dispMedia;
	}

	public Evolucion<Double> gettMedioArreglo() {
		return tMedioArreglo;
	}

	public void settMedioArreglo(Evolucion<Double> tMedioArreglo) {
		this.tMedioArreglo = tMedioArreglo;
	}

	public VariableAleatoria getCantDisponibles() {
		return cantDisponibles;
	}

	public void setCantDisponibles(VariableAleatoria cantDisponibles) {
		this.cantDisponibles = cantDisponibles;
	}

	public boolean isPrimeraInvocacion() {
		return primeraInvocacion;
	}

	public void setPrimeraInvocacion(boolean primeraInvocacion) {
		this.primeraInvocacion = primeraInvocacion;
	}

	@Override
	public double valorVA(String nombreVA) {
		for (VariableAleatoria va : this.getVariablesAleatorias()) {
			if (va.getNombre().equalsIgnoreCase(nombreVA))
				return va.getValor();
		}
		return -1;
	}

	public Evolucion<Integer> getCantMod() {
		return cantMod;
	}

	public void setCantMod(Evolucion<Integer> cantMod) {
		this.cantMod = cantMod;
	}



	@Override
	public boolean tieneVEOptim() {
		// TODO Auto-generated method stub
		return false;
	}

	public Evolucion<Integer> getCantModMant() {
		return cantModMant;
	}

	public void setCantModMant(Evolucion<Integer> cantModMant) {
		this.cantModMant = cantModMant;
	}

	public String getNombreRecurso() {
		return nombreRecurso;
	}

	public void setNombreRecurso(String nombreRecurso) {
		this.nombreRecurso = nombreRecurso;
	}

	public Integer getCantDispIni() {
		return cantDispIni;
	}

	public void setCantDispIni(Integer cantDispIni) {
		this.cantDispIni = cantDispIni;
	}

	public long getInstanteInicialPrimerPaso() {
		return instanteInicialPrimerPaso;
	}

	public void setInstanteInicialPrimerPaso(long instanteInicialPrimerPaso) {
		this.instanteInicialPrimerPaso = instanteInicialPrimerPaso;
	}

	public long getInstanteFinalPrimerPaso() {
		return instanteFinalPrimerPaso;
	}

	public void setInstanteFinalPrimerPaso(long instanteFinalPrimerPaso) {
		this.instanteFinalPrimerPaso = instanteFinalPrimerPaso;
	}
	
	
	@Override
	public void cargarVAOptim(int isort, long[] instantesMuestreo) {
		double suma = 0;
		for (VariableAleatoria va: this.getVariablesAleatorias()) {
			suma += va.getUltimoMuestreoOptim()[isort][0];
		}
		
		cantDisponibles.setValor(suma);
	}

	@Override
	public void producirRealizacionPEEstadoOptim(long[] instantesMuestreo, double[][] innovaciones1Sort, int isort) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preparaUnSorteoMontecarloPEsinVEOptim() {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public int pasoDelAnio(long instante){
		return -1;		
	}

	@Override
	public void producirRealizacionSinPronostico(long instante) {
		// DELIBERADAMENTE EN BLANCO 
	}
	
				
}
