package cp_salidas;

import java.util.Hashtable;

import cp_compdespProgEst.CompDespPE;
import cp_datatypesEntradas.DatosGeneralesCP;
import cp_despacho.BaseRest;
import cp_despacho.BaseVar;
import cp_despacho.GrafoEscenarios;
import datatypesProblema.DatosSalidaProblemaLineal;
import parque.Corrida;
import utilitarios.UtilArrays;

public class BuscadorResolvedoresMOP extends BuscadorResult {

	
	private DatosSalidaProblemaLineal dsPL;
		

	public BuscadorResolvedoresMOP(String origenResultadosPL, Object objetoOrigen, Hashtable<String, Boolean> cat, Corrida corrida, GrafoEscenarios ge,
			DatosGeneralesCP dGCP) {
		super(origenResultadosPL, objetoOrigen, corrida, ge, dGCP);
		dsPL = (DatosSalidaProblemaLineal) objetoOrigen;
	}
	
	
	@Override
	public boolean existeResult(String nomVar, String nomPar) {
		
		// TODO 
		return false;
	}
	
	@Override
	public ResultadoEsc1V dameVariable(String nomVar, String nomPar, int[] vecEsc) {
		boolean esPoste;
		String clave = BaseVar.generaNomVarPar(nomVar, nomPar);
		if(BaseVar.getCatalogoVarParPos().get(clave)==null) {
			int pp = 0;
		}
		System.out.println(clave);
		esPoste = BaseVar.getCatalogoVarParPos().get(clave);
		Double[] valores;
		int cantE = 0;
		if(esPoste) {
			cantE = dGCP.getCantPostes();
		}else {
			cantE = dGCP.getCantDias();
		}
		valores = new Double[cantE];
		for(int e=0; e<cantE; e++) {
			BaseVar bv = CompDespPE.dameBaseVar(nomVar, nomPar, e);
			if(bv!=null) {			
				String nVCPL = ge.nombreVCPL(bv, vecEsc);
				valores[e] = dsPL.getSolucion().get(nVCPL);
			}else {
				valores[e]=null;
			}
		}
		
		ResultadoEsc1V result = new ResultadoEsc1V(nomVar, nomPar, vecEsc, esPoste, ge, valores);
		return result;
		
	}


	@Override
	public ResultadoEsc1V dameDuales(String nomRest, String nomPar, int[] vecEsc) {
		int cantPos = dGCP.getCantPostes();
		Double[] valores = new Double[cantPos];
		int[] vecEscP = null;
		for(int p=0; p<cantPos; p++) {
			int etapap = ge.etapaDePoste(p);
			vecEscP = UtilArrays.truncaNFinalesI(vecEsc, dGCP.getCantEtapas()-etapap-1);
			String nomBR = BaseRest.generaNomBRest(nomRest, nomPar, p);
			String clave = BaseRest.creaNomRestEvolEsc(nomBR, vecEscP);
			valores[p] = dsPL.getDuales().get(clave);
		}
		ResultadoEsc1V result = new ResultadoEsc1V(nomRest, nomPar, vecEscP, true, ge, valores);
		return result;		
	}


	@Override
	public double dame1ValorVariable(String nomVCPL) {
		return dsPL.getSolucion().get(nomVCPL);
	}


	@Override
	public double dame1ValorDual(String nomRest) {
		return dsPL.getDuales().get(nomRest);
	}
	
	
	@Override
	public double dameObjetivo() {
		return dsPL.getValorOptimo();
	}
	
	
}
