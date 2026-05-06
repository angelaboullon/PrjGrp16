package DAO;

import Entidades.MaquinaExpendedora;
import java.util.ArrayList;
import java.util.List;

/**
 * MaquinaDAO.java: CAPA DE PERSISTENCIA
 * 
 * Esta clase gestiona el almacenamiento y recuperación de las unidades de máquinas expendedoras.
 **/
public class MaquinaDAO 
{
	// ========================
	// ATRIBUTOS DE LA CLASE
	// ========================
		
	// Fuente de datos en memoria: almacena todas las máquinas registradas durante la ejecución.
	private List<MaquinaExpendedora> listaMaquinas = new ArrayList<>();

    

	// ========================
	// MÉTODOS DE LA CLASE
	// ========================
	
	/** insertar(): guarda físicamente la instancia de una máquina expendedora en la lista.**/
    public void insertar(MaquinaExpendedora m) { listaMaquinas.add(m); }

    /** buscarPorId(): recupera una máquina expendedora específica mediante su código identificador. **/
    public MaquinaExpendedora buscarPorId(String id) 
    {
        for (MaquinaExpendedora m : listaMaquinas) 
        {
            if (m.getId().equalsIgnoreCase(id)) return m;
        }
        return null;
    }
    
    /** buscarPorNombre(): localiza una máquina basándose en su nombre comercial. **/
    public MaquinaExpendedora buscarPorNombre(String nombre)
    {
    	for (MaquinaExpendedora m: listaMaquinas)
    	{
    		if (m.getNombre().equalsIgnoreCase(nombre)) return m;
    	}
    	return null;
    }
    
    /** listarTodas(): devuelve una copia de la lista completa de máquinas. **/
    public List<MaquinaExpendedora> listarTodas() { return new ArrayList<>(listaMaquinas); }

 
}