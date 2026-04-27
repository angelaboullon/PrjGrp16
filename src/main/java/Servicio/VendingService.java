package Servicio;

import Entidades.*;
import DAO.*;
import Excepciones.*;
import java.util.List;

public class VendingService {
    private MaquinaDAO maquinaDAO;
    private LocalizacionDAO localizacionDAO;

    // Constructor: Solo necesitamos los DAOs de máquinas y localizaciones para HU1
    public VendingService(MaquinaDAO mDao, LocalizacionDAO lDao) {
        this.maquinaDAO = mDao;
        this.localizacionDAO = lDao;
    }

    // HU1: Registro de una ubicación en el sistema
    public void registrarLocalizacion(Localizacion l) throws DuplicateLocationException {
        if (localizacionDAO.buscarPorCoordenadas(l.getLatitud(), l.getLongitud()) != null) {
            throw new DuplicateLocationException("Ya existe una localización en estas coordenadas.");
        }
        localizacionDAO.insertar(l);
    }

    // HU1: Alta de una máquina asociada a una localización
    public void darAltaMaquina(MaquinaExpendedora m, Localizacion l) throws Exception {
        // 1. Intentar registrar localización (si no existe ya)
        try {
            registrarLocalizacion(l);
        } catch (DuplicateLocationException e) {
            // Si ya existe, se permite continuar para asociar la máquina a ella
        }

        // 2. Regla de negocio: Comprobar si la localización ya tiene una máquina asignada
        for (MaquinaExpendedora maq : maquinaDAO.listarTodas()) {
            if (maq.getLocalizacion().getLatitud() == l.getLatitud() && 
                maq.getLocalizacion().getLongitud() == l.getLongitud()) {
                throw new LocationOccupiedException("Esta localización ya tiene una máquina asignada.");
            }
        }

        // 3. Validar que el ID y el Nombre de la máquina sean únicos en el sistema
        if (maquinaDAO.buscarPorId(m.getId()) != null) {
            throw new DuplicateIdentifierException("ID de máquina repetido.");
        }
        
        // Asignación y guardado
        m.setLocalizacion(l);
        maquinaDAO.insertar(m);
    }
}