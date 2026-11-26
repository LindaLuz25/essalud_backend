package pe.edu.idat.essaludmiconsulta.model.enums;

public enum EstadoListaEspera {
    // En uso: se pone al anotarse (ListaEsperaServiceImpl.anotarse).
    ACTIVO,

    // Reservado para una futura mejora (ej. marcar que ya se le avisó al
    // menos una vez, para no repetir el mismo aviso). Con el diseño
    // actual (RN-08) se notifica a TODOS los ACTIVO cada vez que se libera
    // un cupo — de lo contrario alguien que ya fue notificado una vez
    // dejaría de enterarse de futuras liberaciones. No se asigna en
    // ningún punto del código todavía.
    NOTIFICADO,

    // En uso: se pone cuando el paciente agenda una cita de la misma
    // especialidad por la que estaba esperando
    // (ListaEsperaServiceImpl.cerrarEsperaSiExiste) — ya no necesita
    // seguir recibiendo avisos.
    ATENDIDO,

    // Reservado para una futura mejora (expirar inscripciones muy
    // antiguas). No se asigna en ningún punto del código todavía.
    EXPIRADO
}
