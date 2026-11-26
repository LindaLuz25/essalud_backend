package pe.edu.idat.essaludmiconsulta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.idat.essaludmiconsulta.dto.RequisitoResponse;
import pe.edu.idat.essaludmiconsulta.repository.EspecialidadRequisitoRepository;
import pe.edu.idat.essaludmiconsulta.service.RequisitoService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequisitoServiceImpl implements RequisitoService {

    private final EspecialidadRequisitoRepository especialidadRequisitoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RequisitoResponse> listarPorEspecialidad(Integer idEspecialidad) {
        return especialidadRequisitoRepository.findByEspecialidad_IdEspecialidad(idEspecialidad).stream()
                .map(er -> new RequisitoResponse(
                        er.getRequisito().getIdRequisito(),
                        er.getRequisito().getNombre(),
                        er.getRequisito().getDescripcion(),
                        er.getObligatorio()))
                .toList();
    }
}
