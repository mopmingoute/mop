package cp_despacho;

public class NombreBaseVar {
	
	private String nomVar;
	private String nomPar;
	private int entero;
	
		
	
	public NombreBaseVar(String nomVar, String nomPar, int entero) {
		super();
		this.nomVar = nomVar;
		this.nomPar = nomPar;
		this.entero = entero;
	}
	
	
	/**
	 * Devuelve el nombre de la variable de control del problema lineal con nomVar, nomPar y entero de this
	 * y el escenario desde el origen de vecEsc
	 * @param vecEsc
	 * @return
	 */
	public String devuelveNombreVariableControl(int[] vecEsc){
		String nomVarPar = BaseVar.generaNomVarPar(nomVar, nomPar);
		return BaseVar.generaNomVar(nomVarPar, entero, vecEsc);
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
	public int getEntero() {
		return entero;
	}
	public void setEntero(int entero) {
		this.entero = entero;
	}
	
	
	

}
