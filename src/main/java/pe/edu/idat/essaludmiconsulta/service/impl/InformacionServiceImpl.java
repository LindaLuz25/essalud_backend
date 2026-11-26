package pe.edu.idat.essaludmiconsulta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.idat.essaludmiconsulta.dto.InformacionResponse;
import pe.edu.idat.essaludmiconsulta.model.InformacionGeneral;
import pe.edu.idat.essaludmiconsulta.repository.InformacionGeneralRepository;
import pe.edu.idat.essaludmiconsulta.service.InformacionService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InformacionServiceImpl implements InformacionService {

    private final InformacionGeneralRepository informacionGeneralRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InformacionResponse> listarTodo() {
        return informacionGeneralRepository.findByActivoTrueOrderByOrdenAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InformacionResponse> listarPorCategoria(String categoria) {
        return informacionGeneralRepository.findByActivoTrueAndCategoriaOrderByOrdenAsc(categoria).stream()
                .map(this::toResponse)
                .toList();
    }

    private InformacionResponse toResponse(InformacionGeneral info) {
        return new InformacionResponse(
                info.getIdInformacion(), info.getTitulo(), info.getContenido(), info.getCategoria());
    }
}
