package DAO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import Entidades.Venta;
import java.util.List;

/**
 * VentaDAO.java: CAPA DE PERSISTENCIA
 * 
 * Esta clase actúa de repositorio histórico de transacciones comerciales realizadas.
 **/
public class VentaDAO 
{
	// ========================
	// ATRIBUTOS DE LA CLASE
	// ========================
		
	// Historial acumulado de todas las ventas procesadas por el sistema.
    private List<Venta> historialVentas = new ArrayList<>();

    

	// ========================
	// MÉTODOS DE LA CLASE
	// ========================
	
    /** registrar(): almacena el registro de venta con la estampa de tiempo exacta. **/
    public void registrar(Venta v) { historialVentas.add(v); }

    /** buscarVentasPorProductoYMaquina(): filtra el historial para ver el desempeño de un producto en 
      * una máquina concreta. **/
    public List<Venta> buscarVentasPorProductoYMaquina(String idM, String idP) 
    {
        List<Venta> filtradas = new ArrayList<>();
        for (Venta v : historialVentas) 
        {
            if (v.getIdMaquina().equals(idM) && v.getIdProducto().equals(idP)) 
                filtradas.add(v);
        }
        return filtradas;
    }

    /** buscarDesdeFecha(): recupera las ventas ocurridas exclusivamente después de una fecha dada. **/
    public List<Venta> buscarDesdeFecha(String idM, String idP, LocalDateTime fecha) 
    {
        List<Venta> filtradas = new ArrayList<>();
        for (Venta v : historialVentas) 
        {
            if (v.getIdMaquina().equals(idM) && 
                v.getIdProducto().equals(idP) && 
                v.getFechaVenta().isAfter(fecha)) 
            {
                filtradas.add(v);
            }
        }
        return filtradas;
    }
}