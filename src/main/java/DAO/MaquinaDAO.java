package DAO;

import Entidades.MaquinaExpendedora;
import java.util.ArrayList;
import java.util.List;

public class MaquinaDAO {
    private List<MaquinaExpendedora> listaMaquinas = new ArrayList<>();

    public void insertar(MaquinaExpendedora m) {
        listaMaquinas.add(m);
    }

    public MaquinaExpendedora buscarPorId(String id) {
        for (MaquinaExpendedora m : listaMaquinas) {
            if (m.getId().equalsIgnoreCase(id)) return m;
        }
        return null;
    }

    public List<MaquinaExpendedora> listarTodas() {
        return new ArrayList<>(listaMaquinas);
    }
}