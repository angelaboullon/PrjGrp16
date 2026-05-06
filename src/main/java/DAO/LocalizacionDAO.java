package DAO;

import Entidades.Localizacion;
import java.util.ArrayList;
import java.util.List;

/**
 * LocalizacionDAO.java: CAPA DE PERSISTENCIA
 * 
 * Esta clase es la encargada de gestionar los puntos geográficos donde se instalan las máquinas.
 **/
public class LocalizacionDAO 
{
	// ========================
	// ATRIBUTOS DE LA CLASE
	// ========================
	
	//Registro de todas las direcciones y coordenadas almacenadas en el sistema.
	private List<Localizacion> listaUbics = new ArrayList<>();

    

	// ========================
	// MÉTODOS DE LA CLASE
	// ========================
	
	/** insertar(): añade una nueva localización al registro. **/
    public void insertar(Localizacion l) { listaUbics.add(l); }

    /** buscarPorCoordenadas(): verifica si una ubicación ya existe comparando sus coordenadas GPS. **/
    public Localizacion buscarPorCoordenadas(double lat, double lon) 
    {
        for (Localizacion l : listaUbics) 
        {
            // Se utiliza Double.compare para manejar la precisión de los números decimales.
        	if (Double.compare(l.getLatitud(), lat) == 0 && 
                Double.compare(l.getLongitud(), lon) == 0) {
                return l;
            }
        }
        return null;
    }
}