package com.seuprojeto.kanban.service;
import com.seuprojeto.kanban.domain.*; import org.junit.jupiter.api.*; import static org.junit.jupiter.api.Assertions.*; import java.time.LocalDate;
public class ProjetoServiceRulesTest {
  @Test void statusAtrasadoQuandoTerminoPrevistoPassouESemConclusao(){
    ProjetoServiceImpl svc = new ProjetoServiceImpl(null,null,null,null);
    Projeto p = new Projeto();
    p.setNome("Teste");
    p.setInicioPrevisto(LocalDate.now().minusDays(10));
    p.setTerminoPrevisto(LocalDate.now().minusDays(1));
    svc.recalcularMetricas(p, LocalDate.now());
    StatusProjeto st = svc.recalcularStatus(p, LocalDate.now());
    assertEquals(StatusProjeto.ATRASADO, st);
    assertTrue(p.getDiasAtraso() >= 1);
    assertEquals(0, p.getPercentualTempoRestante());
  }
}
