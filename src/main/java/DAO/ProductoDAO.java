package DAO;

import Entidades.Producto;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductoDAO.java: CAPA DE PERSISTENCIA
 * 
 * Esta clase gestiona administra el catálogo global de productos disponibles para la venta.
 **/
public class ProductoDAO 
{
	// ========================
	// ATRIBUTOS DE LA CLASE
	// ========================
	
	// Repositorio en memoria del catálogo de productos.
	private List<Producto> catalogoProductos = new ArrayList<>();

	
	
	// ========================
	// MÉTODOS DE LA CLASE
	// ========================
		
	/** insertar(): registra un nuevo producto en el catálogo. **/
    public void insertar(Producto p) { catalogoProductos.add(p); }

    /** buscarPorId(): busca un producto por su identificador único de catálogo. **/
    public Producto buscarPorId(String id) 
    {
        for (Producto p : catalogoProductos) 
        {
            if (p.getId().equalsIgnoreCase(id)) return p;
        }
        return null;
    }

    /** buscarPorNombre(): busca un producto por su denominación exacta. **/
    public Producto buscarPorNombre(String nombre) 
    {
        for (Producto p : catalogoProductos) 
        {
            if (p.getNombre().equalsIgnoreCase(nombre)) return p;
        }
        return null;
    }

    /** listarCatalogo(): obtiene todos los productos registrados en el catálogo. **/
    public List<Producto> listarCatalogo() { return new ArrayList<>(catalogoProductos); }
}