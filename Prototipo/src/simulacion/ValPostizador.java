/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ValPostizador is part of MOP.
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
import java.util.GregorianCalendar;

import logica.CorridaHandler;
import parque.Corrida;
import procesosEstocasticos.GeneradorDistUniformeLCXOr;
import procesosEstocasticos.IniciadorSemilla;
import procesosEstocasticos.ProcesoEstocastico;
import procesosEstocasticos.Semilla;
import procesosEstocasticos.VariableAleatoria;
import tiempo.SentidoTiempo;
import utilitarios.Constantes;
import utilitarios.Metodos;

public class ValPostizador {
	private boolean externa;
	private PostizacionPaso postPaso;
	private GeneradorDistUniformeLCXOr genUnif;
	private Semilla semGeneral;
	private GregorianCalendar inicioSorteos;
	private GregorianCalendar inicioCorrida;
	private ArrayList<Double> sorteosUnifPostes;
	 

	 


	public ValPostizador(Semilla semGeneral, GregorianCalendar inicioSorteos, GregorianCalendar inicioCorrida) {
		setExterna(true); 
		sorteosUnifPostes = new ArrayList<Double>();
		this.semGeneral = semGeneral;
		this.inicioSorteos = inicioSorteos;
		this.inicioCorrida = inicioCorrida;
	}
	
	
	
	public void inicializarParaOptimizacion(){		
		inicializarSorteosValpostizador(semGeneral, inicioSorteos, inicioCorrida, 0);		
	}
	
	public void inicializarParaEscenario(int esc){
		inicializarSorteosValpostizador(semGeneral, inicioSorteos, inicioCorrida, esc);
		
	}
	
	
	public void sortearUnifPostes(int cantPostes){
		sorteosUnifPostes.clear();
		for(int is=0; is<cantPostes; is++){
			sorteosUnifPostes.add(genUnif.generarValor());
		}
	}
	
	public void inicializarSorteosValpostizador(Semilla semGeneral, GregorianCalendar inicioSorteos, GregorianCalendar inicioCorrida, int escenario){
		genUnif = new GeneradorDistUniformeLCXOr(generarInnovacionInicial(semGeneral, "Valpostizador", inicioSorteos, escenario, 0));
	}
	
	
	
	protected int generarInnovacionInicial(Semilla general, String nombre, GregorianCalendar inicioSorteos, int escenario, int i) {
		IniciadorSemilla iniciador = new IniciadorSemilla(general, nombre, inicioSorteos, escenario, i);
		return iniciador.hashCode();
	}	
	
	/**
	 * Recibe los datos datos muestreados y devuelve los datos por poste de acuerdo a una postización ya existente y al tipo de valpostización
	 * @param datosMuestreados
	 * @param tipoValpostizacion aleatorio, promedio, maximo, minimo
	 * @return
	 */
	public double[] valPostizar(double[] datosMuestreados, int tipoValpostizacion, double valorPaso) {
			
		int cantPos = postPaso.getCantPos();
		double[] retorno = new double[cantPos];
		if (datosMuestreados==null) {
			for (int i =0; i<cantPos;i++) {
				retorno[i]=valorPaso;
			}
			return retorno;
		}
	
		Corrida ch = CorridaHandler.getInstance().getCorridaActual();
		if (ch.getCompDemanda().equalsIgnoreCase(Constantes.DEMRESIDUAL)) 
			tipoValpostizacion = Constantes.VALPPROMEDIO;
		ArrayList<Integer> numpos = postPaso.getNumpos();
		switch (tipoValpostizacion) {		
          case Constantes.VALPPROMEDIO:        	  
        	  for (int i = 0; i< datosMuestreados.length; i++){        		  
        		  retorno[numpos.get(i)-1]+=datosMuestreados[i];    
        	  }
        	  for (int j=0; j < retorno.length;j++) {
        		  retorno[j]/=postPaso.getInterPorPoste()[j];
        	  }        	 
        	  break;
          case Constantes.VALPALEAT:
        	  int[] contadores = new int[cantPos];
        	  for (int i = 0; i < cantPos; i++) contadores[i]=0;
        	  
        	  double[][] colecciones = new double[cantPos][];
        	  for (int i = 0; i < cantPos; i++) {
        		colecciones[i] = new double[postPaso.getInterPorPoste()[i]];        		
        	  }
        	  
        	  for (int i = 0; i< datosMuestreados.length; i++){ 
        		  colecciones[numpos.get(i)-1][contadores[numpos.get(i)-1]] = datosMuestreados[i];
        		  contadores[numpos.get(i)-1]++;
        	  }
        	  for (int i = 0; i< cantPos; i++){
        		  retorno[i] = Metodos.sortearAleatorio(colecciones[i], sorteosUnifPostes.get(i));     
        	  }        	  
              break;
          case Constantes.VALPMIN:
        	  for (int i = 0; i< datosMuestreados.length; i++){        		  
        		  if (retorno[numpos.get(i)-1] > datosMuestreados[i]) {
        			  retorno[numpos.get(i)-1] = datosMuestreados[i];
        		  }
        	  }
              break;
          case Constantes.VALPMAX:
        	  for (int i = 0; i< datosMuestreados.length; i++){
	        	  if (retorno[numpos.get(i)-1] < datosMuestreados[i]) {
	    			  retorno[numpos.get(i)-1] = datosMuestreados[i];
	    		  }
        	  }
        	  
		}
		return retorno;		
	}
	
	
	/*
	 * Devuelve los valores de la variable aleatoria por poste
	 * @param nombreVABase es el nombre de la variable aleatoria del participante
	 * que se va a valpostizar. Los nombres de las variables aleatorias por poste se forman agregando 
	 * al nombreVABase el nómero de poste: 1, 2, 3,...
	 * @param ph es el proceso estocóstico histórico del que se obtienen los valores de las variables aleatorias por poste.
	 */
	
	public double[] valPostizar(ProcesoEstocastico ph, String nombreVABase) { 
		int cantPos = postPaso.getCantPos();
		double[] retorno = new double[cantPos];		
		for (int i = 1; i <= cantPos; i++) {
			retorno[i-1] = ph.valorVA(nombreVABase + i);
		}		
		return retorno;		
	}

	
	public PostizacionPaso getPostPaso() {
		return postPaso;
	}

	public void setPostPaso(PostizacionPaso postPaso) {
		this.postPaso = postPaso;
	}

	public boolean isExterna() {
		return externa;
	}

	public void setExterna(boolean externa) {
		this.externa = externa;
	}

		
}
