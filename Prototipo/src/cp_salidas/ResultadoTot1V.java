package cp_salidas;

import java.util.ArrayList;
import java.util.Hashtable;

import cp_despacho.GrafoEscenarios;

	/**
	 * Datatype que guarda los resultados de una variable en todos los escenarios
	 * @author ut469262
	 *
	 */
	public class ResultadoTot1V {
		
		private String nomVar;  // nombre de la BaseVar o la BaseRest (para las duales)
		
		private String nomPar;
		
		private boolean esPoste;  // true si la variable tiene un valor por poste y false si tiene un valor por día.
		
		private GrafoEscenarios ge;
		
		/**
		 * Los valores por poste o por día según corresponda para todos los escenarios
		 * primer índice escenario
		 * segundo índice poste o día 
		 */
		private Double[][] valores;
		
		

		public ResultadoTot1V(String nomVar, String nomPar, boolean esPoste, GrafoEscenarios ge, Double[][] valores) {
			super();
			this.nomVar = nomVar;
			this.nomPar = nomPar;
			this.esPoste = esPoste;
			this.ge = ge;
			this.valores = valores;
		}

		public String getNomVar() {
			return nomVar;
		}

		public void setNomVar(String nomVar) {
			this.nomVar = nomVar;
		}

		public String getNomPar() {
			return nomPar;
		}

		public void setNomPar(String nomPar) {
			this.nomPar = nomPar;
		}

		public boolean isEsPoste() {
			return esPoste;
		}

		public void setEsPoste(boolean esPoste) {
			this.esPoste = esPoste;
		}

		public GrafoEscenarios getGe() {
			return ge;
		}

		public void setGe(GrafoEscenarios ge) {
			this.ge = ge;
		}

		public Double[][] getValores() {
			return valores;
		}

		public void setValores(Double[][] valores) {
			this.valores = valores;
		} 
		
		
		

}
