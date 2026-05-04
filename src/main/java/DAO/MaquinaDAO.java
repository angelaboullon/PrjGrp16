package DAO;

import Entidades.MaquinaExpendedora;
import java.util.ArrayList;
import java.util.List;

public class MaquinaDAO 
{
    private List<MaquinaExpendedora> listaMaquinas = new ArrayList<>();

    // HU1 - Cargar máquinas en el sistema: persistencia física de la máquina
    // en la lista en memoria.
    public void insertar(MaquinaExpendedora m) { listaMaquinas.add(m); }

    public MaquinaExpendedora buscarPorId(String id) 
    {
        for (MaquinaExpendedora m : listaMaquinas) 
        {
            if (m.getId().equalsIgnoreCase(id)) return m;
        }
        return null;
    }

    public MaquinaExpendedora buscarPorNombre(String nombre)
    {
    	for (MaquinaExpendedora m: listaMaquinas)
    	{
    		if (m.getNombre().equalsIgnoreCase(nombre)) return m;
    	}
    	return null;
    }
    
    public List<MaquinaExpendedora> listarTodas() { return new ArrayList<>(listaMaquinas); }
    
    // public void actualizar
}